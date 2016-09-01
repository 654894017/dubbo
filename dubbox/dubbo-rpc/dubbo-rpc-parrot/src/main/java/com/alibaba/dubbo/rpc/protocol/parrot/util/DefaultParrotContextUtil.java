package com.alibaba.dubbo.rpc.protocol.parrot.util;

import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;

public class DefaultParrotContextUtil {

    public static void encodeContext(DefaultParrotContext defaultParrotContext, TProtocol protocol) throws TException {
        /*
         * TChannelBufferOutputTransport transport=new
         * TChannelBufferOutputTransport(); TProtocol outputProtocol =new
         * TBinaryProtocol(transport, true, true);
         */
        writerContext(protocol, defaultParrotContext);
        /* return transport.getOutputBuffer(); */
    }

    public static boolean decodeContext(DefaultParrotContext defaultParrotContext, TProtocol protocol) throws Exception {
        boolean isOK = readContext(protocol, defaultParrotContext);
        return isOK;
    }

    private static void writerContext(TProtocol protocol, DefaultParrotContext defaultParrotContext) throws TException {
        // TODO Auto-generated method stub
        protocol.writeMessageBegin(new org.apache.thrift.protocol.TMessage(ParrotConstants.parrotContext_make, org.apache.thrift.protocol.TMessageType.CALL, 0));
        Map<String, String> contexts = defaultParrotContext.getSystemContexts();
        protocol.writeMapBegin(new TMap(TType.STRING, TType.STRING, contexts.size()));
        for (Map.Entry<String, String> entry : contexts.entrySet()) {
            protocol.writeString(entry.getKey());
            protocol.writeString(entry.getValue());
        }
        protocol.writeMapEnd();
        protocol.writeMessageEnd();
        protocol.getTransport().flush();
    }

    private static boolean readContext(TProtocol protocol, DefaultParrotContext defaultParrotContext) throws TException {
        // TODO Auto-generated method stub
        TMessage tmessage = protocol.readMessageBegin();
        if (!tmessage.name.equals(ParrotConstants.parrotContext_make))
            return false;
        TMap tMap = protocol.readMapBegin();
        Map<String, String> contexts = defaultParrotContext.getSystemContexts();
        for (int i = 0; i < tMap.size; i++) {
            String key = protocol.readString();
            String value = protocol.readString();
            contexts.put(key, value);
        }
        protocol.readMapEnd();
        protocol.readMessageEnd();
        return true;
    }

    public static DefaultParrotContext copyContext(DefaultParrotContext defaultParrotContext) throws TException {
        // TODO Auto-generated method stub

        DefaultParrotContext pt = new DefaultParrotContext();
        Map<String, String> contexts = defaultParrotContext.getSystemContexts();
        for (String key : contexts.keySet()) {
            pt.putSystemContext(key, contexts.get(key));
        }
        return pt;
    }

    public static void copyContext(DefaultParrotContext fromParrotContext, DefaultParrotContext toParrotContext, boolean isClear) throws TException {
        // TODO Auto-generated method stub
        if (isClear) {
            toParrotContext.getSystemContexts().clear();
        }
        toParrotContext.setChannel(fromParrotContext.getChannel());
        Map<String, String> contexts = fromParrotContext.getSystemContexts();
        for (String key : contexts.keySet()) {
            toParrotContext.putSystemContext(key, contexts.get(key));
        }
    }

    public static boolean putSystemContext(DefaultParrotContext defaultParrotContext, String key, String value) {
        return defaultParrotContext.putSystemContext(key, value);
    }
}
