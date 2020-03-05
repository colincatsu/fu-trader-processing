package com.future.trader.api;

import com.future.trader.common.enums.OrderProgressTypeEnum;
import com.future.trader.common.enums.OrderUpdateActionEnum;
import com.future.trader.common.enums.utils.EnumConverter;
import com.future.trader.common.enums.utils.JnaEnum;
import com.sun.jna.*;

import java.util.*;

/**
 * 交易接口
 */
public interface TraderLibrary extends Library {

    Map<String, Object> options = Collections.unmodifiableMap(new HashMap<String, Object>() {
        private static final long serialVersionUID = -1987971664975780480L;

        {
            put(Library.OPTION_TYPE_MAPPER, new DefaultTypeMapper() {
                {
                    addTypeConverter(JnaEnum.class, new EnumConverter());
                }
            });
        }
    });

    /**
     * 加载动态库文件
     */
    TraderLibrary library = Native.load("mt4api", TraderLibrary.class, options);

    /**
     * 当前是否可进行交易操作
     *
     * @param clientId 通信实例id（in）
     * @return true：可以，false：不可以
     */
    boolean MT4API_IsTradeAllowed(int clientId);

    /**
     * 设置订单处理结果函数
     *
     * @param clientId 通信实例id（in）
     * @param pHandler 订单结果回调函数指针（in）
     * @param param    回传参数（in）
     * @return 成功：true，失败：false
     */
    boolean MT4API_SetOrderNotifyEventHandler(int clientId, Callback pHandler, int param);

    /**
     * 设置订单处理过程回调函数
     *
     * @param clientId 通信实例id（in）
     * @param pHandler 订单处理过程回调函数指针（in）
     * @param param    回传参数（in）
     * @return 成功：true，失败：false
     */
    boolean MT4API_SetOrderUpdateEventHandler(int clientId, Callback pHandler, int param);

    /**
     * 下单（开单/开仓）
     *
     * @param clientId 通信实例id（in）
     * @param symbol 证劵名称（in）
     * @param tradeCmd 交易指令
     * @param volume 量（手）（in）
     * @param price 下单价格
     * @param ie_deviation 点差（in）
     * @param stoploss 止损价（in）
     * @param takeprofit 止盈价（in）
     * @param comment 说明（in）
     * @param magic 魔法号（当前选定订单的表示号）（in）
     * @param expiration 过期时间（in）
     * @param tradeRecord 下单结果（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_OrderSend(int clientId, String symbol, byte tradeCmd, double volume,
                             double price, int ie_deviation, double stoploss, double takeprofit,
                             String comment, int magic, int expiration,
                             OrderLibrary.TradeRecord.ByReference tradeRecord);

    /**
     * 异步下单（开仓/开单）
     *
     * @param clientId     通信实例id(in)
     * @param szSymbol     证劵名称（in）
     * @param tradeCmd     交易指令（OP_buy, op_sel...参见宏定义）（in）
     * @param volumn       量（手）（in）
     * @param price        下单价格（in）
     * @param ie_deviation 点差（in）
     * @param stoploss     止损价（in）
     * @param takeprofit   止盈价（in）
     * @param pszComment   说明（in）
     * @param magic        魔法号（当前选定订单的表示号）（in）
     * @param expiration   过期时间（in）
     * @return 返回当前交易请求号，大于0，则成功，等于0，则失败
     */
    int MT4API_OrderSendAsync(int clientId, String szSymbol, byte tradeCmd, double volumn,
                              double price, int ie_deviation, double stoploss, double takeprofit,
                              String pszComment, int magic, int expiration);

    /**
     * 关闭订单（平仓）
     *
     * @param clientId 通信实例id（in）
     * @param symbol 证劵名称（in）
     * @param order 订单号（in）
     * @param volumn 量（手）（in）
     * @param price 下单价格（in）
     * @param ie_deviation 点差（in）
     * @param tradeRecord 下单结果（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_OrderClose(int clientId, String symbol, int order, double volumn,
                              double price, int ie_deviation,
                              OrderLibrary.TradeRecord.ByReference tradeRecord);

    /**
     * 异步关闭订单（平仓）
     *
     * @param clientId     通信实例id（in）
     * @param szSymbol     证劵名称（in）
     * @param order        订单号（in）
     * @param volumn       量（手）（in）
     * @param price        下单价格（in）
     * @param ie_deviation 点差（in）
     * @return 返回当前交易请求号，大于0，则成功，等于0，则失败
     */
    int MT4API_OrderCloseAsync(int clientId, String szSymbol, int order, double volumn,
                               double price, int ie_deviation);

