package com.future.trader.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.future.trader.api.ConnectLibrary;
import com.future.trader.api.OrderLibrary;
import com.future.trader.api.QuoteLibrary;
import com.future.trader.api.TraderLibrary;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.common.constants.OrderConstant;
import com.future.trader.common.constants.RedisConstant;
import com.future.trader.common.enums.OrderUpdateActionEnum;
import com.future.trader.common.exception.BusinessException;
import com.future.trader.service.OrderInfoService;
import com.future.trader.service.SignalOrderUpdateCallback;
import com.future.trader.util.RedisManager;
import com.future.trader.util.TradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Service
public class SignalOrderUpdateCallbackImpl implements SignalOrderUpdateCallback {

    Logger log= LoggerFactory.getLogger(SignalOrderUpdateCallbackImpl.class);

    @Autowired
    RedisManager redisManager;
    @Resource
    OrderInfoService orderInfoService;


    @Override
    public void onUpdate(TraderLibrary.OrderUpdateEventInfo orderUpdateEventInfo, int clientId) {

        TradeRecordInfo info =TradeUtil.convertTradeRecords(orderUpdateEventInfo.tradeRecord);
        log.info("信号源订单更新回调，clientId：" + clientId);
        log.info("orderUpdateAction : " + orderUpdateEventInfo.updateAction.ordinal());
        log.info("orderInfo : " + JSON.toJSONString(info));

        if(info.getMagic()==OrderConstant.ORDER_FOLLOW_MAGIC){
            log.error("follow order:"+info.getOrder());
            return;
        }else {
            log.info("signal order:"+info.getOrder());
        }
        /*查询跟单信息*/
        Object object= redisManager.hget(RedisConstant.H_ACCOUNT_FOLLOW_RELATION,String.valueOf(info.getLogin()));
        if(ObjectUtils.isEmpty(object)){
            log.info("信号源无跟随账号 signalLogin:"+info.getLogin());
            return;
        }

        //TODO 社区跟随规则校验

        JSONObject followJson=new JSONObject();
        followJson=(JSONObject)object;
        OrderLibrary.TradeRecord tradeRecord = orderUpdateEventInfo.tradeRecord;
        try {
            for(String jsonkey:followJson.keySet()){
                int followName= followJson.getIntValue(jsonkey);
                //TODO 账号跟随规则校验
                if (OrderUpdateActionEnum.OUA_PositionOpen.getIntValue() == orderUpdateEventInfo.updateAction.ordinal()) {
                    log.info("信号源订单开仓,跟随账号："+followName);
                    //开仓
                    followTradeOpen(tradeRecord,followName);
                }else if (OrderUpdateActionEnum.OUA_PositionClose.getIntValue() == orderUpdateEventInfo.updateAction.ordinal()) {
                    log.info("信号源订单平仓,跟随账号："+followName);
                    // 平仓
                    followTradeClose(tradeRecord,followName);
                }else {
                    log.info("信号源订单其他交易,跟随账号："+followName);
                }
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 订单跟随逻辑close
     * @param tradeRecord
     * @param followName
     */
    private void followTradeClose(OrderLibrary.TradeRecord tradeRecord,int followName) {
        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(followName));
        if(ObjectUtils.isEmpty(accountClientId)){
            log.error("用户未连接："+followName);
            throw new BusinessException("用户未连接："+followName);
        }
        int clientId=(int)accountClientId;
        if (!ConnectLibrary.library.MT4API_IsConnect(clientId)) {
            log.info("connect borker false, clientId error!");
            throw new BusinessException("connect borker false, clientId error!");
        }

        String comment=followName+":"+tradeRecord.order;
        Object followOrder=redisManager.hget(RedisConstant.H_ORDER_FOLLOW_ORDER_RELATION,comment);
        if(ObjectUtils.isEmpty(followOrder)){
            log.error("用户："+followName+",未跟随该订单 order："+tradeRecord.order);
            return;
        }
        int followOrderId=(int)followOrder;

        QuoteLibrary.QuoteEventInfo.ByReference quoteInfo = new QuoteLibrary.QuoteEventInfo.ByReference();
        //循环获取行情信息，直到获取到最新的行情信息
        while (!QuoteLibrary.library.MT4API_GetQuote(clientId, new String(tradeRecord.symbol), quoteInfo)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                e.printStackTrace();
            }
        }

        if(TraderLibrary.library.MT4API_IsTradeAllowed(clientId)){
            double volume=tradeRecord.volume*0.01;
            log.info("跟单关闭："
                    + "clientId : " + clientId + ","
                    + "symbol : " + new String(tradeRecord.symbol).trim() + ","
                    + "order : " + followOrderId + ","
                    + "volume : " + tradeRecord.volume*0.01 + ","
                    + "fAsk : " + quoteInfo.fAsk + ","
                    + "nDigits : " + quoteInfo.nDigits + ","
            );
            int isSend= TraderLibrary.library.MT4API_OrderCloseAsync(clientId,new String(tradeRecord.symbol).trim(),followOrderId,
                    volume,quoteInfo.fAsk,40);
            if (isSend>0) {
                log.info("跟单信息：close success! isSend,followOrderid:"+followOrderId);
                //TODO 要做失败监测，里面放订单
                redisManager.hset(RedisConstant.H_ORDER_FOLLOW_CLOSING,comment,followOrderId);
            } else {
                TradeUtil.printError(clientId);
            }
        }
    }

    /**
     * 订单跟随逻辑open
     * @param tradeRecord
     */
    private void followTradeOpen(OrderLibrary.TradeRecord tradeRecord,int followName) {

        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(followName));
        if(ObjectUtils.isEmpty(accountClientId)){
            log.error("用户未连接："+followName);
            throw new BusinessException("用户未连接："+followName);
        }
        int clientId=(int)accountClientId;
        if (!ConnectLibrary.library.MT4API_IsConnect(clientId)) {
            log.info("connect borker false, clientId error!");
            throw new BusinessException("connect borker false, clientId error!");
        }

        OrderLibrary.TradeRecord.ByReference orderSend = new OrderLibrary.TradeRecord.ByReference();
        long time = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

        QuoteLibrary.QuoteEventInfo.ByReference quoteInfo = new QuoteLibrary.QuoteEventInfo.ByReference();

        //循环获取行情信息，直到获取到最新的行情信息
        while (!QuoteLibrary.library.MT4API_GetQuote(clientId, new String(tradeRecord.symbol), quoteInfo)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                e.printStackTrace();
            }
        }

