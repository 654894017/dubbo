package com.alibaba.dubbo.rpc.protocol.parrot.util;

import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

public class ParrotConstants {

    // 是否debug的maker常量
    public static final Marker parrot_debug_marker = new BasicMarkerFactory().getMarker("parrot_debug");
    // 上下文message.name
    public final static String parrotContext_make = "$$$_1_PC$$$$$$$$__1_&_8_#";
    // 心跳检测message.name
    public static final String heartbeat_make = "#$%Heartbeat";
    // 接入配置系统server配置key
    public static final String CLIENT_SERVERS = "client.servers";

    public static final String WHITE_IP_LIST = "server.white.ip";

    // server配置系统开关
    public static final String SERVER_WHITEIP_SWITCH = "server.whiteip-switch";

    // 配置系统回调失败尝试次数
    public static final int CONFIG_EXCUTE_COUNT = 10;
    
    public static final String SERVICEANDMETHODNAME ="qualifiedMethodName";
    
    public static final String PARROTCONTEXT ="parrotContext";
    
    public static final String TMESSAGE ="tmessage";
    
    public enum ActiveStatus {
        ABLE(3, true), UNABLE(2, false), WAITCHECK(1, false);

        private final Integer priority;
        private final boolean isAvailable;

        ActiveStatus(int priority, boolean isAvailable) {
            this.priority = priority;
            this.isAvailable = isAvailable;
        }

        public Integer getPriority() {
            return priority;
        }

        public boolean isAvailable() {
            return isAvailable;
        }

    }

    public enum ConnStrategy {
        WEIGHT("weight"), REQUESTNUM("requestnum");

        private final String strategy;

        ConnStrategy(String strategy) {
            this.strategy = strategy;
        }

        public String getStrategy() {
            return strategy;
        }
    }
}
