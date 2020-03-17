package com.future.trader.common.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * 结束的时候执行
 * @author dmw
 *
 * 2019年4月15日
 */
@Component
public class TradeDisposableBean implements DisposableBean {

    @Override
    public void destroy() throws Exception {
        System.out.println("结束1");
    }
}