        if (QuoteLibrary.library.MT4API_GetQuote(clientId, new String(tradeRecord.symbol), quoteInfo)) {
            if(TraderLibrary.library.MT4API_IsTradeAllowed(clientId)){
                double volume=tradeRecord.volume*0.01;
                log.info("行情最新报价：" + quoteInfo.fAsk);
                log.info("跟单数据："
                        + "clientId : " + clientId + ","
                        + "order : " + tradeRecord.order + ","
                        + "symbol : " + new String(tradeRecord.symbol).trim() + ","
                        + "volume : " + volume + ","
                        + "stoploss : " + tradeRecord.stoploss + ","
                        + "takeprofit : " + tradeRecord.takeprofit + ","
                        + "cmd : " + tradeRecord.cmd + ","
                        + "magic : " + tradeRecord.magic + ","
                        + "fAsk : " + quoteInfo.fAsk + ","
                        + "nDigits : " + quoteInfo.nDigits + ","
                        + "expiration : " + time + ","
                );

                String comment=followName+":"+tradeRecord.order;
                int magic=TradeUtil.getMagic(followName,tradeRecord.order);
                //调用重复交易
                boolean tradeResult=orderInfoService.orderTradeRetrySyn(clientId,tradeRecord,magic,comment,1,5);
                if(tradeResult){
                    //保存正在交易状态  TODO 要做失败监测，里面放订单
                    redisManager.hset(RedisConstant.H_ORDER_FOLLOW_TRADING,comment,orderSend.order);
                    log.info("跟单信息：success! isTrade");
                }
            }else {
                log.error("MT4API_IsTradeAllowed false!");
                TradeUtil.printError(clientId);
            }
        }
    }
}
