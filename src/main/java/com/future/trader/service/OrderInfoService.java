package com.future.trader.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.future.trader.api.*;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.common.constants.OrderConstant;
import com.future.trader.common.constants.RedisConstant;
import com.future.trader.common.enums.OrderTypeEnum;
import com.future.trader.common.enums.TradeErrorEnum;
import com.future.trader.common.exception.BusinessException;
import com.future.trader.common.exception.DataConflictException;
import com.future.trader.util.RedisManager;
import com.future.trader.util.StringUtils;
import com.future.trader.util.TradeUtil;
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
import java.util.Date;
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
            return null;
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
        /*默认查询近一个月的记录*/
        int nThreadHisTimeFrom =(int)((new Date().getTime()-((long)(7 * 24) * 3600000))/1000);
        int nThreadHisTimeTo =(int)(new Date().getTime()/1000);
        if(conditionMap.get("nHisTimeFrom")!=null){
            nThreadHisTimeFrom = Integer.parseInt(String.valueOf(conditionMap.get("nHisTimeFrom")));
        }
        if(conditionMap.get("nHisTimeTo")!=null){
            nThreadHisTimeTo = Integer.parseInt(String.valueOf(conditionMap.get("nHisTimeTo")));
        }
        if(nThreadHisTimeFrom>nThreadHisTimeTo){
            log.error("error input time!");
            return null;
        }
        int clientId=0;
        boolean isConnected=false;/*是否已经链接了*/

        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
        if(!ObjectUtils.isEmpty(accountClientId)&&(Integer)accountClientId>0){
            clientId=(Integer)accountClientId;
            if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
                /*已连接 直接返回*/
                isConnected=true;
            }else {
                /*未连接 删除数据 避免冗余*/
                redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
                redisManager.hdel(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
            }
        }
        if(!isConnected){
            clientId=connectionService.getUserConnect(serverName,username,password);
        }

        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        List<TradeRecordInfo> list=obtainHistoryOrderInfo(clientId,nThreadHisTimeFrom,nThreadHisTimeTo);

        if(!isConnected){
            connectionService.disConnect(clientId);
        }

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

        boolean isConnected=false;/*是否已经链接了*/
        int clientId=0;
        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
        if(!ObjectUtils.isEmpty(accountClientId)&&(Integer)accountClientId>0){
            clientId=(Integer)accountClientId;
            if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
                /*已连接 直接返回*/
                isConnected=true;
            }else {
                /*未连接 删除数据 避免冗余*/
                redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
                redisManager.hdel(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
            }
        }
        if(!isConnected){
            clientId=connectionService.getUserConnect(serverName,username,password);
        }
        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        TradeRecordInfo tradeRecordInfo=obtainCloseOrderInfo(clientId,orderId);
        if(!isConnected){
            /*因为此次查询做的链接 需要关闭*/
            connectionService.disConnect(clientId);
        }

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

        int clientId=0;
        boolean isConnected=false;/*是否已经链接了*/
        /*默认查询近一个月的记录*/
        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
        if(!ObjectUtils.isEmpty(accountClientId)&&(Integer)accountClientId>0){
            clientId=(Integer)accountClientId;
            if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
                /*已连接 直接返回*/
                isConnected=true;
            }else {
                /*未连接 删除数据 避免冗余*/
                redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
                redisManager.hdel(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
            }
        }
        if(!isConnected){
            clientId=connectionService.getUserConnect(serverName,username,password);
        }
        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        List<TradeRecordInfo> list=obtainOpenOrderInfo(clientId);

        if(!isConnected){
            /*新打开的链接需要关闭*/
            connectionService.disConnect(clientId);
        }
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

        boolean isConnected=false;/*是否已经链接了*/
        int clientId=0;
        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
        if(!ObjectUtils.isEmpty(accountClientId)&&(Integer)accountClientId>0){
            clientId=(Integer)accountClientId;
            if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
                /*已连接 直接返回*/
                isConnected=true;
            }else {
                /*未连接 删除数据 避免冗余*/
                redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
                redisManager.hdel(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
            }
        }
        if(!isConnected){
            clientId=connectionService.getUserConnect(serverName,username,password);
        }
        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        TradeRecordInfo info=obtainOpenOrderInfo(clientId,orderId);
        if(!isConnected){
            /*因为此次查询做的链接 需要关闭*/
            connectionService.disConnect(clientId);
        }

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

        List<TradeRecordInfo> recordInfoList=new ArrayList<>();
        IntByReference closeCount = new IntByReference(closeCountInt);
        OrderLibrary.TradeRecord b = new OrderLibrary.TradeRecord();
        OrderLibrary.TradeRecord[] closeReference = (OrderLibrary.TradeRecord[]) b.toArray(closeCountInt);
        boolean isSuccess = OrderLibrary.library.MT4API_GetCloseOrders(clientId, closeReference, closeCount);
        if (isSuccess) {
            log.info("获取已关闭交易的订单列表：" + isSuccess + "，已关闭订单个数：" + closeCountInt);

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
                        + "close_time : " + closetradeRecord.close_time + ","
                        + "close_price : " + closetradeRecord.close_price + ","
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
     * 根据时间段获取历史订单信息
     * @param clientId
     * @param nHisTimeFrom
     * @param nHisTimeTo
     * @return
     */
    public List<TradeRecordInfo> obtainHistoryOrderInfo(int clientId,int nHisTimeFrom,int nHisTimeTo) {
        IntByReference closeOrderCount = new IntByReference();
        OrderLibrary.library.MT4API_GetTradeHistoryCount(clientId,nHisTimeFrom,nHisTimeTo, closeOrderCount);
        int closeCountInt = closeOrderCount.getValue();
        System.out.println("时间段内已关闭订单个数：" + closeCountInt);

        if(closeCountInt==0){
            return null;
        }
        List<TradeRecordInfo> recordInfoList=new ArrayList<>();
        IntByReference closeCount = new IntByReference(closeCountInt);
        OrderLibrary.TradeRecord b = new OrderLibrary.TradeRecord();
        OrderLibrary.TradeRecord[] closeReference = (OrderLibrary.TradeRecord[]) b.toArray(closeCountInt);
        boolean isSuccess = OrderLibrary.library.MT4API_DownloadTradeHistory(clientId, closeReference, closeCount);
        if (isSuccess) {
            System.out.println("获取已关闭交易的订单列表：" + isSuccess + "，已关闭订单个数：" + closeCountInt);

            for (int i = 0; i < closeCountInt; i++) {
                OrderLibrary.TradeRecord closetradeRecord = closeReference[i];

                System.out.println("订单信息："
                        + "order : " + closetradeRecord.order + ","
                        + "login : " + closetradeRecord.login + ","
                        + "symbol : " + new String(closetradeRecord.symbol) + ","
                        + "digits : " + closetradeRecord.digits + ","
                        + "volume : " + closetradeRecord.volume + ","
                        + "open_time : " + closetradeRecord.open_time + ","
                        + "open_price : " + closetradeRecord.open_price + ","
                        + "close_time : " + closetradeRecord.close_time + ","
                        + "close_price : " + closetradeRecord.close_price + ","
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
     * @return (返回错误码)
     */
    public int orderTradeRetrySyn(int clientId,OrderLibrary.TradeRecord tradeRecord,int magic,String comment,int current, int times){

        if (!ConnectLibrary.library.MT4API_IsConnect(clientId)) {
            log.info("connect borker false, clientId error!");
            return TradeErrorEnum.ACC_DIS_CONNECT.code();
        }
        if(!TraderLibrary.library.MT4API_IsTradeAllowed(clientId)){
            log.error("MT4API_IsTradeAllowed false!");
            TradeUtil.printError(clientId);
            return TradeErrorEnum.TRADE_NOT_ALLOWED.code();
        }
        //循环获取行情信息，直到获取到最新的行情信息
        int getQuoteTimes=1;
        QuoteLibrary.QuoteEventInfo.ByReference quoteInfo = new QuoteLibrary.QuoteEventInfo.ByReference();
        try {
            while (!QuoteLibrary.library.MT4API_GetQuote(clientId, new String(tradeRecord.symbol), quoteInfo)&&getQuoteTimes<5) {
                TimeUnit.MILLISECONDS.sleep(100);
                getQuoteTimes++;
            }
            if(getQuoteTimes>=20){
                log.error("getQuoteInfo false!");
                return TradeErrorEnum.ACC_QUOTE_GET_ERROR.code();
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return TradeErrorEnum.ACC_QUOTE_GET_ERROR.code();
        }

        //注意转换
        double volume=tradeRecord.volume*0.01;
        long time = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

        //异步提交
        int isTrade = TraderLibrary.library.MT4API_OrderSendAsync(clientId, new String(tradeRecord.symbol).trim(),
                (byte) tradeRecord.cmd, volume,
                quoteInfo.fAsk, 30, tradeRecord.stoploss, tradeRecord.takeprofit,
                comment, magic, (int) time + 24 * 60 * 60);

        log.info("交易信息：isTrade:"+isTrade);
        if (isTrade>0) {
            log.info("交易信息：success! isTrade,times:"+current);
            return TradeErrorEnum.SUCCESS.code();
        } else {
            TradeUtil.printError(clientId);
            log.info("跟单信息：fail! times:"+current);
            if(current>=times){
                //达到次数
                log.error("跟单信息：fail finally! times:"+current);
                return TradeErrorEnum.ORDER_OPEN_SYN_FAIL.code();
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
                    (byte) tradeRecord.cmd, tradeRecord.volume*0.01,
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
    public int sendOrderCloseAsync(int clientId,int orderId,String symbol,int volume){

        if(clientId==0||orderId==0||volume==0|| StringUtils.isEmpty(symbol)){
            log.info("关闭订单 异步, 参数为空!");
            return TradeErrorEnum.TRADE_PARAM_NULL_ERROR.code();
        }
        if (!ConnectLibrary.library.MT4API_IsConnect(clientId)) {
            log.info("connect borker false, clientId error!");
            return TradeErrorEnum.ACC_DIS_CONNECT.code();
        }

        if(!TraderLibrary.library.MT4API_IsTradeAllowed(clientId)){
            return TradeErrorEnum.TRADE_NOT_ALLOWED.code();
        }

        //循环获取行情信息，直到获取到最新的行情信息
        int getQuoteTimes=1;
        QuoteLibrary.QuoteEventInfo.ByReference quoteInfo = new QuoteLibrary.QuoteEventInfo.ByReference();
        try {
            while (!QuoteLibrary.library.MT4API_GetQuote(clientId, symbol, quoteInfo) && getQuoteTimes<5) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            if(getQuoteTimes>=20){
                log.error("getQuoteInfo false!");
                return TradeErrorEnum.ACC_QUOTE_GET_ERROR.code();
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return TradeErrorEnum.ACC_QUOTE_GET_ERROR.code();
        }

        /*发送异步关闭请求*/
        int isSend= TraderLibrary.library.MT4API_OrderCloseAsync(clientId,symbol,orderId,
                volume*0.01,quoteInfo.fAsk,30);
        if (isSend>0) {
            log.info("order close success! isSend,orderid:"+orderId);
            return TradeErrorEnum.SUCCESS.code();
        } else {
            TradeUtil.printError(clientId);
            return TradeErrorEnum.ORDER_CLOSE_SYN_FAIL.code();
        }
    }


    /**
     * 关闭订单
     * @param clientId
     * @param orderId
     * @param symbol
     * @param volume
     * @return
     */
    public boolean sendOrderClose(int clientId,int orderId,String symbol,int volume){

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
                    volume*0.01,quoteInfo.fAsk,30,orderSend);
            if (isSend) {
                log.info("order close success! isSend,orderid:"+orderId);
            } else {
                TradeUtil.printError(clientId);
                throw new BusinessException("order close fail!");
            }
        }
        return true;
    }


    /**
     * 处理跟单开仓逻辑
     * @param signalRecord
     * @param followRule
     * @return
     */
    public int followOpenLogic(OrderLibrary.TradeRecord signalRecord,OrderLibrary.TradeRecord orderSend,JSONObject followRule){
        try {
            if(signalRecord==null||followRule==null){
                log.error("处理跟单开仓逻辑,传入数据为空！");
                return TradeErrorEnum.FOLLOW_RULE_DATA_ERROR.code();
            }
            /*判断订单类型*/
            if(signalRecord.cmd!= OrderTypeEnum.OP_BUY.code()
                &&signalRecord.cmd!= OrderTypeEnum.OP_SELL.code()
                    &&signalRecord.cmd!= OrderTypeEnum.OP_BUY_LIMIT.code()
                    &&signalRecord.cmd!= OrderTypeEnum.OP_SELL_LIMIT.code()
                    &&signalRecord.cmd!= OrderTypeEnum.OP_BUY_STOP.code()
                    &&signalRecord.cmd!= OrderTypeEnum.OP_SELL_STOP.code()){
                log.error("处理跟单开仓逻辑,开仓类型无效！cmd:"+signalRecord.cmd);
                return TradeErrorEnum.FOLLOW_ORDER_TYPE_MATCH_ERROR.code();
            }

            //跟单方向（0 正向跟单，1  反向跟单）
            int followDirect=0;
            //跟单模式（0 多空跟单，1 只跟多单，2 只跟空单）
            int followMode=0;
            //跟单类型（0 按手数比例，1 按固定金额，2 按固定手数）
            int followType=0;
            //跟单数量/系数
            Double followAmount=0.0;
            if(followRule.getInteger("followDirect")!=null){
                followDirect=followRule.getInteger("followDirect");
            }
            if(followRule.getInteger("followMode")!=null){
                followMode=followRule.getInteger("followMode");
            }
            if(followRule.getInteger("followType")!=null){
                followType=followRule.getInteger("followType");
            }
            if(followRule.getBigDecimal("followAmount")!=null){
                followAmount= followRule.getBigDecimal("followAmount").doubleValue();
            }

            orderSend.order=signalRecord.order;
            orderSend.symbol=signalRecord.symbol;
            orderSend.stoploss=signalRecord.stoploss;
            orderSend.takeprofit=signalRecord.takeprofit;
            //1、判断跟单模式（0 多空跟单，1 只跟多单，2 只跟空单）

            //2、判断跟单方向
            if(followDirect== OrderConstant.ORDER_FOLLOW_DIRECT_FORWARD){
                //正向
                orderSend.cmd=signalRecord.cmd;
            }else {
                //反向
                if(signalRecord.cmd == OrderTypeEnum.OP_BUY.code()){
                    orderSend.cmd=OrderTypeEnum.OP_SELL.code();
                }else if(signalRecord.cmd== OrderTypeEnum.OP_SELL.code()){
                    orderSend.cmd=OrderTypeEnum.OP_BUY.code();
                }else if(signalRecord.cmd== OrderTypeEnum.OP_BUY_LIMIT.code()){
                    orderSend.cmd=OrderTypeEnum.OP_SELL_LIMIT.code();
                }else if(signalRecord.cmd== OrderTypeEnum.OP_SELL_LIMIT.code()){
                    orderSend.cmd=OrderTypeEnum.OP_BUY_LIMIT.code();
                }else if(signalRecord.cmd== OrderTypeEnum.OP_BUY_STOP.code()){
                    orderSend.cmd=OrderTypeEnum.OP_SELL_STOP.code();
                }else if(signalRecord.cmd== OrderTypeEnum.OP_SELL_STOP.code()){
                    orderSend.cmd=OrderTypeEnum.OP_BUY_STOP.code();
                }else {
                    return TradeErrorEnum.ORDER_CMD_ERROR.code();
                }
            }

            //根据规则确定手数  (0 按手数比例；1 按固定金额；2 按固定手数)（最低净值/最低净值百分比）
            if(followType==OrderConstant.ORDER_FOLLOW_TYPE_HANDS_RATE){
                //0 按手数比例
                Double lots=signalRecord.volume*followAmount;
                if(lots.intValue()>=1){
                    orderSend.volume=lots.intValue();
                }else {
                    orderSend.volume=1;
                }
            }else if(followType==OrderConstant.ORDER_FOLLOW_TYPE_AMOUNT_FIXED){
                //1 按固定金额
                Double lots=followAmount/signalRecord.open_price;
                if(lots.intValue()>=1){
                    orderSend.volume=lots.intValue();
                }else {
                    orderSend.volume=1;
                }
            }else if(followType==OrderConstant.ORDER_FOLLOW_TYPE_HANDS_FIXED){
                //2 按固定手数
                if(followAmount.intValue()>=1){
                    orderSend.volume=followAmount.intValue();
                }else {
                    orderSend.volume=1;
                }
            }
            // 判断是否达到上线金额
            // 判断是否超过 最低净值/最低净值百分比
            if(orderSend.volume==0){
                return TradeErrorEnum.ORDER_VOLUME_ERROR.code();
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return TradeErrorEnum.FOLLOW_RULE_DEAL_ERROR.code();
        }
        return TradeErrorEnum.SUCCESS.code();
    }
}
