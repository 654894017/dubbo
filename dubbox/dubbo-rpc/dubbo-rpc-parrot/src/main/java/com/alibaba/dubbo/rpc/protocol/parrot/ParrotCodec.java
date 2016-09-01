package com.alibaba.dubbo.rpc.protocol.parrot;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TIOStreamTransport;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferInputStream;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.parrot.io.RandomAccessByteArrayOutputStream;
import com.alibaba.dubbo.rpc.protocol.parrot.service.ParrotClientMetadata;
import com.alibaba.dubbo.rpc.protocol.parrot.service.ParrotCodecManager;
import com.alibaba.dubbo.rpc.protocol.parrot.service.ParrotMethodHandler;
import com.alibaba.dubbo.rpc.protocol.parrot.service.ParrotMethodProcessor;
import com.alibaba.dubbo.rpc.protocol.parrot.service.ParrotServerProcessor;
import com.alibaba.dubbo.rpc.protocol.parrot.util.DefaultParrotContext;
import com.alibaba.dubbo.rpc.protocol.parrot.util.DefaultParrotContextUtil;
import com.alibaba.dubbo.rpc.protocol.parrot.util.HeartBeatUtil;
import com.alibaba.dubbo.rpc.protocol.parrot.util.ParrotConstants;
import com.facebook.swift.codec.ThriftCodecManager;
import com.google.common.base.Preconditions;
/**
 * 
 * <B>Description</B>  * parrot framed protocol codec.
 *
 * <pre>
 * |<   message header  ->|<- message body ->|
 * +----------------------+------------------+
 * |message size (4 bytes)|   message body   |
 * +----------------------+------------------+
 * |<-               message size          ->| <br />
 * <B>Copyright</B> Copyright (c) 2016 www.diligrp.com All rights reserved. <br />
 * 本软件源代码版权归聚美优品,未经许可不得任意复制与传播.<br />
 * <B>Company</B> 聚美优品
 * @createTime 2016年8月19日 下午5:10:26
 * @author lushiwei
 */
