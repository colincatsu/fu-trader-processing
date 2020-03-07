package com.future.trader.controller;


import com.alibaba.fastjson.JSON;
import com.future.trader.common.exception.DataConflictException;
import com.future.trader.common.result.RequestParams;
import com.future.trader.service.AccountInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/trader/account")
public class AccountController {

    Logger log= LoggerFactory.getLogger(AccountController.class);

    @Resource
    AccountInfoService accountInfoService;

    //设置信号源监听
    @RequestMapping(value= "/setSignalMonitor",method= RequestMethod.POST)
    public @ResponseBody int setSignalMonitor(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();

        if(conditionMap==null||conditionMap.get("brokerName")==null
                ||conditionMap.get("username")==null||conditionMap.get("password")==null){
            log.error("getUserCloseOrders null params!");
            throw new DataConflictException("setSignalMonitor null params!");
        }

        String brokerName=String.valueOf(conditionMap.get("brokerName"));
        int username=Integer.parseInt(String.valueOf(conditionMap.get("username")));
        String password=String.valueOf(conditionMap.get("password"));

        return accountInfoService.setSignalMonitor(brokerName,username,password);
    }

    //链接账户
    @RequestMapping(value= "/setAccountConnect",method= RequestMethod.POST)
    public @ResponseBody int setAccountConnect(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();

        if(conditionMap==null||conditionMap.get("brokerName")==null
                ||conditionMap.get("username")==null||conditionMap.get("password")==null){
            log.error("setAccountConnnect null params!");
            throw new DataConflictException("setAccountConnnect null params!");
        }

        String brokerName=String.valueOf(conditionMap.get("brokerName"));
        int username=Integer.parseInt(String.valueOf(conditionMap.get("username")));
        String password=String.valueOf(conditionMap.get("password"));

        return accountInfoService.setAccountConnect(brokerName,username,password);
    }

    //断开链接账户
    @RequestMapping(value= "/setAccountDisConnect",method= RequestMethod.POST)
    public @ResponseBody boolean setAccountDisConnect(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();

        if(conditionMap==null||conditionMap.get("brokerName")==null
                ||conditionMap.get("username")==null||conditionMap.get("password")==null){
            log.error("setAccountConnnect null params!");
            throw new DataConflictException("setAccountConnnect null params!");
        }
        String brokerName=String.valueOf(conditionMap.get("brokerName"));
        int username=Integer.parseInt(String.valueOf(conditionMap.get("username")));
        String password=String.valueOf(conditionMap.get("password"));

        return accountInfoService.setAccountDisConnect(brokerName,username,password);
    }

    //设置账户跟随关系
    @RequestMapping(value= "/setAccountFollowRelation",method= RequestMethod.POST)
    public @ResponseBody boolean setAccountFollowRelation(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();

        if(conditionMap==null||conditionMap.get("followName")==null
                ||conditionMap.get("signalName")==null){
            log.error("getUserCloseOrders null params!");
            throw new DataConflictException("setAccountFollowRelation null params!");
        }

        int signalName=Integer.parseInt(String.valueOf(conditionMap.get("signalName")));
        int followName=Integer.parseInt(String.valueOf(conditionMap.get("followName")));
        String followRule=String.valueOf(conditionMap.get("followRule"));

        return accountInfoService.setAccountFollowRelation(signalName,followName, JSON.parseObject(followRule));
    }
}