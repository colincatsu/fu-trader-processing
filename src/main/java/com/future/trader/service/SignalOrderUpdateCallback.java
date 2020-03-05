package com.future.trader.service;

import com.future.trader.api.TraderLibrary;
import com.sun.jna.Callback;

public interface SignalOrderUpdateCallback extends Callback {

    void onUpdate(TraderLibrary.OrderUpdateEventInfo orderUpdateEventInfo, int clientId);
}
