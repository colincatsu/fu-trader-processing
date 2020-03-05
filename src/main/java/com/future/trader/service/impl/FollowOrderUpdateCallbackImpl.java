package com.future.trader.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.future.trader.api.TraderLibrary;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.common.constants.OrderConstant;
import com.future.trader.common.constants.RedisConstant;
import com.future.trader.common.enums.OrderUpdateActionEnum;
import com.future.trader.service.FollowOrderUpdateCallback;
import com.future.trader.util.RedisManager;
import com.future.trader.util.TradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FollowOrderUpdateCallbackImpl implements FollowOrderUpdateCallback {

    Logger log= LoggerFactory.getLogger(FollowOrderUpdateCallbackImpl.class);

    @Autowired
    RedisManager redisManager;

    @Override
    public void onUpdate(TraderLibrary.OrderUpdateEventInfo orderUpdateEventInfo, int clientId) {

        log.info("跟随订单更新回调，clientId：" + clientId);
        log.info("orderUpdateAction : " + orderUpdateEventInfo.updateAction.ordinal());
        TradeRecordInfo info =TradeUtil.convertTradeRecords(orderUpdateEventInfo.tradeRecord);
        log.info("orderInfo : " + JSON.toJSONString(info));

        if(orderUpdateEventInfo.tradeRecord.magic== OrderConstant.ORDER_FOLLOW_MAGIC){
            if(StringUtils.isEmpty(info.getComment())){
                log.error("跟单信息错误！ 缺少跟单关系");
            }
            if (OrderUpdateActionEnum.OUA_PositionOpen.getIntValue() == orderUpdateEventInfo.updateAction.ordinal()) {
                log.info("跟随订单开仓,跟随账号："+info.getLogin());
                //开仓
                followCallbackOpen(info);
                log.info("跟随订单开仓,跟随订单号："+info.getOrder());
            }else if (OrderUpdateActionEnum.OUA_PositionClose.getIntValue() == orderUpdateEventInfo.updateAction.ordinal()) {
                log.info("跟随订单平仓,跟随账号："+info.getLogin());
                // 平仓
                followCallbackClose(info);
                log.info("跟随订单平仓,跟随订单号："+info.getOrder());
            }else {
                log.info("跟随订单其他交易,跟随账号："+info.getLogin());
                log.info("跟随订单其他交易,跟随订单号："+info.getOrder());
            }
        }else {
            log.info("user trade order:"+orderUpdateEventInfo.tradeRecord.order);
            return;
        }
    }

    /**
     * 跟随订单变更 开仓逻辑
     * @param info
     */
    public void followCallbackOpen(TradeRecordInfo info){
        if(StringUtils.isEmpty(info.getComment())){
            log.error("跟单信息错误！ 缺少跟单关系");
        }
        String[] followInfo=info.getComment().split(":");
        JSONObject orderJson=new JSONObject();
        orderJson.put("followOrder",JSON.toJSONString(info));
        orderJson.put("followName",followInfo[0]);
        orderJson.put("signalOrderId",followInfo[1]);

        /*将跟单关系保存在redis*/
        redisManager.hset(RedisConstant.H_ORDER_FOLLOW_ORDER_RELATION,info.getComment(),info.getOrder());

        /*将跟随订单保存至redis*/
        redisManager.lSet(RedisConstant.L_ORDER_FOLLOW_ORDERS,orderJson);

        /*删除redis中正在交易的订单*/
        redisManager.hdel(RedisConstant.H_ORDER_FOLLOW_TRADING,info.getComment());
    }

    /**
     * 跟随订单变更 平仓逻辑
     * @param info
     */
    public void followCallbackClose(TradeRecordInfo info){

        if(StringUtils.isEmpty(info.getComment())){
            log.error("跟单信息错误！ 缺少跟单关系");
        }
        String[] followInfo=info.getComment().split(":");
        JSONObject orderJson=new JSONObject();
        orderJson.put("followOrder",JSON.toJSONString(info));
        orderJson.put("followName",followInfo[0]);
        orderJson.put("signalOrderId",followInfo[1]);

        /*删除跟单关系*/
        redisManager.hdel(RedisConstant.H_ORDER_FOLLOW_ORDER_RELATION,info.getComment());

        /*将跟随订单保存至redis*/
        redisManager.lSet(RedisConstant.L_ORDER_FOLLOW_ORDERS,orderJson);

        /*删除redis中正在平仓的订单*/
        redisManager.hdel(RedisConstant.H_ORDER_FOLLOW_CLOSING,info.getComment());
    }
}
