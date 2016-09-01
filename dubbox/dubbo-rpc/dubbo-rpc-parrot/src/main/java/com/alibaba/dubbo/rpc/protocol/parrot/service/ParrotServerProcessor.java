package com.alibaba.dubbo.rpc.protocol.parrot.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.rpc.protocol.parrot.util.DefaultParrotContext;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.service.metadata.ThriftMethodMetadata;
import com.facebook.swift.service.metadata.ThriftServiceMetadata;
import com.google.common.base.Preconditions;

/**
 * 
 * <B>Description</B> TODO <br />
 * <B>Copyright</B> Copyright (c) 2016 www.diligrp.com All rights reserved. <br />
 * 本软件源代码版权归聚美优品,未经许可不得任意复制与传播.<br />
 * <B>Company</B> 聚美优品
 * @createTime 2016年8月19日 下午5:20:02
 * @author lushiwei
 */
public class ParrotServerProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ParrotServerProcessor.class);
    private Map<String, ParrotMethodProcessor> qualifiedMethods = new ConcurrentHashMap<>();;
    private ThriftCodecManager thriftCodecManager;
    private static ParrotServerProcessor parrotServerProcessor = new ParrotServerProcessor();

    private ParrotServerProcessor() {
        thriftCodecManager = ParrotCodecManager.newBuilder().build();
    }

    public static ParrotServerProcessor getInstance() {
        return parrotServerProcessor;
    }

    public void addService(List<Class> services) {
        Preconditions.checkNotNull(services, "service is null");
        Preconditions.checkArgument(!services.isEmpty(), "services is empty");

        // NOTE: ImmutableMap enforces that we don't have duplicate method names
        // ImmutableMap.Builder<String, ThriftMethodProcessor> processorBuilder
        // = ImmutableMap.builder();
        // ImmutableMap.Builder<String, ThriftMethodProcessor>
        // qualifiedProcessorBuilder = ImmutableMap.builder();
        for (Class service : services) {
            ThriftServiceMetadata serviceMetadata = new ThriftServiceMetadata(service, thriftCodecManager.getCatalog());
            for (ThriftMethodMetadata methodMetadata : serviceMetadata.getMethods().values()) {
                ParrotMethodProcessor methodProcessor = new ParrotMethodProcessor(service, serviceMetadata.getName(), methodMetadata, thriftCodecManager);
                // methods.put(methodMetadata.getName(), methodProcessor);
                qualifiedMethods.put(methodMetadata.getQualifiedName(), methodProcessor);
            }
        }

    }

    public Map<String, ParrotMethodProcessor> getQualifiedMethods() {
        return qualifiedMethods;
    }

    public Object[] parseArguments(final TProtocol in, DefaultParrotContext defaultParrotContext) throws Exception {
        TMessage message = in.readMessageBegin();
        String methodName = message.name;
        String qualifiedMethodName = defaultParrotContext.getContext(DefaultParrotContext.SERVER_NAME) == null ? "" : defaultParrotContext.getContext(DefaultParrotContext.SERVER_NAME);
        qualifiedMethodName = qualifiedMethodName + "." + methodName;
        ParrotMethodProcessor method = qualifiedMethods.get(qualifiedMethodName);
        return method.readArguments(in);

    }
}
