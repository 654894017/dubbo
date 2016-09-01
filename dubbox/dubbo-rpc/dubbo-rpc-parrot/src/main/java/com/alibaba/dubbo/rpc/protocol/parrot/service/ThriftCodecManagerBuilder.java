package com.alibaba.dubbo.rpc.protocol.parrot.service;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import com.facebook.swift.codec.ThriftCodec;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.internal.ThriftCodecFactory;
import com.facebook.swift.codec.internal.compiler.CompilerThriftCodecFactory;
import com.facebook.swift.codec.metadata.ThriftCatalog;

/**
 * 
 * <B>Description</B> TODO <br />
 * <B>Copyright</B> Copyright (c) 2016 www.diligrp.com All rights reserved. <br />
 * 本软件源代码版权归聚美优品,未经许可不得任意复制与传播.<br />
 * <B>Company</B> 聚美优品
 * @createTime 2016年8月19日 下午5:20:20
 * @author lushiwei
 */
@ThreadSafe
public class ThriftCodecManagerBuilder {

    public ThriftCodecManager build() {
        ThriftCodecFactory thriftCodecFactory = new CompilerThriftCodecFactory();
        ThriftCatalog thriftCatalog = new ThriftCatalog();
        Set<ThriftCodec<?>> codecSet = new HashSet<ThriftCodec<?>>();
        return new ThriftCodecManager(thriftCodecFactory, thriftCatalog, codecSet);
    }
}
