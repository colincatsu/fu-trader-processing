package com.future.trader.common.constants;

/**
 * 订单常亮
 */
public class OrderConstant {


	/*magic作为社区跟单唯一标识*/
	public static final int ORDER_FOLLOW_MAGIC=20992020;

	/*跟单方向  （0 正向跟单，1  反向跟单）*/
	public static final int ORDER_FOLLOW_DIRECT_FORWARD=0;
	public static final int ORDER_FOLLOW_DIRECT_REVERSE=1;

	/*跟单模式（0 多空跟单，1 只跟多单，2 只跟空单）*/
	public static final int ORDER_FOLLOW_MODE_ALL=0;
	public static final int ORDER_FOLLOW_MODE_BUY=1;
	public static final int ORDER_FOLLOW_MODE_SELL=2;

	/*跟单类型（0 按手数比例，1 按固定金额，2 按固定手数）*/
	public static final int ORDER_FOLLOW_TYPE_HANDS_RATE=0;
	public static final int ORDER_FOLLOW_TYPE_AMOUNT_FIXED=1;
	public static final int ORDER_FOLLOW_TYPE_HANDS_FIXED=2;
}
