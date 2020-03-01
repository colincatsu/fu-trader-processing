package com.future.trader.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.future.trader.api.TraderLibrary;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.service.OrderNotifyCallback;
import com.future.trader.util.TradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderNotifyCallbackImpl implements OrderNotifyCallback {

    Logger log= LoggerFactory.getLogger(OrderNotifyCallbackImpl.class);

    @Override
    public void onNotify(TraderLibrary.OrderNotifyEventInfo orderNotifyEventInfo, int clientId) {
        log.info("订单回调通知");
        log.info("订单回调通知 clientId:"+clientId);
        log.info("订单回调通知 type:"+orderNotifyEventInfo.emType.getIntValue());
        log.info("订单回调通知 status:"+orderNotifyEventInfo.status);
        TradeRecordInfo info = TradeUtil.convertTradeRecords(orderNotifyEventInfo.tradeRecord);
        log.info("订单回调通知 order:"+ JSONObject.toJSONString(info));
    }
}
