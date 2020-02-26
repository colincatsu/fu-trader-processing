package com.future.trader.api;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * 连接相关接口
 */
public interface ConnectLibrary extends Library {

    /**
     * 加载动态库文件
     */
    ConnectLibrary library = Native.load("mt4api", ConnectLibrary.class);

    /**
     * 初始化登陆等相关信息
     *
     * @param clientId           通信实例Id
     * @param bokerName          登陆的服务器名称（客户端config目录下的srv文件名）
     * @param userName           登陆账号
     * @param password           登陆密码
     * @param host               登陆服务器ip
     * @param port               登陆服务器端口
     * @param nThreadHisTimeFrom 初始获取历史订单的开始时间
     * @param nThreadHisTimeTo   初始获取历史订单的结束时间
     * @return 成功：true，失败：false
     * <p>
     * nThreadHisTimeFrom和nThreadHisTimeTo设置为0则默认请求最近一个月的历史交易记录
     */
    boolean MT4API_Init(int clientId, String bokerName, int userName, String password,
                        String host, int port, int nThreadHisTimeFrom, int nThreadHisTimeTo);

    /**
     * 根据srv文件初始化登陆等相关信息
     *
     * @param clientId           通信实例id
     * @param srvFile            登陆的平台服务器srv文件
     * @param userName           登陆账号
     * @param password           登陆密码
     * @param nThreadHisTimeFrom 初始获取历史订单的开始时间
     * @param nThreadHisTimeTo   初始获取历史订单的结束时间
     * @return 成功：true，失败：false
     * <p>
     * nThreadHisTimeFrom和nThreadHisTimeTo设置为0则默认请求最近一个月的历史交易记录
     */
    boolean MT4API_InitBySrvFile(int clientId, String srvFile, int userName, String password,
                                 int nThreadHisTimeFrom, int nThreadHisTimeTo);

    /**
     * 连接登陆服务器
     *
     * @param clientId 通信实例id
     * @return 成功：true，失败：false;
     */
    boolean MT4API_Connect(int clientId);

    /**
     * 设置连接断开回调接收处理函数
     * @param clientId 通信实例id（in）
     * @param pHandler 回调接收函数指针（in）
     * @param param 回调函数回传参数
     * @return 成功：true，失败：false
     */
    boolean MT4API_SetDisconnectEventHandler(int clientId, Callback pHandler, int param);

    /**
     * 断开连接
     *
     * @param clientId 通信实例id
     * @return 成功：true，失败：false
     */
    boolean MT4API_DisConnect(int clientId);

    /**
     * 是否已经连接登陆服务器
     *
     * @param clientId 通信实例id
     * @return true：是，false：否
     */
    boolean MT4API_IsConnect(int clientId);
}
