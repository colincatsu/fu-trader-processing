package com.future.trader.api;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

/**
 * 行情接口
 */
public interface QuoteLibrary extends Library {

    /**
     * 加载动态库文件
     */
    QuoteLibrary library = Native.load("mt4api", QuoteLibrary.class);

    /**
     * 获取历史行情报价
     *
     * @param clientId 通信实例id（in）
     * @param symbolName 证劵名称（in）
     * @param peroid 周期（参见周期宏定义）（in）
     * @param timeFrom 历史报价起始时间（in）
     * @param count 获取记录数（从最后一条开始计数）（in）
     * @return 成功：true，失败：false
     *
     * 注意：如果从timeFrom时间起，记录数小于count，只获取实际记录数，如果记录数大于count，则获取最新的count条记录
     * 结果通过MT4API_SetQuoteHistoryEventHandler设置的函数回调
     */
    boolean MT4API_DownloadQuoteHistory(int clientId, String symbolName, int peroid,
                                        int timeFrom, short count);

    /**
     * 设置历史行情报价数据回调函数指针
     *
     * @param clientId  通信实例id
     * @param pHandlerr 历史行情报价数据回调函数指针
     * @param param     回传参数
     * @return 成功：true，失败：false
     */
    boolean MT4API_SetQuoteHistoryEventHandler(int clientId, Callback pHandlerr, int param);

    /**
     * 获取行情信息
     *
     * @param clientId 通信实例id（in）
     * @param symbol 证劵名称（in）
     * @param info 最新报价信息（in）
     * @return 成功：true，失败false
     */
    boolean MT4API_GetQuote(int clientId, String symbol, QuoteEventInfo.ByReference info);

    /**
     * 设置证劵行情报价数据回调函数指针
     *
     * @param clientId 通讯实例id
     * @param pHandler 最新行情报价数据回调函数指针
     * @param param    回传参数
     * @return 成功：true，失败：false
     */
    boolean MT4API_SetQuoteEventHandler(int clientId, Callback pHandler, int param);

    /**
     * 获取最后报价时间
     *
     * @param clientId 通信实例id（in）
     * @param lastQuoteTime 最后接收报价时间（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetLastQuoteTime(int clientId, IntByReference lastQuoteTime);

    /**
     * 行情信息数据类型
     */
    class QuoteEventInfo extends Structure {
        public static class ByReference extends QuoteEventInfo implements Structure.ByReference{};

        public byte[] szSymbol = new byte[12];
        public int nCount;
        public int nDigits;
        public int nTime;
        public double fAsk;
        public double fBid;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[] {"szSymbol", "nCount", "nDigits", "nTime", "fAsk", "fBid"});
        }
    }

}
