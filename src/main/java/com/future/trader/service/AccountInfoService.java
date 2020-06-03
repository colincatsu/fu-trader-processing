package com.future.trader.service;


import com.alibaba.fastjson.JSONObject;
import com.future.trader.api.*;
import com.future.trader.bean.AccountInfo;
import com.future.trader.common.constants.RedisConstant;
import com.future.trader.common.enums.TradeErrorEnum;
import com.future.trader.common.exception.BusinessException;
import com.future.trader.common.exception.DataConflictException;
import com.future.trader.service.impl.OrderNotifyCallbackImpl;
import com.future.trader.service.impl.QuoteCallbackImpl;
import com.future.trader.util.RedisManager;
import com.future.trader.util.StringUtils;
import com.future.trader.util.TradeUtil;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Service
public class AccountInfoService {

    Logger log= LoggerFactory.getLogger(AccountInfoService.class);

    @Autowired
    RedisManager redisManager;
    @Autowired
    SignalOrderUpdateCallback signalOrderUpdateCallbackImpl;
    @Autowired
    FollowOrderUpdateCallback followOrderUpdateCallbackImpl;
    @Autowired
    OrderNotifyCallback orderNotifyCallbackImpl;
    @Resource
    ConnectionService connectionService;

    /**
     * 设置信号源账户 监听
     * @param serverName
     * @param username
     * @param password
     * @return
     */
    public int setSignalMonitor(String serverName,int username,String password){
        if(StringUtils.isEmpty(serverName)||StringUtils.isEmpty(password)||username==0){
            log.error("设置信号源账户 监听,传入传入参数为空！");
            throw new DataConflictException("设置信号源账户 监听,传入传入参数为空！");
        }
        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
        if(!ObjectUtils.isEmpty(accountClientId)&&(Integer)accountClientId>0){
            int currentClientId=(Integer)accountClientId;
            if (ConnectLibrary.library.MT4API_IsConnect(currentClientId)) {
                /*重新设置信号源监听*/
                TraderLibrary.library.MT4API_SetOrderUpdateEventHandler(currentClientId, signalOrderUpdateCallbackImpl, currentClientId);
                /*已连接 直接返回*/
                log.info("client already connected!");
                return currentClientId;
            }else {
                /*未连接 删除数据 避免冗余*/
                redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
                redisManager.hdel(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(currentClientId));
            }
        }
        int clientId = connectionService.getUserConnectWithConnectCallback(serverName,username,password);
        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        /*设置信号源监听*/
        TraderLibrary.library.MT4API_SetOrderUpdateEventHandler(clientId, signalOrderUpdateCallbackImpl, clientId);

        log.info("set signal monitor account: serverName:"+serverName+",username:"+username);

        String accountInfo=serverName+","+username+","+password;
        redisManager.hset(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username),clientId);
        redisManager.hset(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId),accountInfo);
        return clientId;
    }

    /**
     * 设置跟单关系
     * @param signalName
     * @param followName
     * @param followRule
     * @return
     */
    public boolean addAccountFollowRelation(int signalName,int followName,JSONObject followRule){
        if(signalName<=0||followName<=0){
            log.error("设置跟单关系,参数错误！ signalName:"+signalName+",+"+followName);
        }
        Object object= redisManager.hget(RedisConstant.H_ACCOUNT_FOLLOW_RELATION,String.valueOf(signalName));
        JSONObject followJson=new JSONObject();
        if(!ObjectUtils.isEmpty(object)){
            followJson=(JSONObject)object;
        }
        followJson.put(String.valueOf(followName),followRule);
        /*将跟单关系保存至redis*/
        redisManager.hset(RedisConstant.H_ACCOUNT_FOLLOW_RELATION,String.valueOf(signalName),followJson);
        log.info("设置跟单关系 成功！ signalName:"+signalName+",+followName:"+followName);
        return true;
    }

    /**
     * 移除跟单关系
     * @param signalName
     * @param followName
     * @return
     */
    public boolean removeAccountFollowRelation(int signalName,int followName){
        if(signalName<=0||followName<=0){
            log.error("设置跟单关系,参数错误！ signalName:"+signalName+",+"+followName);
        }
        Object object= redisManager.hget(RedisConstant.H_ACCOUNT_FOLLOW_RELATION,String.valueOf(signalName));
        JSONObject followJson=new JSONObject();
        if(ObjectUtils.isEmpty(object)){
            log.info("信号源 无跟单关系，signalName:"+signalName);
            return true;
        }
        followJson=(JSONObject)object;
        followJson.remove(String.valueOf(followName));
        /*将跟单关系保存至redis*/
        redisManager.hset(RedisConstant.H_ACCOUNT_FOLLOW_RELATION,String.valueOf(signalName),followJson);
        log.error("移除跟单关系 成功！ signalName:"+signalName+",+followName:"+followName);
        return true;
    }

    /**
     * 链接账户
     * @param serverName
     * @param username
     * @param password
     * @return
     */
    public int setAccountConnect(String serverName,int username,String password){
        if(StringUtils.isEmpty(serverName)||StringUtils.isEmpty(password)||username==0){
            log.error("设置链接账户,传入传入参数为空！");
            throw new DataConflictException("设置链接账户,传入传入参数为空！");
        }
        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
        if(!ObjectUtils.isEmpty(accountClientId)&&(Integer)accountClientId>0){
            int currentClientId=(Integer)accountClientId;
            if (ConnectLibrary.library.MT4API_IsConnect(currentClientId)) {
                /*已连接 直接返回*/
                log.info("client already connected!");
                return currentClientId;
            }else {
                /*未连接 删除数据 避免冗余*/
                redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
                redisManager.hdel(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(currentClientId));
            }
        }
        int clientId = connectionService.getUserConnectWithConnectCallback(serverName,username,password);
        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        /*设置订单结果回调*/
        /*TraderLibrary.library.MT4API_SetOrderNotifyEventHandler(clientId, orderNotifyCallbackImpl, clientId);*/
        /*设置跟随订单更新回调*/
        TraderLibrary.library.MT4API_SetOrderUpdateEventHandler(clientId, followOrderUpdateCallbackImpl, clientId);

        String accountInfo=serverName+","+username+","+password;
        redisManager.hset(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username),clientId);
        redisManager.hset(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId),accountInfo);
        log.info("set account connnect: serverName:"+serverName+",username:"+username);
        return clientId;
    }

    /**
     * 链接账户
     * @param serverName
     * @param username
     * @param password
     * @return
     */
    public int setAccountConnectTradeAllowed(String serverName,int username,String password){
        if(StringUtils.isEmpty(serverName)||StringUtils.isEmpty(password)||username==0){
            log.error("设置链接账户,传入传入参数为空！");
            throw new DataConflictException("设置链接账户,传入传入参数为空！");
        }
        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
        if(!ObjectUtils.isEmpty(accountClientId)&&(Integer)accountClientId>0){
            int currentClientId=(Integer)accountClientId;
            if (ConnectLibrary.library.MT4API_IsConnect(currentClientId)) {
                if(!TraderLibrary.library.MT4API_IsTradeAllowed(currentClientId)){
                    log.error("account is not trade Allowed!");
                    TradeUtil.printError(currentClientId);
                    throw new BusinessException(TradeErrorEnum.TRADE_NOT_ALLOWED.message());
                }
                /*已连接 直接返回*/
                log.info("client already connected!");
                return currentClientId;
            }else {
                /*未连接 删除数据 避免冗余*/
                redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
                redisManager.hdel(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(currentClientId));
            }
        }
        int clientId = connectionService.getUserConnectWithConnectCallback(serverName,username,password);
        if(clientId==0){
            // 初始化失败！
            log.error("client init error !");
            throw new BusinessException("client init error !");
        }
        if(!TraderLibrary.library.MT4API_IsTradeAllowed(clientId)){
            log.error("MT4API_IsTradeAllowed false!");
            TradeUtil.printError(clientId);
            throw new BusinessException(TradeErrorEnum.TRADE_NOT_ALLOWED.message());
        }
        /*设置订单结果回调*/
        /*TraderLibrary.library.MT4API_SetOrderNotifyEventHandler(clientId, orderNotifyCallbackImpl, clientId);*/
        /*设置跟随订单更新回调*/
        TraderLibrary.library.MT4API_SetOrderUpdateEventHandler(clientId, followOrderUpdateCallbackImpl, clientId);

        /*FollowOrderUpdateCallback followOrderUpdateCallbackTest=new FollowOrderUpdateCallbackTest();
        TraderLibrary.library.MT4API_SetOrderUpdateEventHandler(clientId, followOrderUpdateCallbackTest, clientId);*/

        /*OrderNotifyCallback orderNotifyCallbackTest=new OrderNotifyCallbackTest();
        TraderLibrary.library.MT4API_SetOrderNotifyEventHandler(clientId,orderNotifyCallbackTest,clientId);*/

        String accountInfo=serverName+","+username+","+password;
        redisManager.hset(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username),clientId);
        redisManager.hset(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId),accountInfo);
        log.info("set account connnect: serverName:"+serverName+",username:"+username);
        return clientId;
    }

    /**
     * 断开链接账户
     * @param serverName
     * @param username
     * @param password
     * @return
     */
    public boolean setAccountDisConnect(String serverName,int username,String password){
        Object accountClientId=redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(username));
        if(ObjectUtils.isEmpty(accountClientId)||(Integer)accountClientId==0){
            log.info("client already disConnected!");
            return true;
        }
        int clientId=(Integer)accountClientId;
        return setAccountDisConnect(clientId);
    }
    /**
     * 断开链接账户
     * @param clientId
     * @return
     */
    public boolean setAccountDisConnect(int clientId){
        if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
            try {
                InstanceLibrary.library.MT4API_Destory(clientId);
                Object accountInfo= redisManager.hget(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
                if(ObjectUtils.isEmpty(accountInfo)){
                    return true;
                }
                String[] accounts=String.valueOf(accountInfo).split(",");
                redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(accounts[1]));
                redisManager.hdel(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
                log.info("connection break success!");
                return true;
            }catch (Exception e){
                log.error("connection break fail!");
                TradeUtil.printError(clientId);
                return false;
            }
        }else {
            log.info("connection already break!");
        }
        return true;
    }


    /**
     * 根据跟随关系初始化链接(只能初始化已连接的)
     */
    public void initConnectByFollowRelation(){

        Map<Object, Object> allFollows= redisManager.hmget(RedisConstant.H_ACCOUNT_FOLLOW_RELATION);
        if(allFollows==null){
            log.info("no follow relations");
            return;
        }
        log.info("------------------initConnectByFollowRelation begin-----------------"+new Date().getTime());

        Object clientId;
        Object accountInfo="";
        String userName="";
        String account[];
        int connectClientId=0;
        for(Object key:allFollows.keySet()){
            userName=(String)key;
            log.info("------------------initConnectByFollowRelation  begin-signals："+userName);
            try {
                clientId= redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,userName);
                if(ObjectUtils.isEmpty(clientId)||(Integer)clientId==0){
                    /*该信号源已经停止监听 此处不做处理*/
                    continue;
                }
                accountInfo=redisManager.hget(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
                if(ObjectUtils.isEmpty(accountInfo)){
                    /*该信号源监听数据不完整*/
                    redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,userName);
                    continue;
                }
                account=String.valueOf(accountInfo).split(",");
                log.info("------------------initConnectByFollowRelation  begin-signal："+userName);
                /*初始化信号源*/
                connectClientId=setSignalMonitor(account[0],Integer.parseInt(account[1]),account[2]);
                if(connectClientId<=0){
                    log.error("signal connect fail,signalMtAccId:"+userName);
                }
                log.info("------------------initConnectByFollowRelation  end-signal："+userName);
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
            Object object= redisManager.hget(RedisConstant.H_ACCOUNT_FOLLOW_RELATION,String.valueOf(userName));
            if(ObjectUtils.isEmpty(object)){
                log.info("信号源无跟随关系 signalMtAccId:"+userName);
                continue;
            }
            JSONObject followJson=(JSONObject)object;
            for(String jsonKey:followJson.keySet()){
                try {
                    /*循环连接跟随账号*/
                    userName=jsonKey;
                    log.info("------------------initConnectByFollowRelation  begin-siganl-follow："+userName);

                    clientId= redisManager.hget(RedisConstant.H_ACCOUNT_CONNECT_INFO,userName);
                    if(ObjectUtils.isEmpty(clientId)||(Integer)clientId==0){
                        /*没有clientId 无法获取用户账户信息 此处不做处理*/
                        continue;
                    }
                    accountInfo=redisManager.hget(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
                    if(ObjectUtils.isEmpty(accountInfo)){
                        /*该用户监听数据不完整 */
                        redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,userName);
                        continue;
                    }
                    account=String.valueOf(accountInfo).split(",");
                    connectClientId=setAccountConnectTradeAllowed(account[0],Integer.parseInt(account[1]),account[2]);
                    if(connectClientId<=0){
                        log.error("user connect fail,userMtAccId:"+userName);
                    }
                    log.info("------------------initConnectByFollowRelation  end-siganl-follow："+userName);
                }catch (Exception e){
                    log.error(e.getMessage(),e);
                    continue;
                }
            }
            log.info("------------------initConnectByFollowRelation end-signals");

        }
        log.info("------------------initConnectByFollowRelation end-----------------"+new Date().getTime());
    }

    /**
     * 查询用户账户信息
     * @param serverName
     * @param username
     * @param password
     * @return
     */
    public AccountInfo getAccountInfo(String serverName,int username,String password){
        if(StringUtils.isEmpty(serverName)||StringUtils.isEmpty(password)||username==0){
            log.error("查询用户账户信息,传入传入参数为空！");
            throw new DataConflictException("查询用户账户信息,传入传入参数为空！");
        }
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
        AccountInfo info=obtainAccoutInfo(clientId);
        if(!isConnected){
            /*因为此次查询做的链接 需要关闭*/
            connectionService.disConnect(clientId);
        }

        return info;
    }

    /**
     * 获取账户信息
     * @param clientId
     */
    public AccountInfo obtainAccoutInfo(int clientId) {
        IntByReference account = new IntByReference();
        AccountInfoLibrary.library.MT4API_GetUser(clientId, account);
        System.out.println("get user " + account.getValue());

        int size = 64;
        byte[] name = new byte[size];
        AccountInfoLibrary.library.MT4API_GetUserName(clientId, name, size);
        System.out.println("get user name : " + new String(name));

        //获取账号资金情况
        DoubleByReference balance = new DoubleByReference();
        DoubleByReference credit = new DoubleByReference();
        DoubleByReference margin = new DoubleByReference();
        DoubleByReference freeMargin = new DoubleByReference();
        DoubleByReference equity = new DoubleByReference();
        DoubleByReference profit = new DoubleByReference();
        AccountInfoLibrary.library.MT4API_GetMoneyInfo(clientId, balance, credit, margin,
                freeMargin, equity, profit);
        log.info("余额 ：" + balance.getValue());
        log.info("信用 ：" + credit.getValue());
        log.info("占用保证金 ：" + margin.getValue());
        log.info("可用保证金 ：" + freeMargin.getValue());
        log.info("净值 ：" + equity.getValue());
        log.info("浮动盈亏 ：" + profit.getValue());

        IntByReference leverage = new IntByReference();
        AccountInfoLibrary.library.MT4API_GetLeverage(clientId, leverage);
        log.info("杠杆比例：" + leverage.getValue());

        AccountInfo accountInfo=new AccountInfo();
        accountInfo.setUser(account.getValue());
        accountInfo.setName(new String(name));
        accountInfo.setBalance(balance.getValue());
        accountInfo.setCredit(credit.getValue());
        accountInfo.setMargin(margin.getValue());
        accountInfo.setFreeMargin(freeMargin.getValue());
        accountInfo.setEquity(equity.getValue());
        accountInfo.setProfit(profit.getValue());
        accountInfo.setLeverage(leverage.getValue());
        return accountInfo;
    }

    /**
     * 获取服务器信息
     * @param clientId
     */
    public void obtainServerInfo(int clientId) {
        IntByReference time = new IntByReference();
        ServerInfoLibrary.library.MT4API_GetServerTime(clientId, time);
        int timeValue = time.getValue();
        System.out.println("服务器时间：" + timeValue);

        LocalDateTime ldt = LocalDateTime.ofEpochSecond(Long.valueOf(timeValue), 0, ZoneOffset.ofHours(8));
        String formatTime = ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("服务器时间，格式化：" + formatTime);

        ShortByReference serverBuild = new ShortByReference();
        ServerInfoLibrary.library.MT4API_GetServerBuild(clientId, serverBuild);
        System.out.println("服务器版本号：" + serverBuild.getValue());

        IntByReference serverInfoCount = new IntByReference();
        ServerInfoLibrary.library.MT4API_GetSeverInfoCount(clientId, serverInfoCount);
        System.out.println("服务器个数：" + serverInfoCount.getValue());

        int count = serverInfoCount.getValue();
        ServerInfoLibrary.ServerInfo serverInfo = new ServerInfoLibrary.ServerInfo();
        ServerInfoLibrary.ServerInfo[] serverInfos = (ServerInfoLibrary.ServerInfo[]) serverInfo.toArray(count);
        IntByReference intByReference = new IntByReference(count);
        boolean isSuccess = ServerInfoLibrary.library.MT4API_GetSeverInfos(clientId, serverInfos, intByReference);
        System.out.println("get server：" + isSuccess);
        if (isSuccess) {
            System.out.println("服务服务器列表：" + isSuccess);
            for (int i = 0; i < count; i++) {
                ServerInfoLibrary.ServerInfo server = serverInfos[i];
                System.out.println("服务器地址：" + new String(server.server));
            }
        } else {
            TradeUtil.printError(clientId);
        }

        System.out.println();
    }

    /**
     * 设置证劵行情报价数据回调函数指针
     * @param clientId
     * @return
     */
    public boolean setQuoteEventHandler(int clientId) {
        if(clientId==0){
            return false;
        }
        QuoteCallback quoteCallback = new QuoteCallbackImpl();
        QuoteLibrary.library.MT4API_SetQuoteEventHandler(clientId, quoteCallback, clientId);
        return true;
    }

    /**
     * 设置订单处理结果函数
     * @param clientId
     * @return
     */
    public boolean setOrderNotifyEventHandler(int clientId) {
        if(clientId==0){
            return false;
        }
        OrderNotifyCallback notifyCallback = new OrderNotifyCallbackImpl();
        TraderLibrary.library.MT4API_SetOrderNotifyEventHandler(clientId, notifyCallback, clientId);
        return true;
    }

    /**
     * 设置订单处理过程回调函数
     * @param clientId
     * @return
     */
    public boolean setOrderUpdateEventHandler(int clientId) {
        if(clientId==0){
            return false;
        }
//        orderInfoService.obtainOpenOrderInfo(clientId);
//        OrderUpdateCallback updateCallback = new OrderUpdateCallbackImpl();
        TraderLibrary.library.MT4API_SetOrderUpdateEventHandler(clientId, signalOrderUpdateCallbackImpl, clientId);
        log.info("success set signal mode, clientId: "+clientId);
        return true;
    }
}
