package com.future.trader.api;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

/**
 * 订单信息接口
 */
public interface OrderLibrary extends Library {

    /**
     * 加载动态库文件
     */
    OrderLibrary library = Native.load("mt4api", OrderLibrary.class);

    /**
     * 通过订单号获取init时传入的时间范围内的交易订单信息
     *
     * @param clientId 通信实例id（in）
     * @param order 订单id（in）
     * @param tradeRecord 订单信息（out）
     * @return 成功：true，失败：false
     *
     * 注意：此接口只能获取MT4API_Init时传入的时间段的历史订单信息
     */
    boolean MT4API_GetCloseOrder(int clientId, int order, TradeRecord.ByReference tradeRecord);

    /**
     * 获取init时传入的时间范围内的交易订单记录个数
     *
     * @param clientId 通信实例id（in）
     * @param orderCount 订单个数
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetCloseOrdersCount(int clientId, IntByReference orderCount);

    /**
     * 获取init时传入的时间范围内的交易订单记录
     * @param clientId 通信实例id（in）
     * @param orders 订单信息（out）
     * @param count 订单个数（in,out）
     * @return 成功：true，失败：false
     *
     * 注意：此接口只能获取MT4API_Initj时传入的时间段的历史订单
     */
    boolean MT4API_GetCloseOrders(int clientId, TradeRecord[] orders, IntByReference count);

    /**
     * 通过订单号获取交易中的订单信息
     *
     * @param clientId 通信实例id（in）
     * @param order 订单id（in）
     * @param tradeRecord 订单信息（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetOpenOrder(int clientId, int order, TradeRecord.ByReference tradeRecord);

    /**
     * 获取交易中的订单个数
     *
     * @param clientId 通信实例id（in）
     * @param orderCount 订单个数（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetOpenOrdersCount(int clientId, IntByReference orderCount);

    /**
     * 获取交易中的订单信息
     *
     * @param clientId 通信实例id（in）
     * @param orders 订单信息（out）
     * @param count 订单个数（in/out）
     * @return 成功：true
     */
    boolean MT4API_GetOpenOrders(int clientId, TradeRecord[] orders, IntByReference count);

    /**
     * 获取历史交易订单个数
     *
     * @param clientId 通信实例id（in）
     * @param timeFrom 开始时间（in）
     * @param timeTo 结束时间（in）
     * @param orderCount 订单记录个数（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetTradeHistoryCount(int clientId, int timeFrom, int timeTo, IntByReference orderCount);

    /**
     * 获取历史交易订单信息
     *
     * @param clientId 通信实例id（in）
     * @param orders 所有订单信息（out）
     * @param count 订单个数（in）
     * @return 成功：true，失败：false
     *
     * 用法：先调用MT4API_GetTradeHistoryCount获取个数，在调用此接口获取具体信息
     */
    boolean  MT4API_DownloadTradeHistory(int clientId, TradeRecord[] orders, IntByReference count);

    /**
     * 订单信息结构体
     */
    class TradeRecord extends Structure {
        public static class ByReference extends TradeRecord implements Structure.ByReference{};
        public static class ByValue extends TradeRecord implements  Structure.ByValue{};

        public TradeRecord() {
        }

        public TradeRecord(Pointer pointer) {
            super(pointer);
        }

        public int order;
        public int login;
        public byte[] symbol = new byte[12];
        public int digits;
        public int cmd;
        public int volume;
        public int open_time;
        public int state;
        public double open_price;
        public double stoploss;
        public double takeprofit;
        public int close_time;
        public int gw_volume;
        public int expiration;
        public byte reason;
        public byte[] conv_reserv = new byte[3];
        public double[] conv_rates = new double[2];

        public double commission;
        public double commission_agent;
        public double storage;
        public double close_price;
        public double profit;
        public double taxes;
        public int magic;
        public byte[] comment = new byte[32];
        public int gw_order;
        public int activation;
        public short gw_open_price;
        public short gw_close_price;
        public double margin_rate;
        public int timestamp;
        public int[] api_data = new int[4];

        public ByReference next;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[] {"order", "login", "symbol", "digits", "cmd",
                    "volume", "open_time", "state", "open_price", "stoploss",
                    "takeprofit", "close_time", "gw_volume", "expiration", "reason",
                    "conv_reserv", "conv_rates", "commission", "commission_agent", "storage",
                    "close_price", "profit", "taxes", "magic", "comment",
                    "gw_order", "activation", "gw_open_price", "gw_close_price", "margin_rate",
                    "timestamp", "api_data", "next"});
        }
    }
}
