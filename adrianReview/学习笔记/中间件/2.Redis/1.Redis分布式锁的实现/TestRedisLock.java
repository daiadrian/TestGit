package com.dai;


import com.dai.distributed_locks.DistributedLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Vector;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRedisLock {

    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private DistributedLock distributedLock;

    @Test
    public void testRedisLock() throws Exception {
        Vector<Thread> vector = new Vector<>();
        Jedis jedis = jedisPool.getResource();
        jedis.set("myLockRedisTest", "0");
        for (int i = 0;i<10;i++){
            Thread thread = new Thread(() -> {
                try {
                    System.out.println("---->>>线程:" + Thread.currentThread().getName() + " 开始啦");
                    String myLockRedis = distributedLock.lockWithTimeOut("myLockRedis", 60, 20000);
                    System.out.println("---->>>线程:" + Thread.currentThread().getName() + " 进来啦");
                    if (myLockRedis != null && myLockRedis != "") {
                        jedis.incr("myLockRedisTest");
                        System.out.println(">>>" + jedis.get("myLockRedisTest") + "<<<");
                    } else {
                        return;
                    }
                    System.out.println("---->>>线程:" + Thread.currentThread().getName() + " 出去啦啦");
                    distributedLock.releaseLock("myLockRedis", myLockRedis);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            vector.add(thread);
            thread.start();
        }
        //主线程等待所有线程执行完
        for (Thread thread : vector){
            thread.join();
        }
    }


}
