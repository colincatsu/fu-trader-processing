package com.future.trader.service;


import com.sun.jna.Callback;

/**
 * 行情回调接口
 */
public interface QuoteCallback extends Callback {

    void onQuoteEventHandler(int infoData, int infoNum);
}