public class ParrotCodec implements Codec2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParrotCodec.class);
    private static final AtomicInteger THRIFT_SEQ_ID = new AtomicInteger(0);
    // 客户端使用
    private static Map<Long, ParrotMethodHandler> mapMethodHandler = new java.util.concurrent.ConcurrentHashMap<Long, ParrotMethodHandler>();
    // 客户端使用
    private static Map<String, ParrotClientMetadata> mapParrotClientMetadata = new java.util.concurrent.ConcurrentHashMap<String, ParrotClientMetadata>();

    private static final int MESSAGE_SHORTEST_LENGTH = 4;
    //提前加载
    private ThriftCodecManager codecManager = ParrotCodecManager.newBuilder().build();
    /**
     * swift编码
     */
    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {
        if (message instanceof Request) {
            try {
                encodeRequest(channel, buffer, (Request) message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (message instanceof Response) {
            encodeResponse(channel, buffer, (Response) message);
        } else {
            throw new UnsupportedOperationException(new StringBuilder(32).append("Parrot codec only support encode ").append(Request.class.getName()).append(" and ").append(Response.class.getName())
                    .toString());
        }
    }
    /**
     * swift解码
     */
    @Override
    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {
        int available = buffer.readableBytes();
        
        if (buffer.readableBytes() < MESSAGE_SHORTEST_LENGTH) {
            return DecodeResult.NEED_MORE_INPUT;
        } else {
            byte[] bytes = new byte[MESSAGE_SHORTEST_LENGTH];
            buffer.readBytes(bytes);
            int msgLenth = TFramedTransport.decodeFrameSize(bytes);
            if(buffer.readableBytes()<(msgLenth)){
                return DecodeResult.NEED_MORE_INPUT; 
            }
            int sequenceId = 0;
            TMessage message = null;
            try {
                TIOStreamTransport transport = new TIOStreamTransport(new ChannelBufferInputStream(buffer));

                TBinaryProtocol protocol = new TBinaryProtocol(transport);
               
                // 反序列化context
                DefaultParrotContext context = DefaultParrotContext.getContext();
                // 尝试 read context
                DefaultParrotContextUtil.decodeContext(context, protocol);
                // 反序列化argument

                message = protocol.readMessageBegin();
                sequenceId = message.seqid;
                Object obj = null;
                if (message.type == TMessageType.CALL) {
                    //处理parrot心跳请求
                    if(message.name.equals(ParrotConstants.heartbeat_make)){
                        return HeartBeatUtil.parrotHeartBeat2Request(buffer,context, message);
                    }
                    obj = parseCall(message, context, protocol);
                } else {
                    obj = parseReply(message, context, protocol);
                }
                protocol.readMessageEnd();
                return obj;
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
                throw new IOException(e.getMessage());
            } finally {
                // 删除methodhandler
                if (message.type == TMessageType.REPLY) {
                    mapMethodHandler.remove(new Long(sequenceId));
                }
            }

        }
        
    }
    /**
     * 
     * this method is swift解码服务端返回数据包
     * @param message
     * @param context
     * @param protocol
     * @return
     * @throws Exception
     * @createTime 2016年8月19日 下午5:15:02
     * @author lushiwei
     */
    private Object parseReply(TMessage message, DefaultParrotContext context, TBinaryProtocol protocol) throws Exception {
        // id映射
        try{
            String responseId = context.getSystemContexts().get(DefaultParrotContext.REQUESTID);
            ParrotMethodHandler handler = mapMethodHandler.get(new Long(message.seqid));
            Response response = new Response();
            response.setId(Long.valueOf(responseId));

            RpcResult rpcResult = new RpcResult();
            if(message.type == TMessageType.REPLY){
                Object object = handler.readResponse(protocol);  
                rpcResult.setValue(object);
            }else if(message.type == TMessageType.EXCEPTION){
                TApplicationException exception = TApplicationException.read(protocol);
                protocol.readMessageEnd();
                rpcResult.setException(exception);
            }
            response.setResult(rpcResult);

            return response;
        }finally{
        }
       

    }
    /**
     * 
     * this method is 解码客户端请求数据包
     * @param message
     * @param context
     * @param protocol
     * @return
     * @throws Exception
     * @createTime 2016年8月19日 下午5:15:43
     * @author lushiwei
     */
    private Request parseCall(TMessage message, DefaultParrotContext context, TBinaryProtocol protocol) throws Exception {
        int sequenceId = 0;
        String methodName = message.name;
        String qualifiedMethodName = context.getContext(DefaultParrotContext.SERVER_NAME) == null ? "" : context.getContext(DefaultParrotContext.SERVER_NAME);
        qualifiedMethodName = qualifiedMethodName + "." + methodName;
        sequenceId = message.seqid;
        ParrotServerProcessor parrotServerProcessor = ParrotServerProcessor.getInstance();
        ParrotMethodProcessor methodProcessor = parrotServerProcessor.getQualifiedMethods().get(qualifiedMethodName);
        if (methodProcessor == null) {
            try {
                throw new TApplicationException("Invalid method name: '" + methodName + "'");
            } catch (TApplicationException e) {
                LOGGER.error(e);
            }
        }
        Object[] args = null;
        args = methodProcessor.readArguments(protocol);

        protocol.readMessageEnd();
        RpcInvocation result = new RpcInvocation();
        result.setAttachment(Constants.INTERFACE_KEY, methodProcessor.getServiceClass().getName());
        result.setMethodName(message.name);
        result.setArguments(args);
        result.setParameterTypes(methodProcessor.getParameterType());
        //Id版本兼容处理
        Request request = null;
        String requestId = context.getContext(DefaultParrotContext.REQUESTID);
        //非dubbo客户端
        if(requestId == null || "".equals(requestId.trim())){
            request = new Request(sequenceId);
        }else{
            request = new Request(new Long(requestId));
        }
        request.getRequestParams().put(ParrotConstants.SERVICEANDMETHODNAME, qualifiedMethodName);
        request.getRequestParams().put(ParrotConstants.PARROTCONTEXT, context);
        request.getRequestParams().put(ParrotConstants.TMESSAGE, message);
        request.setData(result);
        return request;
    }
    /**
     * 
     * this method is swift 编码请求数据包
     * @param channel
     * @param buffer
     * @param request
     * @throws Exception
     * @createTime 2016年8月19日 下午5:16:09
     * @author lushiwei
     */
    private void encodeRequest(Channel channel, ChannelBuffer buffer, Request request) throws Exception {
        RpcInvocation inv = (RpcInvocation) request.getData();

        int seqId = nextSeqId();
        //TODO dubbo作为客户端调研其它服务时需要做注解到接口的转换
        String serviceName = inv.getAttachment(Constants.INTERFACE_KEY);
        if (StringUtils.isEmpty(serviceName)) {
            throw new IllegalArgumentException(new StringBuilder(32).append("Could not find service name in attachment with key ").append(Constants.INTERFACE_KEY).toString());
        }
        Class serviceClass = null;
        try {
            serviceClass = this.getClass().getClassLoader().loadClass(serviceName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Preconditions.checkNotNull(serviceClass, "interface is null");

        RandomAccessByteArrayOutputStream bos = new RandomAccessByteArrayOutputStream(1024);

        TIOStreamTransport transport = new TIOStreamTransport(bos);

        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        //双重验证
        ParrotClientMetadata parrotClientMetadata = mapParrotClientMetadata.get(serviceName);
        if (parrotClientMetadata == null) {
            synchronized (mapParrotClientMetadata) {
                parrotClientMetadata = mapParrotClientMetadata.get(serviceName);
                if (parrotClientMetadata == null) {
                    parrotClientMetadata = new ParrotClientMetadata(serviceClass, codecManager);
                    mapParrotClientMetadata.put(serviceName, parrotClientMetadata);
                }
            }
        }
        Method method = MethodUtils.getMatchingAccessibleMethod(serviceClass, inv.getMethodName(), inv.getParameterTypes());
        ParrotMethodHandler parrotMethodHandler = parrotClientMetadata.getMethodHandlers().get(method);
        // 序列化context
        DefaultParrotContext context = DefaultParrotContext.getContext();
        DefaultParrotContextUtil.putSystemContext(context, DefaultParrotContext.SERVER_NAME, parrotClientMetadata.getThriftServiceMetadata().getName());
        DefaultParrotContextUtil.putSystemContext(context, DefaultParrotContext.METHOD_NAME, parrotMethodHandler.getName());
        DefaultParrotContextUtil.putSystemContext(context, DefaultParrotContext.REQUESTID, String.valueOf(request.getId()));
        try {
            DefaultParrotContextUtil.encodeContext(context, protocol);
        } catch (TException e) {
            
            e.printStackTrace();
            throw new RpcException("swift serialize DefaultParrotContext error:" + e.getMessage());
        }
        // 序列化参数

        try {
            parrotMethodHandler.writeArguments2Buffer(protocol, seqId, inv.getArguments());
        } catch (Exception e) {

            e.printStackTrace();
            throw new RpcException("swift serialize Arguments error:" + e.getMessage());
        }

        byte[] bytes = new byte[4];
        TFramedTransport.encodeFrameSize(bos.size(), bytes);
        buffer.writeBytes(bytes);
        buffer.writeBytes(bos.toByteArray());
        mapMethodHandler.put(new Long(seqId), parrotMethodHandler);
    }
    /**
     * 
     * this method is swift 编码服务端返回数据包
     * @param channel
     * @param buffer
     * @param response
     * @throws IOException
     * @createTime 2016年8月19日 下午5:16:37
     * @author lushiwei
     */
    private void encodeResponse(Channel channel, ChannelBuffer buffer, Response response) throws IOException {
        try {
            RandomAccessByteArrayOutputStream bos = new RandomAccessByteArrayOutputStream(1024);
            TIOStreamTransport transport = new TIOStreamTransport(bos);

            TProtocol protocol = new TBinaryProtocol(transport);
            if(response.isEvent()){
                HeartBeatUtil.response2ParrotHeartBeat(response, protocol);
            }else{
                RpcResult result = (RpcResult) response.getResult();
              
                String qualifiedMethodName = (String)response.getRequest().getRequestParams().get(ParrotConstants.SERVICEANDMETHODNAME);
                if(qualifiedMethodName == null || "".equals(qualifiedMethodName)){
                    throw new RpcException("获取qualifiedMethodName失败！");
                }
                ParrotServerProcessor parrotServerProcessor = ParrotServerProcessor.getInstance();
                ParrotMethodProcessor methodProcessor = parrotServerProcessor.getQualifiedMethods().get(qualifiedMethodName);
                 DefaultParrotContext parrotContext = (DefaultParrotContext)response.getRequest().getRequestParams().get(ParrotConstants.PARROTCONTEXT);
                 if(parrotContext == null){
                     throw new RpcException("获取parrotContext失败！");
                 }
                DefaultParrotContextUtil.encodeContext(parrotContext, protocol);
                TMessage message = (TMessage) response.getRequest().getRequestParams().get(ParrotConstants.TMESSAGE);
                if (result.getException() == null) {
                    methodProcessor.writeResponse(protocol, message.seqid, TMessageType.REPLY, "success", result.getValue());
                } else {
                    methodProcessor.writeException(result.getException(), protocol, message.seqid);
                }
              
            }
            byte[] bytes = new byte[4];
            TFramedTransport.encodeFrameSize(bos.size(), bytes);
            buffer.writeBytes(bytes);
            buffer.writeBytes(bos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            
        }
    }

    private static int nextSeqId() {
        return THRIFT_SEQ_ID.incrementAndGet();
    }

    static int getSeqId() {
        return THRIFT_SEQ_ID.get();
    }

    static class RequestData {

        int id;
        String serviceName;
        String methodName;

        static RequestData create(int id, String sn, String mn) {
            RequestData result = new RequestData();
            result.id = id;
            result.serviceName = sn;
            result.methodName = mn;
            return result;
        }

    }

    protected static org.jboss.netty.buffer.ChannelBuffer extractFrame(org.jboss.netty.buffer.ChannelBuffer buffer, int index, int length) {
        return buffer.slice(index, length);
    }

}
