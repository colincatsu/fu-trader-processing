package com.future.trader.controller;


import com.alibaba.fastjson.JSONObject;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.common.exception.DataConflictException;
import com.future.trader.common.helper.PageInfoHelper;
import com.future.trader.common.result.RequestParams;
import com.future.trader.service.OrderInfoService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/trader/order")
public class OrderController {

    Logger log= LoggerFactory.getLogger(OrderController.class);

    @Resource
    OrderInfoService orderInfoService;

    //获取历史订单
    @RequestMapping(value= "/getUserCloseOrders",method= RequestMethod.POST)
    public @ResponseBody
    PageInfo<TradeRecordInfo> getUserCloseOrders(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();
        PageInfoHelper helper = requestParams.getPageInfoHelper();
        return orderInfoService.getUserCloseOrders(conditionMap,helper);
    }

    //获取open订单
    @RequestMapping(value= "/getUserOpenOrders",method= RequestMethod.POST)
    public @ResponseBody
    PageInfo<TradeRecordInfo> getUserOpenOrders(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();
        PageInfoHelper helper = requestParams.getPageInfoHelper();
        return orderInfoService.getUserOpenOrders(conditionMap,helper);
    }

    //关闭订单
    @RequestMapping(value= "/sendOrderCloseAsync",method= RequestMethod.POST)
    public @ResponseBody boolean sendOrderCloseAsync(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();
        if(conditionMap==null||conditionMap.get("clientId")==null
                ||conditionMap.get("orderId")==null||conditionMap.get("symbol")==null||conditionMap.get("volume")==null){
            log.error("sendrderCloseAsync null params!");
            throw new DataConflictException("sendrderCloseAsync null params!");
        }
        int clientId=Integer.parseInt(String.valueOf(conditionMap.get("clientId")));
        int orderId=Integer.parseInt(String.valueOf(conditionMap.get("orderId")));
        String symbol= String.valueOf(conditionMap.get("symbol"));
        double volume=Double.valueOf(String.valueOf(conditionMap.get("volume")));

        return orderInfoService.sendOrderCloseAsync(clientId,orderId,symbol,volume);
    }

    //关闭订单
    @RequestMapping(value= "/sendOrderClose",method= RequestMethod.POST)
    public @ResponseBody boolean sendOrderClose(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();
        if(conditionMap==null||conditionMap.get("clientId")==null
                ||conditionMap.get("orderId")==null||conditionMap.get("symbol")==null||conditionMap.get("volume")==null){
            log.error("sendrderCloseAsync null params!");
            throw new DataConflictException("sendrderCloseAsync null params!");
        }
        int clientId=Integer.parseInt(String.valueOf(conditionMap.get("clientId")));
        int orderId=Integer.parseInt(String.valueOf(conditionMap.get("orderId")));
        String symbol= String.valueOf(conditionMap.get("symbol"));
        double volume=Double.valueOf(String.valueOf(conditionMap.get("volume")));

        return orderInfoService.sendOrderClose(clientId,orderId,symbol,volume);
    }

    //关闭订单
    @RequestMapping(value= "/getSymbolInfo",method= RequestMethod.POST)
    public @ResponseBody
    JSONObject getSymbolInfo(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();
        if(conditionMap==null||conditionMap.get("clientId")==null||conditionMap.get("symbol")==null){
            log.error("sendrderCloseAsync null params!");
            throw new DataConflictException("sendrderCloseAsync null params!");
        }
        int clientId=Integer.parseInt(String.valueOf(conditionMap.get("clientId")));
        String symbol= String.valueOf(conditionMap.get("symbol"));

        return orderInfoService.obtainSymbolInfo(clientId,symbol);
    }
}