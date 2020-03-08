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

	/*订单 跟随交易中*/
	public static final String H_ORDER_FOLLOW_TRADING="tradeOrderFollowTrading";
	/*订单 跟随关闭中*/
	public static final String H_ORDER_FOLLOW_CLOSING="tradeOrderFollowClosing";
	/*订单 跟随订单信息*/
	public static final String L_ORDER_FOLLOW_ORDERS="tradeOrderFollowOrders";
	/*订单 跟随订单关系*/
	public static final String H_ORDER_FOLLOW_ORDER_RELATION="tradeOrderFollowOrderRelation";

}