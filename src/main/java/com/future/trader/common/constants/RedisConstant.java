package com.future.trader.common.constants;

/**
 * redis key
 */
public class RedisConstant {


	/*账户 记录账户链接信息 clientId:account*/
	public static final String H_ACCOUNT_CLIENT_INFO="tradeAccountClientInfo";
	/*账户 记录账户链接信息 account:clientId*/
	public static final String H_ACCOUNT_CONNECT_INFO="tradeAccountConnectInfo";
	/*账户 跟随关系*/
	public static final String H_ACCOUNT_FOLLOW_RELATION="tradeAccountFollowRelation";
	/*账户 记录断开链接的账户信息 serverName+","+username+","+password*/
	public static final String L_ACCOUNT_INFO_DISCONNECT="accountInfoDisconnect";

	/*订单 跟随交易中*/
	public static final String H_ORDER_FOLLOW_TRADING="tradeOrderFollowTrading";
	/*订单 跟随交易中数据*/
	public static final String H_ORDER_FOLLOW_TRADING_DATA="tradeOrderFollowTradingData";
	/*订单 跟随关闭中*/
	public static final String H_ORDER_FOLLOW_CLOSING="tradeOrderFollowClosing";
	/*订单 跟随关闭中数据*/
	public static final String H_ORDER_FOLLOW_CLOSING_DATA="tradeOrderFollowClosingData";
	/*订单 跟随订单关系*/
	public static final String H_ORDER_FOLLOW_ORDER_RELATION="tradeOrderFollowOrderRelation";

	/*订单 信号源订单*/
	public static final String L_ORDER_FOLLOW_SIGNAL_ORDER="tradeOrderFollowSignalOrder";
	/*订单 跟随订单信息*/
	public static final String L_ORDER_FOLLOW_ORDERS="tradeOrderFollowOrders";
	/*订单 跟随错误*/
	public static final String L_ORDER_FOLLOW_ERROR_DATA="tradeOrderFollowErrorData";

	/*服务器 服务器主从管理保存*/
	public static final String H_SERVER_SLAVE_INFO="serverSlaveInfo";
}
