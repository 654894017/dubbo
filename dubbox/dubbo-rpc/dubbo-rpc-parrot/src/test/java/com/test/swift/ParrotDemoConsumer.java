package com.test.swift;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author <a href="mailto:gang.lvg@alibaba-inc.com">kimi</a>
 */
public class ParrotDemoConsumer {
   private static final int THREAD_NUM =2;
    
    private static final int CALL_NUM = 1000000;
    
    private static CountDownLatch startLatch = new CountDownLatch(1);
    
    private static CountDownLatch endLatch = new CountDownLatch(THREAD_NUM);
    
    private static ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUM);
    public static void QpsTest(final HelloService service)throws Exception{
        
        final AtomicLong totalTime = new AtomicLong(0);
        for(int i=0; i<THREAD_NUM; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                        long start = System.currentTimeMillis();
                        for(int j =0, size = CALL_NUM/THREAD_NUM; j<size; j++) {
                            ResultCode code = service.log(new LogEntry("sd", "hello world"));
                        }
                        long time = System.currentTimeMillis() - start;
                        
                        totalTime.addAndGet(time);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        System.out.println("测试开始");
        
        endLatch.await();
        
        
        System.out.println("测试结束");
        long stime = totalTime.get()/1000;
        System.out.println("消耗时间：" + stime);
        System.out.println("TPS：" + (CALL_NUM/THREAD_NUM)/(stime/THREAD_NUM));
    }
    public static void test(HelloService service)throws Exception{
        int j = new Random().nextInt();
        LogEntry log = new LogEntry("sd"+j, "hello world");
       // System.out.println("request Time="+System.currentTimeMillis());
        LogEntry logResult = service.getLog(log,j);
       // System.out.println("finish Time="+System.currentTimeMillis());
        if(!log.getCategory().equals(logResult.getCategory())){
            System.out.println("error");
        }
    }
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("dubbo-demo-consumer.xml");
        context.start();
        HelloService helloService = (HelloService) context.getBean("helloService");
        ParrotDemoConsumer.QpsTest(helloService);
    }

}
