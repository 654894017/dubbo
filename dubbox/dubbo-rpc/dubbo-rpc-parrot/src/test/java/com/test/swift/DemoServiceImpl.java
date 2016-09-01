package com.test.swift;

import java.util.ArrayList;
import java.util.List;

public class DemoServiceImpl implements DemoService{
	private final List<LogEntry> messages = new ArrayList<LogEntry>();

	@Override
	public ResultCode log(LogEntry log) {
		this.messages.addAll(messages);
		System.out.println("this is server");
		return ResultCode.ceshi;
	}
}
