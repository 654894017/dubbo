package com.test.swift;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ParrotExamPrivider {
    
    public static void  main(String [] args) throws Exception{
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("dubbo-demo-provider.xml");
        context.start();
        System.out.println("context started");
        System.in.read();
    }

}
