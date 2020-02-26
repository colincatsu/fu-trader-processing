package com.future.trader.service;


import com.future.trader.api.TraderLibrary;
import com.sun.jna.Callback;

public interface OrderNotifyCallback extends Callback {

    void onNotify(TraderLibrary.OrderNotifyEventInfo orderNotifyEventInfo, int clientId);
}
