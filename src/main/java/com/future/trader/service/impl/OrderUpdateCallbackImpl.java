package com.future.trader.service.impl;

import com.alibaba.fastjson.JSON;
import com.future.trader.api.*;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.common.enums.OrderUpdateActionEnum;
import com.future.trader.service.OrderUpdateCallback;
import com.future.trader.util.TradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class OrderUpdateCallbackImpl implements OrderUpdateCallback {

    Logger log= LoggerFactory.getLogger(OrderUpdateCallbackImpl.class);

    @Override
    public void onUpdate(TraderLibrary.OrderUpdateEventInfo orderUpdateEventInfo, int clientId) {

        TradeRecordInfo info =TradeUtil.convertTradeRecords(orderUpdateEventInfo.tradeRecord);
        log.info("订单更新回调，clientId：" + clientId);
        log.info("orderUpdateAction : " + orderUpdateEventInfo.updateAction.ordinal());
        log.info("orderInfo : " + JSON.toJSONString(info));

        OrderLibrary.TradeRecord tradeRecord = orderUpdateEventInfo.tradeRecord;
        if (OrderUpdateActionEnum.OUA_PositionOpen.getIntValue() == orderUpdateEventInfo.updateAction.ordinal()) { //开仓
            followTrade(tradeRecord);
        }
    }

    /**
     * 订单跟随逻辑
     * @param tradeRecord
     */
    private void followTrade(OrderLibrary.TradeRecord tradeRecord) {

        /*String brokerName = "MultiBankFXInt-Demo F";
        int username = 2102282461;
        String password = "bcd8krv";*/
        String brokerName = "USGFX-Demo";
        int username = 1100551517;
        String password = "3bwndrx";

        int clientId = TradeUtil.getUserConnect(brokerName,username,password);
        if(clientId==0){
            log.error("follow Trade error! brokerName:"+brokerName+",username:"+username);
        }

        try {
            OrderLibrary.TradeRecord.ByReference orderSend = new OrderLibrary.TradeRecord.ByReference();
            long time = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

            QuoteLibrary.QuoteEventInfo.ByReference quoteInfo = new QuoteLibrary.QuoteEventInfo.ByReference();

            //循环获取行情信息，直到获取到最新的行情信息
            while (!QuoteLibrary.library.MT4API_GetQuote(clientId, new String(tradeRecord.symbol), quoteInfo)) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    e.printStackTrace();
                }
            }

            if (QuoteLibrary.library.MT4API_GetQuote(clientId, new String(tradeRecord.symbol), quoteInfo)) {
                log.info("行情最新报价：" + quoteInfo.fAsk);

                log.info("跟单数据："
                        + "clientId : " + clientId + ","
                        + "symbol : " + new String(tradeRecord.symbol).trim() + ","
                        + "volume : " + tradeRecord.volume + ","
                        + "stoploss : " + tradeRecord.stoploss + ","
                        + "takeprofit : " + tradeRecord.takeprofit + ","
                        + "cmd : " + tradeRecord.cmd + ","
                        + "magic : " + tradeRecord.magic + ","
                        + "fAsk : " + quoteInfo.fAsk + ","
                        + "nDigits : " + quoteInfo.nDigits + ","
                );
                boolean isTrade = TraderLibrary.library.MT4API_OrderSend(clientId, new String(tradeRecord.symbol).trim(),
                        (byte) tradeRecord.cmd, tradeRecord.volume,
                        quoteInfo.fAsk, quoteInfo.nDigits, tradeRecord.stoploss, tradeRecord.takeprofit,
                        "", tradeRecord.magic, (int) time + 24 * 60 * 60, orderSend);
                if (isTrade) {
                    log.info("跟单信息："
                            + "order : " + orderSend.order + ","
                            + "login : " + orderSend.login + ","
                            + "symbol : " + new String(orderSend.symbol) + ","
                            + "digits : " + orderSend.digits + ","
                            + "volume : " + orderSend.volume + ","
                            + "open_time : " + orderSend.open_time + ","
                            + "open_price : " + orderSend.open_price + ","
                            + "state : " + orderSend.state + ","
                            + "stoploss : " + orderSend.stoploss + ","
                            + "takeprofit : " + orderSend.takeprofit + ","
                    );
                } else {
                    TradeUtil.printError(clientId);
                }
            }
        } finally {
            InstanceLibrary.library.MT4API_Destory(clientId);
        }
    }

}
