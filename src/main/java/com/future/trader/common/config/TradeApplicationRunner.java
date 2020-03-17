package com.future.trader.common.config;


import com.future.trader.service.AccountInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TradeApplicationRunner implements ApplicationRunner {

    @Autowired
    AccountInfoService accountInfoService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("FuApplicationRunner~!");
        /*重新链接跟单关系---账号*/
        accountInfoService.initConnectByFollowRelation();
    }

}