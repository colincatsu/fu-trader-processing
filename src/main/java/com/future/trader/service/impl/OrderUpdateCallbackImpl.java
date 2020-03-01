package com.future.trader.service.impl;

import com.alibaba.fastjson.JSON;
import com.future.trader.api.ConnectLibrary;
import com.future.trader.api.OrderLibrary;
import com.future.trader.api.QuoteLibrary;
import com.future.trader.api.TraderLibrary;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.common.enums.OrderUpdateActionEnum;
import com.future.trader.common.exception.BusinessException;
import com.future.trader.service.OrderInfoService;
import com.future.trader.service.OrderUpdateCallback;
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
public class OrderUpdateCallbackImpl implements OrderUpdateCallback {

    Logger log= LoggerFactory.getLogger(OrderUpdateCallbackImpl.class);

    @Autowired
    RedisManager redisManager;
    @Resource
    OrderInfoService orderInfoService;


    @Override
    public void onUpdate(TraderLibrary.OrderUpdateEventInfo orderUpdateEventInfo, int clientId) {

        TradeRecordInfo info =TradeUtil.convertTradeRecords(orderUpdateEventInfo.tradeRecord);
        log.info("订单更新回调，clientId：" + clientId);
        log.info("orderUpdateAction : " + orderUpdateEventInfo.updateAction.ordinal());
        log.info("orderInfo : " + JSON.toJSONString(info));

        OrderLibrary.TradeRecord tradeRecord = orderUpdateEventInfo.tradeRecord;
        try {
            if (OrderUpdateActionEnum.OUA_PositionOpen.getIntValue() == orderUpdateEventInfo.updateAction.ordinal()) {
                //开仓
                followTradeOpen(tradeRecord);
            }else {
                // 平仓
                followTradeClose(tradeRecord);
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 订单跟随逻辑close
     * @param tradeRecord
     */
    private void followTradeClose(OrderLibrary.TradeRecord tradeRecord) {
        int username = 51243174;
        String password = "1tvkhzu";
        Object accountClientId=redisManager.hget("account-connect-clientId",String.valueOf(username));
        if(ObjectUtils.isEmpty(accountClientId)){
            log.error("用户未连接："+username);
            throw new BusinessException("用户未连接："+username);
        }
        int clientId=(int)accountClientId;
        if (!ConnectLibrary.library.MT4API_IsConnect(clientId)) {
            log.info("connect borker false, clientId error!");
            throw new BusinessException("connect borker false, clientId error!");
        }

        String redisKey=String.valueOf(username)+tradeRecord.order;
        Object followOrder=redisManager.hget("account-follow-order",redisKey);
        if(ObjectUtils.isEmpty(followOrder)){
            log.error("用户："+username+",未跟随该订单 order："+tradeRecord.order);
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
                log.info("跟单信息：isSend:"+isSend);
                redisManager.hdel("account-follow-order",redisKey);
            } else {
                TradeUtil.printError(clientId);
            }
            /*OrderLibrary.TradeRecord.ByReference orderSend = new OrderLibrary.TradeRecord.ByReference();
            boolean isTrade= TraderLibrary.library.MT4API_OrderClose(clientId,new String(tradeRecord.symbol).trim(),followOrderId,
                    volume,quoteInfo.fAsk,quoteInfo.nDigits,orderSend);
            if (isTrade) {
                log.info("跟单信息：close success! isSend,followOrderid:"+followOrderId);
                log.info("跟单信息：isSend:"+isTrade);
                redisManager.hdel("account-follow-order",redisKey);
            } else {
                TradeUtil.printError(clientId);
            }*/
        }
    }

    /**
     * 订单跟随逻辑open
     * @param tradeRecord
     */
    private void followTradeOpen(OrderLibrary.TradeRecord tradeRecord) {

        /*String brokerName = "MultiBankFXInt-Demo F";
        int username = 2102282461;
        String password = "bcd8krv";*/
        String brokerName = "FXCM-USDDemo02";
        int username = 51243174;
        String password = "1tvkhzu";

        /*int clientId = TradeUtil.getUserConnect(brokerName,username,password);
        if(clientId==0){
            log.error("follow Trade error! brokerName:"+brokerName+",username:"+username);
        }*/
        Object accountClientId=redisManager.hget("account-connect-clientId",String.valueOf(username));
        if(ObjectUtils.isEmpty(accountClientId)){
            log.error("用户未连接："+username);
            throw new BusinessException("用户未连接："+username);
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
                int isSend= TraderLibrary.library.MT4API_OrderSendAsync(clientId, new String(tradeRecord.symbol).trim(),
                        (byte) tradeRecord.cmd, volume,
                        quoteInfo.fAsk, quoteInfo.nDigits, tradeRecord.stoploss, tradeRecord.takeprofit,
                        "", 999, (int) time + 24 * 60 * 60);
                if (isSend>0) {
                    log.info("跟单信息：success! isSend");
                    log.info("跟单信息：isSend:"+isSend);
                    //查询最新订单
                    /*TradeRecordInfo lastOrder=orderInfoService.getLastFollowOpenOrder(clientId);
                    String redisKey=String.valueOf(username)+tradeRecord.order;
                    redisManager.hset("account-follow-order",redisKey,lastOrder.getOrder());*/
                } else {
                    TradeUtil.printError(clientId);
                }
                /*boolean isTrade = TraderLibrary.library.MT4API_OrderSend(clientId, new String(tradeRecord.symbol).trim(),
                        (byte) tradeRecord.cmd, volume,
                        quoteInfo.fAsk, quoteInfo.nDigits, tradeRecord.stoploss, tradeRecord.takeprofit,
                        "", tradeRecord.magic, (int) time + 24 * 60 * 60, orderSend);
                if (isTrade) {
                    String redisKey=String.valueOf(username)+tradeRecord.order;
                    redisManager.hset("account-follow-order",redisKey,orderSend.order);
                    log.info("跟单信息：success! isTrade");
                    log.info("跟单信息："
                            + "order : " + orderSend.order + ","
                            + "login : " + orderSend.login + ","
                            + "symbol : " + new String(orderSend.symbol) + ","
                            + "digits : " + orderSend.digits + ","
                            + "volume : " + orderSend.volume + ","
                            + "open_time : " + orderSend.open_time + ","
                            + "open_price : " + orderSend.open_price + ","
                            + "state : " + orderSend.state + ","
                            + "stoploss : " + orderSend.stoploss + ","
                            + "takeprofit : " + orderSend.takeprofit + ","
                    );
                } else {
                    TradeUtil.printError(clientId);
                }*/
            }else {
                log.error("MT4API_IsTradeAllowed false!");
                TradeUtil.printError(clientId);
            }
        }
    }
}
