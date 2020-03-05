package com.future.trader.controller;


import com.future.trader.common.exception.DataConflictException;
import com.future.trader.common.helper.PageInfoHelper;
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

    //设置信号源监听
    @RequestMapping(value= "/setAccountMonitor",method= RequestMethod.POST)
    public @ResponseBody int setAccountMonitor(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();
        if(conditionMap==null||conditionMap.get("clientId")==null){
            log.error("getUserCloseOrders null params!");
            throw new DataConflictException("setAccountMonitor null params!");
        }
        int clientId=Integer.parseInt(String.valueOf(conditionMap.get("clientId")));

        return accountInfoService.setAccountMonitor(clientId);
    }

    //链接账户
    @RequestMapping(value= "/setAccountConnnect",method= RequestMethod.POST)
    public @ResponseBody int setAccountConnnect(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();

        if(conditionMap==null||conditionMap.get("brokerName")==null
                ||conditionMap.get("username")==null||conditionMap.get("password")==null){
            log.error("getUserCloseOrders null params!");
            throw new DataConflictException("setAccountConnnect null params!");
        }

        String brokerName=String.valueOf(conditionMap.get("brokerName"));
        int username=Integer.parseInt(String.valueOf(conditionMap.get("username")));
        String password=String.valueOf(conditionMap.get("password"));

        return accountInfoService.setAccountConnnect(brokerName,username,password);
    }

    //断开链接账户
    @RequestMapping(value= "/setAccountDisConnnect",method= RequestMethod.POST)
    public @ResponseBody boolean setAccountDisConnnect(@RequestBody RequestParams<Map> requestParams){
        // 获取请求参数
        Map conditionMap = requestParams.getParams();

        if(conditionMap==null||conditionMap.get("clientId")==null){
            log.error("getUserCloseOrders null params!");
            throw new DataConflictException("setAccountDisConnnect null params!");
        }
        int clientId=Integer.parseInt(String.valueOf(conditionMap.get("clientId")));

        return accountInfoService.setAccountDisConnnect(clientId);
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

        int followName=Integer.parseInt(String.valueOf(conditionMap.get("followName")));
        int signalName=Integer.parseInt(String.valueOf(conditionMap.get("signalName")));

        return accountInfoService.setAccountFollowRelation(signalName,followName);
    }
}