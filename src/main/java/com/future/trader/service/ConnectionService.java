package com.future.trader.service;


import com.future.trader.api.ConnectLibrary;
import com.future.trader.api.InstanceLibrary;
import com.future.trader.common.constants.RedisConstant;
import com.future.trader.util.RedisManager;
import com.future.trader.util.TradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class ConnectionService {

    Logger log= LoggerFactory.getLogger(ConnectionService.class);

    @Autowired
    DisConnectCallback disConnectCallbackImpl;
    @Autowired
    RedisManager redisManager;

    /**
     * 获取连接
     * @param serverName
     * @param username
     * @param password
     * @return
     */
    public int getUserConnect(String serverName,int username,String password){
        int clientId = InstanceLibrary.library.MT4API_Create();
        log.info("clientId : " + clientId);
        if (clientId > 0) {
            String srvName =    "srv\\"+serverName+".srv";
            boolean init = ConnectLibrary.library.MT4API_InitBySrvFile(clientId,srvName,username,password,
                    0,0);
            log.info("MT4API_Init : " + init);
            if (init) {
                boolean login = ConnectLibrary.library.MT4API_Connect(clientId);
                log.info("MT4API_Connect login : " + login);
                if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
                    log.info("connect borker success!");
                    return clientId;
                }
            }
            TradeUtil.printError(clientId);
        }
        return 0;
    }

    /**
     * 断开连接
     * @param clientId
     * @return
     */
    public boolean disConnect(int clientId){
        log.info("disConnect ,clientId : " + clientId);
        if (ConnectLibrary.library.MT4API_DisConnect(clientId)) {
            log.info("disConnect success!");
        }else {
            TradeUtil.printError(clientId);
        }
        return true;
    }

    /**
     * 获取连接
     * @param serverName
     * @param username
     * @param password
     * @return
     */
    public int getUserConnectWithConnectCallback(String serverName,int username,String password){
        int clientId = InstanceLibrary.library.MT4API_Create();
        log.info("clientId : " + clientId);
        if (clientId > 0) {
            String srvName =    "srv\\"+serverName+".srv";
            boolean init = ConnectLibrary.library.MT4API_InitBySrvFile(clientId,srvName,username,password,
                    0,0);
            log.info("MT4API_Init : " + init);
            ConnectLibrary.library.MT4API_SetDisconnectEventHandler(clientId,disConnectCallbackImpl,clientId);
            if (init) {
                boolean login = ConnectLibrary.library.MT4API_Connect(clientId);
                log.info("MT4API_Connect login : " + login);
                if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
                    log.info("connect borker success!");
                    return clientId;
                }
            }
            TradeUtil.printError(clientId);
        }
        return 0;
    }

    /**
     * 重新连接
     * @param clientId
     * @return
     */
    public boolean reConnectWithConnectCallback(int clientId){
        if (clientId <= 0) {
            log.info("error clientId : " + clientId);
            return false;
        }
        Object clientInfo= redisManager.hget(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
        if(ObjectUtils.isEmpty(clientInfo)){
            log.error("get no clientInfo from redis by clientId:"+clientId);
            return false;
        }
        String[] userInfo=String.valueOf(clientInfo).split(",");
        if(userInfo.length<3){
            log.error("get error clientInfo from redis by clientId:"+clientId);
            return false;
        }
        String srvName =    "srv\\"+userInfo[0]+".srv";
        boolean init = ConnectLibrary.library.MT4API_InitBySrvFile(clientId,srvName,Integer.parseInt(userInfo[1]),userInfo[2],
                0,0);
        log.info("MT4API_Init : " + init);
        if (!init) {
            TradeUtil.printError(clientId);
            return false;
        }
        boolean login = ConnectLibrary.library.MT4API_Connect(clientId);
        log.info("MT4API_Connect login : " + login);
        if(!login){
            TradeUtil.printError(clientId);
            return false;
        }
        if (!ConnectLibrary.library.MT4API_IsConnect(clientId)) {
            TradeUtil.printError(clientId);
            return false;
        }
        ConnectLibrary.library.MT4API_SetDisconnectEventHandler(clientId,disConnectCallbackImpl,clientId);
        log.info("connect borker success!");
        return true;
    }

    /**
     * 获取连接
     * @param serverName
     * @param username
     * @param password
     * @param nThreadHisTimeFrom 初始获取历史订单的开始时间
     * @param nThreadHisTimeTo   初始获取历史订单的结束时间
     * @return
     */
    public int getUserConnect(String serverName,int username,String password,int nThreadHisTimeFrom,int nThreadHisTimeTo){
        int clientId = InstanceLibrary.library.MT4API_Create();
        log.info("clientId : " + clientId);
        if (clientId > 0) {
            String srvName =    "srv\\"+serverName+".srv";
            boolean init = ConnectLibrary.library.MT4API_InitBySrvFile(clientId,srvName,username,password,
                    nThreadHisTimeFrom,nThreadHisTimeTo);
            log.info("MT4API_Init : " + init);
            if (init) {
                boolean login = ConnectLibrary.library.MT4API_Connect(clientId);
                log.info("MT4API_Connect login : " + login);
                if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
                    log.info("connect borker success!");
                    return clientId;
                }
            }
            TradeUtil.printError(clientId);
        }
        return 0;
    }

    /**
     * 获取连接
     * @param serverName
     * @param username
     * @param password
     * @param nThreadHisTimeFrom 初始获取历史订单的开始时间
     * @param nThreadHisTimeTo   初始获取历史订单的结束时间
     * @return
     */
    public int getUserConnectWithConnectCallback(String serverName,int username,String password,int nThreadHisTimeFrom,int nThreadHisTimeTo){
        int clientId = InstanceLibrary.library.MT4API_Create();
        log.info("clientId : " + clientId);
        if (clientId > 0) {
            String srvName =    "srv\\"+serverName+".srv";
            boolean init = ConnectLibrary.library.MT4API_InitBySrvFile(clientId,srvName,username,password,
                    nThreadHisTimeFrom,nThreadHisTimeTo);
            log.info("MT4API_Init : " + init);
            ConnectLibrary.library.MT4API_SetDisconnectEventHandler(clientId,disConnectCallbackImpl,clientId);
            if (init) {
                boolean login = ConnectLibrary.library.MT4API_Connect(clientId);
                log.info("MT4API_Connect login : " + login);
                if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
                    log.info("connect borker success!");
                    return clientId;
                }
            }
            TradeUtil.printError(clientId);
        }
        return 0;
    }
}
