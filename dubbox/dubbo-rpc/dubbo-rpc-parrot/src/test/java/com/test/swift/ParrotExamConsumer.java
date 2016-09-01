package com.test.swift;

import org.apache.thrift.TException;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ParrotExamConsumer {
    
    public static void main(String [] args) throws Exception{
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("dubbo-demo-consumer.xml");
        context.start();
        Example e=(Example)context.getBean("example");
        Object o=e.sayHello("oooo");
        System.out.println("ok"+o);
    }

}
