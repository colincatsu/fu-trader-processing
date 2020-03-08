package com.future.trader.common.enums;


/**
 * 用户模块统一返回状态码及描述信息
 * 用户模块错误码区间：200000-299999
 *
 * @author Admin
 * @version: 1.0
 */
public enum OrderTypeEnum{
    /* enum { OP_BUY=0,OP_SELL,OP_BUY_LIMIT,OP_SELL_LIMIT,OP_BUY_STOP,OP_SELL_STOP,OP_BALANCE,OP_CREDIT }; */

    OP_BUY(0, "OP_BUY"),
    OP_SELL(1, "OP_SELL"),
    OP_BUY_LIMIT(2, "OP_BUY_LIMIT"),
    OP_SELL_LIMIT(3, "OP_SELL_LIMIT"),
    OP_BUY_STOP(4, "OP_BUY_STOP"),
    OP_SELL_STOP(5, "OP_SELL_STOP"),
    OP_BALANCE(6, "OP_BALANCE"),
    OP_CREDIT(7, "OP_CREDIT"),
    OP_BUY_STOP_LIMIT(8, "OP_BUY_STOP_LIMIT"),
    OP_SELL_STOP_LIMIT(9, "OP_SELL_STOP_LIMIT");

    /**
     * 全局异常状态码
     * 状态码规则：状态码是6位长度的字符串。示例：1 01 100
     * 1：应用标记（例如组织机构应用或者人员管理应用）
     * 01：应用下的模块（例组织机构下的获取机构数据）
     * 100：定义的业务异常
     */


    /**
     * 状态码
     */
    private Integer code;
    /**
     * 提示信息
     */
    private String message;

    /**
     * 构造器
     */
    OrderTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取状态码
     */
    public Integer code() {
        return this.code;
    }

    /**
     * 获取提示信息
     */
    public String message() {
        return this.message;
    }

    /**
     * 通过枚举属性名称获取提示信息
     */
    public static String getMessage(String name) {
        for (OrderTypeEnum item : OrderTypeEnum.values()) {
            if (item.name().equals(name)) {
                return item.message;
            }
        }
        return name;
    }

    /**
     * 通过枚举值获取状态码
     */
    public static Integer getCode(String message) {
        for (OrderTypeEnum item : OrderTypeEnum.values()) {
            if (item.message.equals(message)) {
                return item.code;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
