package com.future.trader.api;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * 实例创建销毁和错误获取接口
 */
public interface InstanceLibrary extends Library {

    /**
     * 加载动态库文件
     */
    InstanceLibrary library = Native.load("mt4api", InstanceLibrary.class);

    /**
     * 创建客户端通信实例
     *
     * @return 通讯实例id，成功时返回大于0的值，失败返回-1
     */
    int MT4API_Create();

    /**
     * 关闭通信实例
     *
     * @param clientId 通讯实例id
     */
    void MT4API_Destory(int clientId);

    /**
     * 获取错误号
     *
     * @param clientId 通信实例id
     * @param position 错误号位置（0，取最后一个错误号，-1，倒数第二个错误号，依次类推）
     * @return 错误号
     */
    int MT4API_GetLastError(int clientId, int position);

    /**
     * 获取指定错误号的错误描述
     *
     * @param errorCode 错误号（in）
     * @param errorDesc 错误描述，一般分配256个字符以上（out）
     * @param size errorDesc低配的空间大小（in）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetErrorDescription(int errorCode, byte[] errorDesc, int size);

    /**
     * 获取指定位置的错误号的错误描述
     *
     * @param clientId 通信实例id（in）
     * @param errorDesc 错误描述，一般分配256个字符以上（out）
     * @param size errorDesc低配的空间大小（in）
     * @param pos 错误号位置（0，取最后一个错误号；-1，取倒数第二个错误号，依次类推）（in）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetLastErrorDescription(int clientId, byte[] errorDesc, int size, int pos);
}
