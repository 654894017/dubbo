/*
 * Copyright (C) 2012-2013 jumei, Inc. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.alibaba.dubbo.rpc.protocol.parrot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;

public class DefaultParrotContext {

    //private static ThreadLocal<DefaultParrotContext> threadLocalContext = new ThreadLocal<>();
    public final static String UUID = "uuid";// 由客服端生成唯一id
    public final static String PROTOCOL = "protocol";// 'binary' ,
                                                     // 'compact','json'
    public final static String ISHEARTBEAT = "isHeartBeat";// 'true' , 'false'
    public final static String SERVER_NAME = "serverName";// 服务的可以提供多个服务，此key用于区分服务名
    public final static String METHOD_NAME = "methodName";// 服务的可以提供多个服务，此key用于区分服务名
    public final static String TIMEOUT = "timeOut";// 超时时间由客服端发送，若是没有则选择服务的的默认超时时间,以秒为单位
    public final static String REQUESTID = "id"; //请求ID
    private Map<String, String> contexts = new ConcurrentHashMap<String, String>();
    private Channel channel = null;

    /**
     * Gets the thread-local {@link DefaultParrotContext} for the Thrift request
     * that is being processed on the current thread.
     *
     * Note that this method will only work properly when called from the thread
     * on which Parrot invoked your server method. If you transfer work to
     * another thread in the course of processing a request, this is not tracked
     * by Parrot.
     *
     * @return The {@link DefaultParrotContext} of the current request
     * @throws IllegalStateException when not called on the thread on which your
     *             server * method was originally invoked
     */
    public static DefaultParrotContext getContext() {
      /*  DefaultParrotContext currentContext = threadLocalContext.get();
        if (currentContext == null) {
            threadLocalContext.set(new DefaultParrotContext());
        }
        // checkState(currentContext != null,
        // "Cannot only get a RequestContext when running inside a Thrift handler");
        return threadLocalContext.get();*/
        return new DefaultParrotContext();
    }

    public boolean isPrimaryKey(String key) {
        if (key == null)
            return false;
        if (key.equals(UUID) || key.equals(PROTOCOL) || key.equals(ISHEARTBEAT) || key.equals(SERVER_NAME) || key.equals(TIMEOUT) || key.equals(METHOD_NAME)) {// 此值由框架设定
            return true;
        }
        return false;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    // Contexts are only created, set, and cleared internally by Parrot
    public DefaultParrotContext() {
    }

    public void setContexts(Map<String, String> contexts) {
        this.contexts = contexts;
    }

    public String getContext(String key) {
        return contexts.get(key);
    }

    public boolean removeContext(String key) {
        if (key == null)
            return false;
        if (isPrimaryKey(key)) {
            throw new RuntimeException(key + "is primary key ,Cannot delete");
        } else {
            contexts.remove(key);
        }
        return true;
    }

    public boolean putContext(String key, String value) {
        if (key == null || value == null)
            return false;
        if (isPrimaryKey(key)) {
            throw new RuntimeException(key + "is primary key ,Cannot setting");
        } else {
            contexts.put(key, value);
        }
        return true;
    }

    // 此方法供框架使用
    boolean putSystemContext(String key, String value) {
        if (key == null || value == null)
            return false;
        contexts.put(key, value);
        return true;
    }

    public Map<String, String> getSystemContexts() {
        return this.contexts;
    }


    public String toString() {
        return this.channel + ":" + contexts;

    }

    public Map<String, String> getContexts(boolean containPrimaryKey) {
        Map<String, String> contexts = new HashMap<String, String>();
        for (String key : this.contexts.keySet()) {
            if (!containPrimaryKey && isPrimaryKey(key)) {
                continue;
            }
            contexts.put(key, this.contexts.get(key));
        }
        return contexts;
    }

    public void clearContexts() {
        // TODO Auto-generated method stub
        for (String key : contexts.keySet()) {
            if (!isPrimaryKey(key)) {
                contexts.remove(key);
            }
        }
    }
}
