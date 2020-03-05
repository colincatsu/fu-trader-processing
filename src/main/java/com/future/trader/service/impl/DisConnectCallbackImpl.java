package com.future.trader.service.impl;


import com.future.trader.api.InstanceLibrary;
import com.future.trader.common.constants.RedisConstant;
import com.future.trader.service.ConnectionService;
import com.future.trader.service.DisConnectCallback;
import com.future.trader.util.RedisManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

/**
 * 行情回调接口实现累
 */
@Service
public class DisConnectCallbackImpl implements DisConnectCallback {

    Logger log= LoggerFactory.getLogger(DisConnectCallbackImpl.class);
    @Resource
    ConnectionService connectionService;
    @Autowired
    RedisManager redisManager;

    @Override
    public void onDisConnect(int clientId) {
        log.info("onDisConnect callback clientId:"+clientId);
        /*被动销毁的clientId需要重新链接*/
        reConnect(clientId,1,3);
    }

    /**
     * 账号断线重连
     * @param clientId
     * @param current
     * @param times
     */
    public void reConnect(int clientId,int current, int times){
        /*需要根据clientId 获取user信息 重新init*/
        boolean login = connectionService.reConnectWithConnectCallback(clientId);
        log.info("MT4API_Connect reConnect current:"+current+", login" + login);

        if(!login){
            if(current>=times){
                log.error("reConnect faile,clinetId:"+clientId);
                log.error("Destory,clinetId:"+clientId);
                InstanceLibrary.library.MT4API_Destory(clientId);
                Object accountInfo= redisManager.hget(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
                if(ObjectUtils.isEmpty(accountInfo)){
                    return;
                }
                String[] accounts=String.valueOf(accountInfo).split(",");
                redisManager.hdel(RedisConstant.H_ACCOUNT_CONNECT_INFO,String.valueOf(accounts[1]));
                redisManager.hdel(RedisConstant.H_ACCOUNT_CLIENT_INFO,String.valueOf(clientId));
                log.info("connection break success!");
                return;
            }
            //重连一次
            try {
                Thread.sleep(1000);
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
            reConnect(clientId,current+1,times);
        }
    }
}
