package com.alibaba.dubbo.rpc.protocol.parrot.service;

import java.lang.reflect.Method;
import java.util.Map;

import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.service.metadata.ThriftMethodMetadata;
import com.facebook.swift.service.metadata.ThriftServiceMetadata;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * 
 * <B>Description</B> TODO <br />
 * <B>Copyright</B> Copyright (c) 2016 www.diligrp.com All rights reserved. <br />
 * 本软件源代码版权归聚美优品,未经许可不得任意复制与传播.<br />
 * <B>Company</B> 聚美优品
 * @createTime 2016年8月19日 下午5:19:05
 * @author lushiwei
 */
public class ParrotClientMetadata {

    private final String clientType;
    private final ThriftServiceMetadata thriftServiceMetadata;
    private final Map<Method, ParrotMethodHandler> methodHandlers;

    public ParrotClientMetadata(Class<?> clientType, ThriftCodecManager codecManager) {
        Preconditions.checkNotNull(clientType, "clientType is null");
        Preconditions.checkNotNull(codecManager, "codecManager is null");

        thriftServiceMetadata = new ThriftServiceMetadata(clientType, codecManager.getCatalog());
        this.clientType = thriftServiceMetadata.getName();
        ImmutableMap.Builder<Method, ParrotMethodHandler> methods = ImmutableMap.builder();
        for (ThriftMethodMetadata methodMetadata : thriftServiceMetadata.getMethods().values()) {
            ParrotMethodHandler methodHandler = new ParrotMethodHandler(methodMetadata, codecManager);
            methods.put(methodMetadata.getMethod(), methodHandler);
        }
        methodHandlers = methods.build();
    }

    public String getClientType() {
        return clientType;
    }

    public String getName() {
        return thriftServiceMetadata.getName();
    }

    public ThriftServiceMetadata getThriftServiceMetadata() {
        return thriftServiceMetadata;
    }

    public Map<Method, ParrotMethodHandler> getMethodHandlers() {
        return methodHandlers;
    }

}
