package com.future.trader.controller;


import com.future.trader.bean.TradeRecordInfo;
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
}