package com.future.trader.service;


import com.alibaba.fastjson.JSONObject;
import com.future.trader.api.TraderLibrary;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.util.TradeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TradeInfoService {


    /**
     * 获取交易信息
     * @param clientId
     */
    public void trader(int clientId) {
        boolean isTrader = TraderLibrary.library.MT4API_IsTradeAllowed(clientId);
        if (isTrader) {
            System.out.println("当前可进行交易操作：" + isTrader);
        } else {
            TradeUtil.printError(clientId);
        }
    }

    public static void main(String[] args) {
        /*TradeRecordTest b = new TradeRecordTest();
        b.login=123;
        b.open_price=234;
        System.out.println(b.login);

        String json= JSONObject.toJSONString(b);
        System.out.println(json);

        TradeRecordInfo info=new TradeRecordInfo();
        BeanUtils.copyProperties(b,info);
        info.setComment(new String(b.comment).trim());
        info.setSymbol(new String(b.symbol).trim());
        info.setConv_reserv(new String(b.conv_reserv).trim());;*//*

        System.out.println(info.getComment());

        json= JSONObject.toJSONString(info);
        System.out.println(json);*/
    }
}
