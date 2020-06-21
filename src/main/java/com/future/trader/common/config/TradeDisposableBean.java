package com.future.trader.common.config;

import com.future.trader.service.AccountInfoService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 结束的时候执行
 * @author dmw
 *
 * 2019年4月15日
 */
@Component
public class TradeDisposableBean implements DisposableBean {

    @Autowired
    AccountInfoService accountInfoService;

    @Override
    public void destroy() throws Exception {
        System.out.println("closing------");
        accountInfoService.disConnectByFollowRelation();
    }
}