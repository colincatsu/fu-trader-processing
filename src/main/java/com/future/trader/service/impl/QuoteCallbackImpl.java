package com.future.trader.service.impl;


import com.future.trader.service.QuoteCallback;

/**
 * 行情回调接口实现累
 */
public class QuoteCallbackImpl implements QuoteCallback {

    @Override
    public void onQuoteEventHandler(int infoData, int infoNum) {
        System.out.println("quote callback start");

//        if (info == null || infoNum <= 0) return;

        System.out.println("infoData : " + infoData + ", infoNum : " + infoNum);

//        try {
//            for (int i = 0; i < infoNum; i++) {
//                System.out.println(new String(info[i].szSymbol, "UTF-8"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
