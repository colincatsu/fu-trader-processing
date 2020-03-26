package com.future.trader.timing;

import com.alibaba.fastjson.JSONObject;
import com.future.trader.common.constants.RedisConstant;
import com.future.trader.common.enums.TradeErrorEnum;
import com.future.trader.util.RedisManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.Map;

/**
 * @author ：haijun
 * @date ：Created in 2020-02-17 15:17
 * @description：用户跟单结果定时检查
 * @modified By：
 * @version: 1/0$
 */
@Configuration
@EnableScheduling
public class OrderTradeMonitor {

    Logger log= LoggerFactory.getLogger(OrderTradeMonitor.class);

    @Autowired
    RedisManager redisManager;

    /**
     * 订单执行结果 （每分钟监测一次）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void monitor(){
        long time=new Date().getTime();
        /*监测正在交易中的 订单，有没有过期的，过期时间60s*/

        Map<Object,Object>  tradingOrders=redisManager.hmget(RedisConstant.H_ORDER_FOLLOW_TRADING);
        for(Object key:tradingOrders.keySet()){
            try {
                long vTime=(long)redisManager.hget(RedisConstant.H_ORDER_FOLLOW_TRADING,String.valueOf(key));
                //超过1分钟 就认为交易失败
                if(time-vTime>60000){
                    Object followData=redisManager.hget(RedisConstant.H_ORDER_FOLLOW_TRADING_DATA,String.valueOf(key));
                    JSONObject followJson=(JSONObject)followData;
                    log.info(followJson.toJSONString());
                    followJson.put("errorCode", TradeErrorEnum.TRADE_TIMEOUT.code());
                    //交易超时 写入错误日志
                    redisManager.lSet(RedisConstant.L_ORDER_FOLLOW_ERROR_DATA,followJson);
                    //删除 正在交易记录和缓存
                    redisManager.hdel(RedisConstant.H_ORDER_FOLLOW_TRADING_DATA,String.valueOf(key));;
                    redisManager.hdel(RedisConstant.H_ORDER_FOLLOW_TRADING,String.valueOf(key));
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }

        /*监测正在关闭中的 订单，有没有过期的，过期时间60s*/
        Map<Object,Object>  closingOrders=redisManager.hmget(RedisConstant.H_ORDER_FOLLOW_CLOSING);
        for(Object key:closingOrders.keySet()){
            try {
                long vTime=(long)redisManager.hget(RedisConstant.H_ORDER_FOLLOW_CLOSING,String.valueOf(key));
                //超过1分钟 就认为交易失败
                if(time-vTime>60000){
                    Object followData=redisManager.hget(RedisConstant.H_ORDER_FOLLOW_CLOSING_DATA,String.valueOf(key));
                    JSONObject followJson=(JSONObject)followData;
                    log.info(followJson.toJSONString());
                    followJson.put("errorCode", TradeErrorEnum.TRADE_TIMEOUT.code());
                    //交易超时 写入错误日志
                    redisManager.lSet(RedisConstant.L_ORDER_FOLLOW_ERROR_DATA,followJson);
                    //删除 正在交易记录和缓存
                    redisManager.hdel(RedisConstant.H_ORDER_FOLLOW_CLOSING_DATA,String.valueOf(key));;
                    redisManager.hdel(RedisConstant.H_ORDER_FOLLOW_CLOSING,String.valueOf(key));
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }
}
