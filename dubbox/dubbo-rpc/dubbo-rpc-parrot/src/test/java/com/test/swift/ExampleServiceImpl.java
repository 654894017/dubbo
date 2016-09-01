package com.test.swift;

import org.apache.thrift.TException;


public class ExampleServiceImpl implements Example{

    @Override
    public String sayHello(String yourname) throws TException {
        System.out.println("server response");
        return "王兵是大傻逼";
    }
    
    

}
