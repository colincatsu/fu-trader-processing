package com.future.trader.service.impl;


import com.future.trader.api.TraderLibrary;
import com.future.trader.service.OrderNotifyCallback;

public class OrderNotifyCallbackImpl implements OrderNotifyCallback {
    @Override
    public void onNotify(TraderLibrary.OrderNotifyEventInfo orderNotifyEventInfo, int clientId) {
        System.out.println("订单回调通知");
    }
}
