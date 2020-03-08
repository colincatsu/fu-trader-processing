package com.future.trader.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.future.trader.api.*;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.common.constants.RedisConstant;
import com.future.trader.common.exception.BusinessException;
import com.future.trader.common.exception.DataConflictException;
import com.future.trader.common.helper.PageInfoHelper;
import com.future.trader.util.RedisManager;
import com.future.trader.util.StringUtils;
import com.future.trader.util.TradeUtil;
import com.github.pagehelper.PageInfo;
import com.sun.jna.ptr.IntByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OrderInfoService {

    Logger log= LoggerFactory.getLogger(OrderInfoService.class);

    @Autowired
    RedisManager redisManager;
    @Resource
    ConnectionService connectionService;

    /**
     * 根据条件查询用户历史订单
     * @param conditionMap
     * @return
     */
    public List<TradeRecordInfo> getUserCloseOrders(Map conditionMap){

        if(conditionMap==null){
            log.error("null input message!");
        }else {
            log.info(JSON.toJSONString(conditionMap));
        }
        if(conditionMap.get("serverName")==null
                ||conditionMap.get("username")==null||conditionMap.get("password")==null){
            log.error("null input message!");
            throw new DataConflictException("null input message!");
        }
        if(conditionMap.get("nHisTimeFrom")==null
                ||conditionMap.get("nHisTimeTo")==null){
            log.error("null input message!");
            throw new DataConflictException("null input message!");
        }
        String serverName = String.valueOf(conditionMap.get("serverName"));
        int username = Integer.parseInt(String.valueOf(conditionMap.get("username")));
        String password = String.valueOf(conditionMap.get("password"));
        int nThreadHisTimeFrom = Integer.parseInt(String.valueOf(conditionMap.get("nHisTimeFrom")));
        int nThreadHisTimeTo = Integer.parseInt(String.valueOf(conditionMap.get("nHisTimeTo")));

        //TODO 时间段不能超过1周

        int clientId = connectionService.getUserConnect(serverName,username,password,nThreadHisTimeFrom,nThreadHisTimeTo);
        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        List<TradeRecordInfo> list=obtainCloseOrderInfo(clientId);
        connectionService.disConnect(clientId);

        return list;
    }

    /**
     * 根据条件查询用户历史订单
     * @param conditionMap
     * @return
     */
    public TradeRecordInfo getUserCloseOrderById(Map conditionMap){

        if(conditionMap==null){
            log.error("null input message!");
        }else {
            log.info(JSON.toJSONString(conditionMap));
        }
        if(conditionMap.get("serverName")==null
                ||conditionMap.get("username")==null||conditionMap.get("password")==null){
            log.error("null input message!");
            throw new DataConflictException("null input message!");
        }
        if(conditionMap.get("orderId")==null){
            log.error("null input message!");
            throw new DataConflictException("null input message!");
        }
        String serverName = String.valueOf(conditionMap.get("serverName"));
        int username = Integer.parseInt(String.valueOf(conditionMap.get("username")));
        String password = String.valueOf(conditionMap.get("password"));
        int orderId = Integer.parseInt(String.valueOf(conditionMap.get("orderId")));

        int clientId=0;
        Object oClientId = redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
        if(ObjectUtils.isEmpty(oClientId)){
            clientId=connectionService.getUserConnect(serverName,username,password);
        }else {
            clientId=(int)oClientId;
        }
        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        TradeRecordInfo tradeRecordInfo=obtainCloseOrderInfo(clientId,orderId);
        connectionService.disConnect(clientId);

        return tradeRecordInfo;
    }

    /**
     * 根据条件查询用户在仓订单
     * @param conditionMap
     * @return
     */
    public List<TradeRecordInfo> getUserOpenOrders(Map conditionMap){

        if(conditionMap==null){
            log.error("null input message!");
        }else {
            log.info(JSON.toJSONString(conditionMap));
        }
        if(conditionMap.get("serverName")==null
                ||conditionMap.get("username")==null||conditionMap.get("password")==null){
            log.error("null input message!");
            throw new DataConflictException("null input message!");
        }
        String serverName = String.valueOf(conditionMap.get("serverName"));
        int username = Integer.parseInt(String.valueOf(conditionMap.get("username")));
        String password = String.valueOf(conditionMap.get("password"));
        int nThreadHisTimeFrom =0;
        int nThreadHisTimeTo =0;
        if(conditionMap.get("nHisTimeFrom")!=null){
            nThreadHisTimeFrom = Integer.parseInt(String.valueOf(conditionMap.get("nHisTimeFrom")));
        }
        if(conditionMap.get("nHisTimeTo")!=null){
            nThreadHisTimeTo = Integer.parseInt(String.valueOf(conditionMap.get("nHisTimeTo")));
        }

        int clientId = connectionService.getUserConnect(serverName,username,password,nThreadHisTimeFrom,nThreadHisTimeTo);
        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        List<TradeRecordInfo> list=obtainOpenOrderInfo(clientId);
        connectionService.disConnect(clientId);

        return list;
    }

    /**
     * 根据条件查询用户在仓订单
     * @param conditionMap
     * @return
     */
    public TradeRecordInfo getUserOpenOrder(Map conditionMap){

        if(conditionMap==null){
            log.error("null input message!");
        }else {
            log.info(JSON.toJSONString(conditionMap));
        }
        if(conditionMap.get("serverName")==null
                ||conditionMap.get("username")==null||conditionMap.get("password")==null){
            log.error("null input message!");
            throw new DataConflictException("null input message!");
        }
        if(conditionMap.get("orderId")==null){
            log.error("null input message!");
            throw new DataConflictException("null input message!");
        }
        String serverName = String.valueOf(conditionMap.get("serverName"));
        int username = Integer.parseInt(String.valueOf(conditionMap.get("username")));
        String password = String.valueOf(conditionMap.get("password"));
        int orderId = Integer.parseInt(String.valueOf(conditionMap.get("orderId")));

        int clientId=0;
        Object oClientId = redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
        if(ObjectUtils.isEmpty(oClientId)){
            clientId=connectionService.getUserConnect(serverName,username,password);
        }else {
            clientId=(int)oClientId;
        }
        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        TradeRecordInfo info=obtainOpenOrderInfo(clientId,orderId);
        connectionService.disConnect(clientId);

        return info;
    }

    /**
     * 获取历史订单信息
     * @param clientId
     */
    public List<TradeRecordInfo> obtainCloseOrderInfo(int clientId) {
        IntByReference closeOrderCount = new IntByReference();
        OrderLibrary.library.MT4API_GetCloseOrdersCount(clientId, closeOrderCount);
        int closeCountInt = closeOrderCount.getValue();
        log.info("已关闭订单个数：" + closeCountInt);

        if(closeCountInt==0){
            return null;
        }
        closeCountInt=1;

        List<TradeRecordInfo> recordInfoList=new ArrayList<>();
        IntByReference closeCount = new IntByReference(closeCountInt);
        OrderLibrary.TradeRecord b = new OrderLibrary.TradeRecord();
        OrderLibrary.TradeRecord[] closeReference = (OrderLibrary.TradeRecord[]) b.toArray(closeCountInt);
        boolean closeSuccess = OrderLibrary.library.MT4API_GetCloseOrders(clientId, closeReference, closeCount);
        if (closeSuccess) {
            log.info("获取已关闭交易的订单列表：" + closeSuccess + "，已关闭订单个数：" + closeCountInt);

            for (int i = 0; i < closeCountInt; i++) {
                OrderLibrary.TradeRecord closetradeRecord = closeReference[i];
                log.info("订单信息："
                        + "order : " + closetradeRecord.order + ","
                        + "login : " + closetradeRecord.login + ","
                        + "symbol : " + new String(closetradeRecord.symbol) + ","
                        + "digits : " + closetradeRecord.digits + ","
                        + "volume : " + closetradeRecord.volume + ","
                        + "open_time : " + closetradeRecord.open_time + ","
                        + "open_price : " + closetradeRecord.open_price + ","
                        + "state : " + closetradeRecord.state + ","
                        + "stoploss : " + closetradeRecord.stoploss + ","
                        + "takeprofit : " + closetradeRecord.takeprofit + ","
                );
                TradeRecordInfo info =TradeUtil.convertTradeRecords(closetradeRecord);
                recordInfoList.add(info);
            }
        } else {
            TradeUtil.printError(clientId);
        }
        return recordInfoList;
    }

    /**
     * 根据orderId获取关闭订单
     * @param clientId
     * @param orderId
     * @return
     */
    public TradeRecordInfo obtainCloseOrderInfo(int clientId,int orderId) {
        if(orderId==0){
            return null;
        }
        OrderLibrary.TradeRecord.ByReference tradeRecord = new OrderLibrary.TradeRecord.ByReference();
        OrderLibrary.library.MT4API_GetCloseOrder(clientId, orderId, tradeRecord);
        System.out.println("订单信息："
                + "order : " + tradeRecord.order + ","
                + "login : " + tradeRecord.login + ","
                + "symbol : " + new String(tradeRecord.symbol) + ","
                + "digits : " + tradeRecord.digits + ","
                + "volume : " + tradeRecord.volume + ","
                + "open_time : " + tradeRecord.open_time + ","
                + "open_price : " + tradeRecord.open_price + ","
                + "state : " + tradeRecord.state + ","
                + "stoploss : " + tradeRecord.stoploss + ","
                + "takeprofit : " + tradeRecord.takeprofit + ","
                + "magic : " + tradeRecord.magic + ","
                + "cmd : " + tradeRecord.cmd + ","
        );
        return TradeUtil.convertTradeRecords(tradeRecord);
    }

    /**
     * 根据orderId获取打开订单
     * @param clientId
     * @param orderId
     * @return
     */
    public TradeRecordInfo obtainOpenOrderInfo(int clientId,int orderId) {
        if(orderId==0){
            return null;
        }
        OrderLibrary.TradeRecord.ByReference tradeRecord = new OrderLibrary.TradeRecord.ByReference();
        OrderLibrary.library.MT4API_GetOpenOrder(clientId, orderId, tradeRecord);
        System.out.println("订单信息："
                + "order : " + tradeRecord.order + ","
                + "login : " + tradeRecord.login + ","
                + "symbol : " + new String(tradeRecord.symbol) + ","
                + "digits : " + tradeRecord.digits + ","
                + "volume : " + tradeRecord.volume + ","
                + "open_time : " + tradeRecord.open_time + ","
                + "open_price : " + tradeRecord.open_price + ","
                + "state : " + tradeRecord.state + ","
                + "stoploss : " + tradeRecord.stoploss + ","
                + "takeprofit : " + tradeRecord.takeprofit + ","
                + "magic : " + tradeRecord.magic + ","
                + "cmd : " + tradeRecord.cmd + ","
        );
        return TradeUtil.convertTradeRecords(tradeRecord);
    }


    /**
     * 获取在仓订单信息
     * @param clientId
     */
    public List<TradeRecordInfo> obtainOpenOrderInfo(int clientId) {
        IntByReference openOrderCount = new IntByReference();
        OrderLibrary.library.MT4API_GetOpenOrdersCount(clientId, openOrderCount);
        int intCount = openOrderCount.getValue();
        System.out.println("正在交易的订单个数：" + intCount);
        if(intCount==0){
            return null;
        }

        List<TradeRecordInfo> recordInfoList=new ArrayList<>();
        IntByReference count = new IntByReference(intCount);
        OrderLibrary.TradeRecord a = new OrderLibrary.TradeRecord();
        OrderLibrary.TradeRecord[] reference = (OrderLibrary.TradeRecord[]) a.toArray(intCount);
        boolean isSuccess = OrderLibrary.library.MT4API_GetOpenOrders(clientId, reference, count);
        if (isSuccess) {
            log.info("获取交易中的订单列表：" + isSuccess + "，订单个数：" + intCount);
            for (int i = 0; i < intCount; i++) {
                OrderLibrary.TradeRecord opentTradeRecord = reference[i];

                log.info("交易中订单信息："
                        + "order : " + opentTradeRecord.order + ","
                        + "login : " + opentTradeRecord.login + ","
                        + "symbol : " + new String(opentTradeRecord.symbol) + ","
                        + "digits : " + opentTradeRecord.digits + ","
                        + "volume : " + opentTradeRecord.volume + ","
                        + "open_time : " + opentTradeRecord.open_time + ","
                        + "open_price : " + opentTradeRecord.open_price + ","
                        + "state : " + opentTradeRecord.state + ","
                        + "stoploss : " + opentTradeRecord.stoploss + ","
                        + "takeprofit : " + opentTradeRecord.takeprofit + ","
                );
                TradeRecordInfo info =TradeUtil.convertTradeRecords(opentTradeRecord);
                recordInfoList.add(info);
            }
        } else {
            TradeUtil.printError(clientId);
        }

        return recordInfoList;
    }

    /**
     * 订单交易(尝试知道达到次数或者成功)
     * @param clientId
     * @param tradeRecord
     * @param magic
     * @param comment
     * @param current
     * @param times
     * @return
     */
    public boolean orderTradeRetrySyn(int clientId,OrderLibrary.TradeRecord tradeRecord,int magic,String comment,int current, int times){

        OrderLibrary.TradeRecord.ByReference orderSend = new OrderLibrary.TradeRecord.ByReference();
        QuoteLibrary.QuoteEventInfo.ByReference quoteInfo = new QuoteLibrary.QuoteEventInfo.ByReference();

        //循环获取行情信息，直到获取到最新的行情信息
        while (!QuoteLibrary.library.MT4API_GetQuote(clientId, new String(tradeRecord.symbol), quoteInfo)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                return false;
            }
        }

        double volume=tradeRecord.volume*0.01;
        /*int ieDeviation =*/
        long time = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

        /*boolean isTrade = TraderLibrary.library.MT4API_OrderSend(clientId, new String(tradeRecord.symbol).trim(),
                (byte) tradeRecord.cmd, volume,
                quoteInfo.fAsk, 40, tradeRecord.stoploss, tradeRecord.takeprofit,
                ""+tradeRecord.login, tradeRecord.order, (int) time + 24 * 60 * 60, orderSend)*/;
        //异步提交
        int isTrade = TraderLibrary.library.MT4API_OrderSendAsync(clientId, new String(tradeRecord.symbol).trim(),
                (byte) tradeRecord.cmd, volume,
                quoteInfo.fAsk, 40, tradeRecord.stoploss, tradeRecord.takeprofit,
                comment, magic, (int) time + 24 * 60 * 60);

        log.info("交易信息：isTrade:"+isTrade);
        if (isTrade>0) {
            log.info("交易信息：success! isTrade,times:"+current);
            return true;
        } else {
            TradeUtil.printError(clientId);
            log.info("跟单信息：fail! times:"+current);
            if(current>=times){
                //达到次数
                log.error("跟单信息：fail finally! times:"+current);
                return false;
            }
            /*一直请求 知道成功或者达到次数*/
            return orderTradeRetrySyn(clientId,tradeRecord,magic,comment,current+1,times);
        }
    }

    /**
     * 获取证券信息
     * @param clientId
     */
    public JSONObject obtainSymbolInfo(int clientId, String symbolName) {
        IntByReference symbolCount = new IntByReference();
        SymbolInfoLibrary.SymbolInfo.ByReference symbolInfo = new SymbolInfoLibrary.SymbolInfo.ByReference();

        try {
            SymbolInfoLibrary.library.MT4API_GetSymbolsCount(clientId, symbolCount);
            log.info("服务端支持的证劵个数：" + symbolCount.getValue());
            /*String symbolName = "EURUSD";*/
            boolean getInfo= SymbolInfoLibrary.library.MT4API_GetSymbolInfo(clientId, symbolName, symbolInfo);
            log.info("获取证券信息 "+symbolName+":"+getInfo);
            if(!getInfo){
                TradeUtil.printError(clientId);
            }
            log.info("证劵信息："
                    + "symbol : " + new String(symbolInfo.symbol) + ","
                    + "description : " + new String(symbolInfo.description) + ","
                    + "source : " + new String(symbolInfo.source) + ","
                    + "currency : " + new String(symbolInfo.currency) + ","
                    + "type : " + symbolInfo.type + ","
                    + "digits : " + symbolInfo.digits + ","
                    + "spread : " + symbolInfo.spread + ","
                    + "spread_balance : " + symbolInfo.spread_balance + ","
                    + "trade : " + symbolInfo.trade);
            log.info(JSON.toJSONString(symbolInfo));
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }

        return JSON.parseObject(JSONObject.toJSONString(symbolInfo));
    }

    /**
     * 订单交易随逻辑
     * @param clientId
     * @param tradeRecord
     */
    public boolean orderTrade(int clientId, OrderLibrary.TradeRecord tradeRecord) {

        if (!ConnectLibrary.library.MT4API_IsConnect(clientId)) {
            log.info("connect borker false, clientId error!");
            throw new BusinessException("connect borker false, clientId error!");
        }

        boolean isTrade=true;
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
            isTrade = TraderLibrary.library.MT4API_OrderSend(clientId, new String(tradeRecord.symbol).trim(),
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
        return isTrade;
    }

    /**
     * 关闭订单 异步
     * @param clientId
     * @param orderId
     * @param symbol
     * @param volume
     * @return
     */
    public boolean sendOrderCloseAsync(int clientId,int orderId,String symbol,double volume){

        if(clientId==0||orderId==0||volume==0|| StringUtils.isEmpty(symbol)){
            log.info("关闭订单 异步, 参数为空!");
            throw new BusinessException("关闭订单 异步, 参数为空!");
        }

        if (!ConnectLibrary.library.MT4API_IsConnect(clientId)) {
            log.info("connect borker false, clientId error!");
            throw new BusinessException("connect borker false, clientId error!");
        }
        QuoteLibrary.QuoteEventInfo.ByReference quoteInfo = new QuoteLibrary.QuoteEventInfo.ByReference();
        //循环获取行情信息，直到获取到最新的行情信息
        while (!QuoteLibrary.library.MT4API_GetQuote(clientId, symbol, quoteInfo)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new BusinessException(e);
            }
        }

        if(TraderLibrary.library.MT4API_IsTradeAllowed(clientId)){
            int isSend= TraderLibrary.library.MT4API_OrderCloseAsync(clientId,symbol,orderId,
                    volume,quoteInfo.fAsk,40);
            if (isSend>0) {
                log.info("order close success! isSend,orderid:"+orderId);
            } else {
                TradeUtil.printError(clientId);
                throw new BusinessException("order close fail!");
            }
        }
        return true;
    }


    /**
     * 关闭订单
     * @param clientId
     * @param orderId
     * @param symbol
     * @param volume
     * @return
     */
    public boolean sendOrderClose(int clientId,int orderId,String symbol,double volume){

        if(clientId==0||orderId==0||volume==0|| StringUtils.isEmpty(symbol)){
            log.info("关闭订单 异步, 参数为空!");
            throw new BusinessException("关闭订单 异步, 参数为空!");
        }

        if (!ConnectLibrary.library.MT4API_IsConnect(clientId)) {
            log.info("connect borker false, clientId error!");
            throw new BusinessException("connect borker false, clientId error!");
        }
        QuoteLibrary.QuoteEventInfo.ByReference quoteInfo = new QuoteLibrary.QuoteEventInfo.ByReference();
        //循环获取行情信息，直到获取到最新的行情信息
        while (!QuoteLibrary.library.MT4API_GetQuote(clientId, symbol, quoteInfo)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new BusinessException(e);
            }
        }
        OrderLibrary.TradeRecord.ByReference orderSend = new OrderLibrary.TradeRecord.ByReference();
        if(TraderLibrary.library.MT4API_IsTradeAllowed(clientId)){
            boolean isSend= TraderLibrary.library.MT4API_OrderClose(clientId,symbol,orderId,
                    volume,quoteInfo.fAsk,40,orderSend);
            if (isSend) {
                log.info("order close success! isSend,orderid:"+orderId);
            } else {
                TradeUtil.printError(clientId);
                throw new BusinessException("order close fail!");
            }
        }
        return true;
    }
}
