package com.future.trader.util;

import com.future.trader.api.ConnectLibrary;
import com.future.trader.api.InstanceLibrary;
import com.future.trader.api.OrderLibrary;
import com.future.trader.bean.TradeRecordInfo;
import com.future.trader.common.constants.OrderConstant;
import com.future.trader.service.DisConnectCallback;
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

    /**
     * 校验magic是否合规
     * @param comment
     * @param magic
     * @return
     */
    public static boolean checkMagic(String comment,int magic){
        if(StringUtils.isEmpty(comment)){
            return false;
        }
        String[] followInfo=comment.split(":");
        try {
            int followName=Integer.parseInt(followInfo[0]);
            int orderId=Integer.parseInt(followInfo[1]);
            int newMagic=getMagic(followName,orderId);
            if(magic!=newMagic){
                return false;
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }

    /**
     * 根据跟随账号和信号源订单生成跟随magic
     * @param followName
     * @param orderId
     * @return
     */
    public static int getMagic(int followName,int orderId){
        int magic=OrderConstant.ORDER_FOLLOW_MAGIC>>2|followName<<4&orderId<<3;
        return magic%1000000;
    }
}