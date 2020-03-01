package com.future.trader.service.impl;


import com.future.trader.api.ConnectLibrary;
import com.future.trader.service.ConnectionService;
import com.future.trader.service.DisConnectCallback;
import com.future.trader.util.TradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 行情回调接口实现累
 */
@Service
public class DisConnectCallbackImpl implements DisConnectCallback {

    Logger log= LoggerFactory.getLogger(DisConnectCallbackImpl.class);
    @Resource
    ConnectionService connectionService;

    @Override
    public void onDisConnect(int clientId) {
        log.info("onDisConnect callback clientId:"+clientId);
        /*被动销毁的clientId需要重新链接*/
        /*需要根据clientId 获取user信息 重新init*/
        boolean login = connectionService.reConnectWithConnectCallback(clientId);
        log.info("MT4API_Connect reConnect 1: " + login);

        if(!login){
            //重连一次
            try {
                Thread.sleep(1000);
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
            login = connectionService.reConnectWithConnectCallback(clientId);
            log.info("MT4API_Connect reConnect 2: " + login);
        }
    }
}
