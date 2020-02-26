package com.future.trader.api;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * 账户信息接口
 */
public interface AccountInfoLibrary extends Library {

    /**
     * 加载动态库文件
     */
    AccountInfoLibrary library = Native.load("mt4api", AccountInfoLibrary.class);

    /**
     * 获取当前登陆账号
     *
     * @param clientId 通讯实例id（in）
     * @param userName 账号（out)
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetUser(int clientId, IntByReference userName);

    /**
     * 获取当前登陆账号名称
     *
     * @param clientId 通讯实例id（in）
     * @param userName 账号名称（out）
     * @param size     userName分配空间大小（建议不低于64）（in）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetUserName(int clientId, byte[] userName, int size);

    boolean MT4API_GetUserGroup();

    /**
     * 获取当前登陆账号是否demo账号
     *
     * @param clientId 通讯实例id
     * @return true：是，fale：否
     */
    boolean MT4API_IsDemoAccount(int clientId);

    /**
     * 获取账号资金情况
     *
     * @param clientId   通讯实例id（in）
     * @param balance    余额（out）
     * @param credit     信用（out)
     * @param margin     占用保障金（out）
     * @param freeMargin 可用保障金（out)
     * @param equity     净值（out）
     * @param profit     浮动盈亏（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetMoneyInfo(int clientId, DoubleByReference balance, DoubleByReference credit,
                                DoubleByReference margin, DoubleByReference freeMargin,
                                DoubleByReference equity, DoubleByReference profit);

    /**
     * 获取可用保证金
     *
     * @param clientId 通信实例id（in）
     * @param freeMargin 可用保证金（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetFreeMargin(int clientId, DoubleByReference freeMargin);

    /**
     * 获取占用保证金
     *
     * @param clientId 通信实例id（in）
     * @param margin 占用保证金（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetMargin(int clientId, DoubleByReference margin);

    /**
     * 获取余额
     *
     * @param clientId 通信实例id（in）
     * @param balance 余额（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetBalance(int clientId, DoubleByReference balance);

    /**
     * 获取信用
     *
     * @param clientId 通信实例id（in）
     * @param credit 信用（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetCredit(int clientId, DoubleByReference credit);

    /**
     * 获取浮动盈亏
     *
     * @param clientId 通信实例id（in）
     * @param profit 浮动盈亏（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetProfit(int clientId, DoubleByReference profit);

    /**
     * 获取净值
     *
     * @param clientId 通信实例（id）
     * @param equity 净值（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetEquity(int clientId, DoubleByReference equity);

    /**
     * 获取杠杆比例
     *
     * @param clientId 通信实例id（in）
     * @param leverage 杠杆比例（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetLeverage(int clientId, IntByReference leverage);
}

