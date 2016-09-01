package com.alibaba.dubbo.rpc.protocol.parrot.service;

import com.facebook.swift.codec.ThriftCodecManager;
/**
 * 
 * <B>Description</B> TODO <br />
 * <B>Copyright</B> Copyright (c) 2016 www.diligrp.com All rights reserved. <br />
 * 本软件源代码版权归聚美优品,未经许可不得任意复制与传播.<br />
 * <B>Company</B> 聚美优品
 * @createTime 2016年8月19日 下午5:19:23
 * @author lushiwei
 */
public class ParrotCodecManager extends ThriftCodecManager{
	
	 public static ThriftCodecManagerBuilder newBuilder() {
	    	return new ThriftCodecManagerBuilder();
	    }
}
