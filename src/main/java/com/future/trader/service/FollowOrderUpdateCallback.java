package com.future.trader.service;

import com.future.trader.api.TraderLibrary;
import com.sun.jna.Callback;

/**
 * 跟随订单更新回调
 */
public interface FollowOrderUpdateCallback extends Callback {

    void onUpdate(TraderLibrary.OrderUpdateEventInfo orderUpdateEventInfo, int clientId);
}
