/**        
 *
 * Scribe.java Create on 2013-5-24 上午11:56:24 
 */
package com.test.swift;

import org.apache.thrift.TException;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

/**
 * 
 * @author libinsong1204@gmail.com
 * 
 */
@ThriftService("HelloService")
public interface HelloService {
	@ThriftMethod("log")
	ResultCode log(final LogEntry log) throws TException;
	@ThriftMethod("getLog")
	LogEntry getLog(final LogEntry log,int num) throws TException;
}