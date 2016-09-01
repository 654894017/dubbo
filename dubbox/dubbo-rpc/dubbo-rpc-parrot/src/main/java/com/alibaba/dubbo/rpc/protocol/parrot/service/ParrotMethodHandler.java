package com.alibaba.dubbo.rpc.protocol.parrot.service;

import static org.apache.thrift.protocol.TMessageType.CALL;
import static org.apache.thrift.protocol.TMessageType.ONEWAY;

import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.weakref.jmx.Managed;

import com.facebook.swift.codec.ThriftCodec;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.internal.TProtocolReader;
import com.facebook.swift.codec.internal.TProtocolWriter;
import com.facebook.swift.codec.metadata.ThriftFieldMetadata;
import com.facebook.swift.codec.metadata.ThriftParameterInjection;
import com.facebook.swift.codec.metadata.ThriftType;
import com.facebook.swift.service.metadata.ThriftMethodMetadata;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * 
 * <B>Description</B> TODO <br />
 * <B>Copyright</B> Copyright (c) 2016 www.diligrp.com All rights reserved. <br />
 * 本软件源代码版权归聚美优品,未经许可不得任意复制与传播.<br />
 * <B>Company</B> 聚美优品
 * @createTime 2016年8月19日 下午5:19:31
 * @author lushiwei
 */
@ThreadSafe
public class ParrotMethodHandler {

    private final String name;
    private final String qualifiedName;
    private final List<ParameterHandler> parameterCodecs;
    private final boolean oneway;
    private final ThriftCodec<Object> successCodec;
    private final Map<Short, ThriftCodec<Object>> exceptionCodecs;
    private final boolean invokeAsynchronously;

    public ParrotMethodHandler(ThriftMethodMetadata methodMetadata, ThriftCodecManager codecManager) {
        name = methodMetadata.getName();
        qualifiedName = methodMetadata.getQualifiedName();
        invokeAsynchronously = methodMetadata.isAsync();

        oneway = methodMetadata.getOneway();

        // get the thrift codecs for the parameters
        ParameterHandler[] parameters = new ParameterHandler[methodMetadata.getParameters().size()];
        for (ThriftFieldMetadata fieldMetadata : methodMetadata.getParameters()) {
            ThriftParameterInjection parameter = (ThriftParameterInjection) fieldMetadata.getInjections().get(0);

            ParameterHandler handler = new ParameterHandler(fieldMetadata.getId(), fieldMetadata.getName(), (ThriftCodec<Object>) codecManager.getCodec(fieldMetadata.getType()));

            parameters[parameter.getParameterIndex()] = handler;
        }
        parameterCodecs = ImmutableList.copyOf(parameters);

        // get the thrift codecs for the exceptions
        ImmutableMap.Builder<Short, ThriftCodec<Object>> exceptions = ImmutableMap.builder();
        for (Map.Entry<Short, ThriftType> entry : methodMetadata.getExceptions().entrySet()) {
            exceptions.put(entry.getKey(), (ThriftCodec<Object>) codecManager.getCodec(entry.getValue()));
        }
        exceptionCodecs = exceptions.build();

        // get the thrift codec for the return value
        successCodec = (ThriftCodec<Object>) codecManager.getCodec(methodMetadata.getReturnType());
    }

    public Object readResponse(TProtocol in) throws Exception {
        TProtocolReader reader = new TProtocolReader(in);
        reader.readStructBegin();
        Object results = null;
        Exception exception = null;
        while (reader.nextField()) {
            if (reader.getFieldId() == 0) {
                results = reader.readField(successCodec);
            } else {
                ThriftCodec<Object> exceptionCodec = exceptionCodecs.get(reader.getFieldId());
                if (exceptionCodec != null) {
                    exception = (Exception) reader.readField(exceptionCodec);
                } else {
                    reader.skipFieldData();
                }
            }
        }
        reader.readStructEnd();
        in.readMessageEnd();

        if (exception != null) {
            throw exception;
        }

        // if (successCodec.getType() == ThriftType.VOID) {
        // // TODO: check for non-null return from a void function?
        // return null;
        // }
        //
        // if (results == null) {
        // throw new TApplicationException(TApplicationException.MISSING_RESULT,
        // name + " failed: unknown result");
        // }
        return results;
    }

    @Managed
    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void writeArguments2Buffer(final TProtocol outputProtocol, final int sequenceId, final Object... args) throws Exception {
        writeArguments(outputProtocol, sequenceId, args);
    }

    private void writeArguments(TProtocol out, int sequenceId, Object[] args) throws Exception {
        // Note that though setting message type to ONEWAY can be helpful when
        // looking at packet
        // captures, some clients always send CALL and so servers are forced to
        // rely on the "oneway"
        // attribute on thrift method in the interface definition, rather than
        // checking the message
        // type.
        out.writeMessageBegin(new TMessage(name, oneway ? ONEWAY : CALL, sequenceId));

        // write the parameters
        TProtocolWriter writer = new TProtocolWriter(out);
        writer.writeStructBegin(name + "_args");
        for (int i = 0; i < args.length; i++) {
            Object value = args[i];
            ParameterHandler parameter = parameterCodecs.get(i);
            writer.writeField(parameter.getName(), parameter.getId(), parameter.getCodec(), value);
        }
        writer.writeStructEnd();

        out.writeMessageEnd();
        out.getTransport().flush();
    }

    private static final class ParameterHandler {

        private final short id;
        private final String name;
        private final ThriftCodec<Object> codec;

        private ParameterHandler(short id, String name, ThriftCodec<Object> codec) {
            this.id = id;
            this.name = name;
            this.codec = codec;
        }

        public short getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public ThriftCodec<Object> getCodec() {
            return codec;
        }
    }
}