    /**
     * 同一价格关闭锁仓的两个订单（同时平掉一个多单和一个空单）
     *
     * @param clientId 通信实例id（in）
     * @param order1   订单号1（in）
     * @param order2   反向订单号2（in）
     * @return 成功：true，失败：false
     * <p>
     * 注意：次函数需要服务器设置支持才能调用成功
     */
    boolean MT4API_OrderCloseBy(int clientId, int order1, int order2);

    /**
     * 异步同一价格关闭锁仓的两个订单(同时平掉一个多单和一个空单);
     *
     * @param clientId 通信实例id（in）
     * @param order1   订单号1（in）
     * @param order2   反向订单号2（in）
     * @return 成功：true，失败：false
     * <p>
     * 注意：次函数需要服务器设置支持才能调用成功
     */
    boolean MT4API_OrderCloseByAsync(int clientId, int order1, int order2);

    /**
     * 关闭指定证券的所有订单(对指定证券的所有订单进行平仓);
     *
     * @param clientId 通信实例id（in）
     * @param szSymbol 证劵名称（in）
     * @return 成功:true， 失败:false;
     * <p>
     * 此函数需要服务器设置支持才能调用成功。
     */
    boolean MT4API_OrderMultipleCloseBy(int clientId, String szSymbol);

    /**
     * 异步关闭指定证券的所有订单(对指定证券的所有订单进行平仓);
     *
     * @param clientId 通信实例id（in）
     * @param szSymbol 证券名称;(IN)
     * @return 返回当前交易请求号，大于 0，则成功，等于 0，失败;
     * <p>
     * 此函数需要服务器设置支持才能调用成功。
     */
    boolean MT4API_OrderMultipleCloseByAsync(int clientId, String szSymbol);

    /**
     * 删除挂单
     *
     * @param clientId 通信实例id（in）
     * @param order    挂单号（in）
     * @return 成功：true，失败：false
     */
    boolean MT4API_OrderDelete(int clientId, int order);

    /**
     * 异步删除挂单
     *
     * @param clientId 通信实例id（in）
     * @param order    挂单号（in）
     * @return 返回当前交易请求号，大于0，则成功，等于0，则失败
     */
    int MT4API_OrderDeleteAsync(int clientId, int order);

    /**
     * 修改挂单
     *
     * @param clientId 通信实例id（in）
     * @param tradeCmd 交易指令（in）
     * @param order 订单号（in）
     * @param price 下单价格（in）
     * @param stoploss 止损价（in）
     * @param takeprofit 止盈价（in）
     * @param expiration 过期时间（in）
     * @param tradeRecord 下单结果（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_OrderModify(int clientId, byte tradeCmd, int order, double price,
                               double stoploss, double takeprofit, int expiration,
                               OrderLibrary.TradeRecord.ByReference tradeRecord);

    /**
     * 异步修改挂单
     *
     * @param clientId   通信实例id（in）
     * @param tradeCmd   交易指令（op_buy,op_sell...参见宏定义）（in）
     * @param order      订单号（in）
     * @param price      下单价格（in）
     * @param stoploss   止损价（in）
     * @param takeprofit 止盈价（in）
     * @param expiration 过期时间
     * @return 返回当前交易请求号，大于0，则成功，等于0，则失败
     */
    int MT4API_OrderModifyAsync(int clientId, byte tradeCmd, int order, double price,
                                double stoploss, double takeprofit, int expiration);

    /**
     * 订单通知结构体
     */
    class OrderNotifyEventInfo extends Structure {
        public int nReqId;
        public short nStatus;
        public OrderProgressTypeEnum emType;
        public OrderLibrary.NotifyRecord notifyRecord;
        public double bid;
        public double ask;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"nReqId", "nStatus", "emType", "notifyRecord", "bid", "ask"});
        }
    }

    /**
     * 订单更新结构体
     */
    class OrderUpdateEventInfo extends Structure {
        public OrderUpdateActionEnum updateAction;
        public double balance;
        public double credit;
        public OrderLibrary.TradeRecord tradeRecord;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"updateAction", "balance", "credit", "tradeRecord"});
        }
    }
}
