package com.future.trader.service;


import com.sun.jna.Callback;

/**
 * 连接断开回调接收处理函数
 */
public interface DisConnectCallback extends Callback {

    void onDisConnect(int clientId);
}
