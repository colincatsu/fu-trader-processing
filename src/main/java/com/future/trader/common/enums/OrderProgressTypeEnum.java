package com.future.trader.common.enums;


import com.future.trader.common.enums.utils.JnaEnum;

/**
 * 订单处理类型枚举
 */
public enum OrderProgressTypeEnum implements JnaEnum<OrderProgressTypeEnum> {
    TPT_Rejected, //拒绝
    TPT_Accepted, //接收
    TPT_InProcess, //处理中
    TPT_Opened, //开仓
    TPT_Closed, //平仓
    TPT_Modified, //修改
    TPT_PendingDeleted, //删除挂单
    TPT_ClosedBy, //对冲平仓
    TPT_MultipleClosedBy, //同一证劵多订单对冲平仓
    TPT_Timeout, //超时
    TPT_Price, //获取价格
    TPT_Cancel, //取消
    TPT_UNKNOW;

    @Override
    public int getIntValue() {
        return this.ordinal();
    }

    @Override
    public OrderProgressTypeEnum getIntValue(int i) {
        for (OrderProgressTypeEnum orderProgressTypeEnum : this.values()) {
            if (orderProgressTypeEnum.getIntValue() == i) {
                return orderProgressTypeEnum;
            }
        }
        return null;
    }
}
