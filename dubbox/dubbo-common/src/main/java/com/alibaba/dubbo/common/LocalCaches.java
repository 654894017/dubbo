package com.alibaba.dubbo.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 本地缓存
 * <B>Description</B> TODO <br />
 * <B>Copyright</B> Copyright (c) 2016 www.diligrp.com All rights reserved. <br />
 * 本软件源代码版权归聚美优品,未经许可不得任意复制与传播.<br />
 * <B>Company</B> 聚美优品
 * @createTime 2016年8月24日 下午3:21:07
 * @author MaJ
 */
public class LocalCaches {
	 //新增发布服务注解名映射缓存(MaJ)
    public static final Map<String,String> annotationMappingsCache=new ConcurrentHashMap<String,String>();

    public static Map<String, String> getAnnotationmappingscache() {
        return annotationMappingsCache;
    }
    
    public static void  setAnnotationmappingscache(String key,String value) {
        if(key==null)return;
         annotationMappingsCache.put(key, value);
    }
}
