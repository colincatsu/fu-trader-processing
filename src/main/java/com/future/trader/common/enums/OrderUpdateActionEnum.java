package com.future.trader.common.enums;


import com.future.trader.common.enums.utils.JnaEnum;

/**
 * 订单类型枚举
 */
public enum OrderUpdateActionEnum implements JnaEnum<OrderUpdateActionEnum> {
    OUA_PositionOpen, //开仓
    OUA_PositionClose, //平仓
    OUA_PositionModify, //修改
    OUA_PendingOpen, //挂单
    OUA_PendingDelete, //删除挂单
    OUA_PendingModify, //挂单修改
    OUA_PendingFill, //挂单触发
    OUA_Balance, //余额
    OUA_Credit; //信用

    @Override
    public int getIntValue() {
        return this.ordinal();
    }

    @Override
    public OrderUpdateActionEnum getIntValue(int i) {
        for (OrderUpdateActionEnum orderUpdateActionEnum : this.values()) {
            if (orderUpdateActionEnum.getIntValue() == i) {
                return orderUpdateActionEnum;
            }
        }
        return null;
    }
}
