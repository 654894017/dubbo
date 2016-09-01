/**        
 *
 * SwiftScribe.java Create on 2013-5-24 上午11:57:07 
 */
package com.test.swift;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

/**
 * 
 * @author libinsong1204@gmail.com
 * 
 */
public class HelloServiceImpl implements HelloService {
	private final List<LogEntry> messages = new ArrayList<LogEntry>();

	@Override
	public ResultCode log(LogEntry log) {
		//this.messages.addAll(messages);
		//System.out.println("this is server");
		/*if(1==1){
		    throw new RuntimeException("test error");
		}*/
		return ResultCode.OK;
	}

	@Override
	public LogEntry getLog(LogEntry log, int num) throws TException {
		// TODO Auto-generated method stub
		return log;
	}
}