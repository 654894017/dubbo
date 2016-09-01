package com.test.swift;

import org.apache.thrift.TException;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService("DemoService")
public interface DemoService {
    
	@ThriftMethod("log")
	ResultCode log(final LogEntry log) throws TException;

}
