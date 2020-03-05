package com.future.trader.service.impl;


import com.alibaba.fastjson.JSON;
import com.future.trader.api.TraderLibrary;
import com.future.trader.service.OrderNotifyCallback;
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
        log.info("订单回调通知 status:"+orderNotifyEventInfo.nStatus);
        log.info("订单回调通知 info:"+ JSON.toJSONString(orderNotifyEventInfo));
        /*TradeRecordInfo info = TradeUtil.convertTradeRecords(orderNotifyEventInfo.notifyRecord);
        log.info("订单回调通知 order:"+ JSON.toJSONString(info));
        log.info("订单回调通知 info:"+ JSON.toJSONString(orderNotifyEventInfo));*/
    }
}
