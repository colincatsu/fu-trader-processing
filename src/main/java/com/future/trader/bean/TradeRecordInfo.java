package com.future.trader.bean;

import lombok.Data;

@Data
public class TradeRecordInfo{

        // order ticket
        public int order;
        // owner's login
        public int login;
        // security
        public String symbol;
        // security precision
        public int digits;
        // trade command
        public int cmd;
        // volume
        public int volume;
        // open time
        public int open_time;
        // reserved
        public int state;
        // open price
        public double open_price;
        // stop loss & take profit
        public double stoploss;
        public double takeprofit;
        // close time
        public int close_time;
        // gateway order volume
        public int gw_volume;
        // pending order's expiration time
        public int expiration;
        // trade reason
        public byte reason;
        // reserved fields
        public String conv_reserv;
        // convertation rates from profit currency to group deposit currency
        public double[] conv_rates = new double[2];
        // (first element-for open time, second element-for close time)
        // commission
        public double commission;
        // agent commission
        public double commission_agent;
        // order swaps
        public double storage;
        // close price
        public double close_price;
        // profit
        public double profit;
        // taxes
        public double taxes;
        // special value used by client experts
        public int magic;
        // comment
        public String comment;
        // gateway order ticket
        public int gw_order;
        // used by MT Manager
        public int activation;
        // gateway order price deviation (pips) from order open price
        public short gw_open_price;
        // gateway order price deviation (pips) from order close price
        public short gw_close_price;
        // margin convertation rate (rate of convertation from margin currency to deposit one)
        public double margin_rate;
        // timestamp
        public int timestamp;
        // for api usage
        public int[] api_data = new int[4];
    }