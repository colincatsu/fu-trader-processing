package com.future.trader.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.future.trader.api.OrderLibrary;
import com.future.trader.api.TraderLibrary;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.common.constants.OrderConstant;
import com.future.trader.common.constants.RedisConstant;
import com.future.trader.common.enums.OrderUpdateActionEnum;
import com.future.trader.common.enums.TradeErrorEnum;
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
import java.util.Date;

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

        JSONObject followJson=(JSONObject)object;
        JSONObject dataJson=new JSONObject();
        OrderLibrary.TradeRecord tradeRecord = orderUpdateEventInfo.tradeRecord;
        for(String jsonKey:followJson.keySet()){
            dataJson.clear();
            dataJson.put("order",JSON.parseObject(JSON.toJSONString(orderUpdateEventInfo)));
            dataJson.put("followRule",followJson.getJSONObject(jsonKey));
            int followName= Integer.parseInt(jsonKey);
            String comment=TradeUtil.getComment(followName,tradeRecord.login,tradeRecord.order);
            //TODO 账号跟随规则校验

            try {
                if (OrderUpdateActionEnum.OUA_PositionOpen.getIntValue() == orderUpdateEventInfo.updateAction.ordinal()) {
                    log.info("信号源订单开仓,跟随账号："+followName);
                    // 跟单逻辑
                    OrderLibrary.TradeRecord followRecord=orderInfoService.followOpenLogic(tradeRecord,followJson.getJSONObject(jsonKey));
                    //开仓
                    int result= followTradeOpen(followRecord,followName);
                    if(result>TradeErrorEnum.SUCCESS.code()){
                        //异常订单入库
                        log.error("信号源订单开仓 处理失败,跟随账号："+followName);
                        dataJson.put("errorCode",result);
                        redisManager.lSet(RedisConstant.L_ORDER_FOLLOW_ERROR_DATA,dataJson);
                    }
                    //保存正在交易状态
                    redisManager.hset(RedisConstant.H_ORDER_FOLLOW_TRADING,comment,new Date().getTime());
                    //保存正在交易数据
                    redisManager.hset(RedisConstant.H_ORDER_FOLLOW_TRADING_DATA,comment,dataJson.toJSONString());
                }else if (OrderUpdateActionEnum.OUA_PositionClose.getIntValue() == orderUpdateEventInfo.updateAction.ordinal()) {
                    /*判断跟随关系*/
                    Object followOrder=redisManager.hget(RedisConstant.H_ORDER_FOLLOW_ORDER_RELATION,comment);
                    if(ObjectUtils.isEmpty(followOrder)){
                        log.error("用户："+followName+",未跟随该订单 order："+tradeRecord.order);
                        return;
                    }
                    log.info("信号源订单平仓,跟随账号："+followName);
                    // 平仓
                    int result=followTradeClose(tradeRecord,followName,(int)followOrder);
                    if(result>TradeErrorEnum.SUCCESS.code()){
                        //异常订单入库
                        log.error("信号源订单平仓 处理失败,跟随账号："+followName);
                        dataJson.put("errorCode",result);
                        redisManager.lSet(RedisConstant.L_ORDER_FOLLOW_ERROR_DATA,dataJson);
                    }
                    //保存正在关闭状态
                    redisManager.hset(RedisConstant.H_ORDER_FOLLOW_CLOSING,comment,new Date().getTime());
                    //保存正在关闭数据
                    redisManager.hset(RedisConstant.H_ORDER_FOLLOW_CLOSING_DATA,comment,dataJson.toJSONString());
                }else {
                    log.info("信号源订单其他交易,跟随账号："+followName);
                    continue;
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
                //异常订单入库
                dataJson.put("errorCode",TradeErrorEnum.FAILURE.code());
                redisManager.lSet(RedisConstant.L_ORDER_FOLLOW_ERROR_DATA,dataJson);
            }
        }
    }

    /**
     * 订单跟随逻辑close
     * @param tradeRecord
     * @param followName
     * @param followOrderId
     * @return 返回错误码
     */
    private int followTradeClose(OrderLibrary.TradeRecord tradeRecord,int followName,int followOrderId) {
        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(followName));
        if(ObjectUtils.isEmpty(accountClientId)||(int)accountClientId==0){
            log.error("用户未连接："+followName);
            return TradeErrorEnum.ACC_DIS_CONNECT.code();
        }
        int clientId=(int)accountClientId;

        /*异步关闭订单*/
        int sendCode= orderInfoService.sendOrderCloseAsync(clientId,followOrderId,new String(tradeRecord.symbol).trim(),tradeRecord.volume);
        if (sendCode==TradeErrorEnum.SUCCESS.code()) {
            log.info("跟单信息：close success! isSend,followOrderid:"+followOrderId);
        } else {
            log.info("跟单信息：close fail! followOrderid:"+followOrderId);
        }
        return sendCode;
    }

    /**
     * 订单跟随逻辑open (返回错误码 0为正常)
     * @param tradeRecord
     */
    private int followTradeOpen(OrderLibrary.TradeRecord tradeRecord,int followName) {

        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(followName));
        if(ObjectUtils.isEmpty(accountClientId)||(int)accountClientId==0){
            log.error("用户未连接："+followName);
            return TradeErrorEnum.ACC_DIS_CONNECT.code();
        }
        int clientId=(int)accountClientId;

        if(TraderLibrary.library.MT4API_IsTradeAllowed(clientId)){
            String comment=TradeUtil.getComment(followName,tradeRecord.login,tradeRecord.order);
            int magic=TradeUtil.getMagic(followName,tradeRecord.login,tradeRecord.order);
            //调用重复交易
            int tradeResult=orderInfoService.orderTradeRetrySyn(clientId,tradeRecord,magic,comment,1,5);
            if(tradeResult==TradeErrorEnum.SUCCESS.code()){
                log.info("跟单信息：success! isTrade");
            }else {
                log.info("跟单信息：failure! isTrade");
            }
            return tradeResult;
        }else {
            log.error("MT4API_IsTradeAllowed false!");
            TradeUtil.printError(clientId);
            return TradeErrorEnum.TRADE_NOT_ALLOWED.code();
        }
    }
}
