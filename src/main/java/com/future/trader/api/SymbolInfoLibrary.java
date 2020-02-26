package com.future.trader.api;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

/**
 * 证劵信息接口
 */
public interface SymbolInfoLibrary extends Library {

    /**
     * 加载动态库文件
     */
    SymbolInfoLibrary library = Native.load("mt4api", SymbolInfoLibrary.class);

    /**
     * 获取证劵信息
     *
     * @param clientId 通信实例id（in）
     * @param symbolName 证劵名称（in）
     * @param symbol 证劵信息存储结构体（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetSymbolInfo(int clientId, String symbolName, SymbolInfo.ByReference symbol);

    /**
     * 获取当前服务端支持的证劵个数
     *
     * @param clientId 通信实例id（in）
     * @param symbolCount 证劵个数（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetSymbolsCount(int clientId, IntByReference symbolCount);

    boolean MT4API_GetSymbolsInfo();

    boolean MT4API_GetSymbolGroups();

    boolean MT4API_GetSymbolGroupParams();

    /**
     * 证劵是否订阅
     *
     * @param clientId 通信实例id（in）
     * @param symbolName 证劵名称
     * @return true：已订阅，false，未订阅
     */
    boolean MT4API_IsSubscribe(int clientId, String symbolName);

    /**
     * 订阅证劵报价
     *
     * @param clientId 通信实例id（in）
     * @param symbolName 证劵名称（in）
     * @return 成功：true，失败：false
     */
    boolean MT4API_Subscribe(int clientId, String symbolName);

    /**
     * 取消证劵报价订阅
     *
     * @param clientId 通信实例id（in）
     * @param symbolName 证劵名称（in）
     * @return 成功：true，失败：false
     */
    boolean MT4API_UnSubscribe(int clientId, String symbolName);

    /**
     * 证劵信息结构体
     */
    class SymbolInfo extends Structure {
        public static class ByReference extends SymbolInfo implements Structure.ByReference{};
        public static class ByValue extends SymbolInfo implements Structure.ByValue{};

        public SymbolInfo() {
            symbol = new byte[12];
            description = new byte[64];
            source = new byte[12];
            currency = new byte[12];
            external_unused = new int[7];
            sessions = new ConSessions.ByReference[7];
            margin_currency = new byte[12];
            unused = new int[21];
        }

        /**
         * common settings
         */
        public byte[] symbol;
        public byte[] description;
        public byte[] source;
        public byte[] currency;
        public int type;
        public int digits;
        public int trade;

        /**
         * external settings
         */
        public int background_color;
        public int count;
        public int count_original;
        public int[] external_unused;

        /**
         * sessions
         */
        public int realtime;
        public int starting;
        public int expiration;
        public ConSessions.ByReference[] sessions;

        /**
         * profits
         */
        public int profit_mode;
        public int profit_reserved;

        /**
         * filtration
         */
        public int filter;
        public int filter_counter;
        public double filter_limit;
        public int filter_smoothing;
        public float filter_reserved;
        public int logging;

        /**
         * spread & swaps
         */
        public int spread;
        public int spread_balance;
        public int exemode;
        public int swap_enable;
        public int swap_type;
        public double swap_long;
        public double swap_short;
        public int swap_rollover3days;
        public double contract_size;
        public double tick_value;
        public double tick_size;
        public int stops_level;
        public int gtc_pendings;

        /**
         * margin calculation
         */
        public int margin_mode;
        public double margin_initial;
        public double margin_maintenance;
        public double margin_hedged;
        public double margin_divider;

        /**
         * calclulated variables(internal data)
         */
        public double point;
        public double multiply;
        public double bid_tickvalue;
        public double ask_tickvalue;

        public int long_only;
        public int instant_max_volume;

        public byte[] margin_currency;
        public int freeze_level;
        public int margin_hedged_strong;
        public int value_date;
        public int quotes_delay;
        public int swap_openprice;
        public int swap_variation_margin;

        public int[] unused;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[] {"symbol", "description", "source", "currency", "type",
                    "digits", "trade", "background_color", "count", "count_original",
                    "external_unused", "realtime", "starting", "expiration", "sessions",
                    "profit_mode", "profit_reserved", "filter", "filter_counter", "filter_limit",
                    "filter_smoothing", "filter_reserved", "logging", "spread", "spread_balance",
                    "exemode", "swap_enable", "swap_type", "swap_long", "swap_short",
                    "swap_rollover3days", "contract_size", "tick_value", "tick_size", "stops_level",
                    "gtc_pendings", "margin_mode", "margin_initial", "margin_maintenance", "margin_hedged",
                    "margin_divider", "point", "multiply", "bid_tickvalue", "ask_tickvalue",
                    "long_only", "instant_max_volume", "margin_currency", "freeze_level", "margin_hedged_strong",
                    "value_date", "quotes_delay", "swap_openprice", "swap_variation_margin", "unused"});
        }
    }

    class ConSessions extends Structure {
        public static class ByReference extends ConSessions implements Structure.ByReference{};
        public static class ByValue extends ConSessions implements Structure.ByValue{};

        public ConSessions(){
            quote = new ConSession.ByReference[3];
            trade = new ConSession.ByReference[3];
            reserved = new int[2];
        }

        public ConSession.ByReference[] quote;
        public ConSession.ByReference[] trade;
        public int quote_overnight;
        public int trade_overnight;
        public int[] reserved;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[] {"quote", "trade", "quote_overnight", "trade_overnight", "reserved"});
        }
    }

    class ConSession extends Structure {
        public static class ByReference extends ConSession implements Structure.ByReference{};
        public static class ByValue extends  ConSession implements Structure.ByValue{};

        public ConSession(){
            align = new short[7];
        }

        public short open_hour;
        public short open_min;
        public short close_hour;
        public short close_min;
        public int open;
        public int close;
        public short[] align;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[] {"open_hour", "open_min", "close_hour", "close_min",
                    "open", "close", "align"});
        }
    }
}
