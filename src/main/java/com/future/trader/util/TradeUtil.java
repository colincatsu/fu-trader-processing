package com.future.trader.util;

import com.future.trader.api.ConnectLibrary;
import com.future.trader.api.InstanceLibrary;
import com.future.trader.api.OrderLibrary;
import com.future.trader.bean.TradeRecordInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 交易公共工具类
 *
 * @author Admin
 * @version: 1.0
 */
public class TradeUtil {

    static Logger log= LoggerFactory.getLogger(TradeUtil.class);

    /**
     * 打印错误日志
     * @param clientId
     */
    public static void printError(int clientId) {
        byte[] errorDesc = new byte[1024];
        InstanceLibrary.library.MT4API_GetLastErrorDescription(clientId, errorDesc, 1024, 0);
        log.error(new String(errorDesc));
    }

    /**
     * 获取连接
     * @param brokerName
     * @param username
     * @param password
     * @return
     */
    public static int getUserConnect(String brokerName,int username,String password){
        int clientId = InstanceLibrary.library.MT4API_Create();
        log.info("clientId : " + clientId);
        if (clientId > 0) {
            String srvName =    "srv\\"+brokerName+".srv";
            boolean init = ConnectLibrary.library.MT4API_InitBySrvFile(clientId,srvName,username,password,
                    0,0);
            log.info("MT4API_Init : " + init);
            if (init) {
                boolean login = ConnectLibrary.library.MT4API_Connect(clientId);
                log.info("MT4API_Connect login : " + login);
                if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
                    log.info("connect borker success!");
                    return clientId;
                }
            }
            printError(clientId);
        }
        return 0;
    }

    /**
     * 获取连接
     * @param brokerName
     * @param username
     * @param password
     * @param nThreadHisTimeFrom 初始获取历史订单的开始时间
     * @param nThreadHisTimeTo   初始获取历史订单的结束时间
     * @return
     */
    public static int getUserConnect(String brokerName,int username,String password,int nThreadHisTimeFrom,int nThreadHisTimeTo){
        int clientId = InstanceLibrary.library.MT4API_Create();
        log.info("clientId : " + clientId);
        if (clientId > 0) {
            String srvName =    "srv\\"+brokerName+".srv";
            boolean init = ConnectLibrary.library.MT4API_InitBySrvFile(clientId,srvName,username,password,
                    nThreadHisTimeFrom,nThreadHisTimeTo);
            log.info("MT4API_Init : " + init);
            if (init) {
                boolean login = ConnectLibrary.library.MT4API_Connect(clientId);
                log.info("MT4API_Connect login : " + login);
                if (ConnectLibrary.library.MT4API_IsConnect(clientId)) {
                    log.info("connect borker success!");
                    return clientId;
                }
            }
            printError(clientId);
        }
        return 0;
    }

    /**
     * 转换订单
     * @return
     */
    public static TradeRecordInfo convertTradeRecords(OrderLibrary.TradeRecord record){
        if(record==null){
            return null;
        }
        TradeRecordInfo info=new TradeRecordInfo();
        info.setComment(new String(record.comment).trim());
        info.setSymbol(new String(record.symbol).trim());
        info.setConv_reserv(new String(record.conv_reserv).trim());

        info.setOrder(record.order);
        info.setLogin(record.login);
        info.setDigits(record.digits);
        info.setCmd(record.cmd);
        info.setVolume(record.volume);
        info.setState(record.state);
        info.setOpen_time(record.open_time);
        info.setOpen_price(record.open_price);
        info.setClose_time(record.close_time);
        info.setClose_price(record.close_price);

        info.setStoploss(record.stoploss);
        info.setTakeprofit(record.takeprofit);
        info.setStorage(record.storage);
        info.setProfit(record.profit);
        info.setTaxes(record.taxes);
        info.setMagic(record.magic);
        info.setReason(record.reason);
        info.setCommission(record.commission);
        info.setCommission_agent(record.commission_agent);

        info.setConv_rates(record.conv_rates);
        info.setActivation(record.activation);
        info.setGw_open_price(record.gw_open_price);
        info.setGw_volume(record.gw_volume);
        info.setGw_order(record.gw_order);
        info.setMargin_rate(record.margin_rate);
        info.setTimestamp(record.timestamp);
        info.setApi_data(record.api_data);

        return info;
    }
}