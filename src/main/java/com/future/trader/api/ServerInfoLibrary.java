package com.future.trader.api;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

import java.util.Arrays;
import java.util.List;

/**
 * 服务器信息接口
 */
public interface ServerInfoLibrary extends Library {

    /**
     * 加载动态库文件
     */
    ServerInfoLibrary library = Native.load("mt4api", ServerInfoLibrary.class);

    /**
     * 获取服务器时间
     *
     * @param clientId 通信实例id（in）
     * @param time 服务器时间（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetServerTime(int clientId, IntByReference time);

    /**
     * 获取服务端版本号
     *
     * @param clientId 通信实例id（in）
     * @param serverBuild 服务端版本号（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetServerBuild(int clientId, ShortByReference serverBuild);

    /**
     * 获取服务器列表信息个数
     *
     * @param clientId 通信实例id（in）
     * @param serverInfoCount 服务器列表信息个数（out）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetSeverInfoCount(int clientId, IntByReference serverInfoCount);

    /**
     * 获取服务器列表信息
     *
     * @param clientId 通信实例id（in）
     * @param serverInfos 服务器列表信息（out）
     * @param count 服务器列表信息个数（int）
     * @return 成功：true，失败：false
     */
    boolean MT4API_GetSeverInfos(int clientId, ServerInfo[] serverInfos, IntByReference count);

    /**
     * 服务器信息结构体
     */
    class ServerInfo extends Structure {
        public static class ByReference extends ServerInfo implements Structure.ByReference{};

        public static class ByValue extends ServerInfo implements Structure.ByValue{};

        public ServerInfo() {
        }

        public ServerInfo(Pointer pointer) {
            super(pointer);
        }

        public byte[] server = new byte[64];
        public long ip;
        public byte[] description = new byte[64];
        public int isproxy;
        public int priority;
        public int loading;
        public long ip_internal;
        public int[] reserved = new int[2];
        public ByReference next;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[] {"server", "ip", "description", "isproxy", "priority",
                    "loading", "ip_internal", "reserved", "next"});
        }
    }

}
