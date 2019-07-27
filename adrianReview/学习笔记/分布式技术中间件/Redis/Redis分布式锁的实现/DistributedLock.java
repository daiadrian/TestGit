package com.dai.distributed_locks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * redis实现分布式锁
 */
@Component
public class DistributedLock {

    @Autowired
    private JedisPool jedisPool;

    /**
     * 加锁
     * @param lockName 存放redis中的key
     * @param acquireTimeOut 分布式锁的过期时间
     * @param timeout 获取锁的超时时间
     * @return
     */
    public String lockWithTimeOut(String lockName, int acquireTimeOut, long timeout) {
        /**
         * 先setnx key是否成功;
         *      成功则设置随机值(UUID),然后设置过期时间,返回随机值给释放锁用
         *
         *      失败则计算获取锁的超时时间,时间未到则自旋获取锁直到成功或者达到超时时间
         */

        String identifier = UUID.randomUUID().toString().replaceAll("-", "");
        timeout = System.currentTimeMillis() + timeout;
        String reIdentifier = "";

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(0);
            while(System.currentTimeMillis() < timeout){
                Long setnx = jedis.setnx(lockName, identifier);
                if (setnx != null && setnx == 1){
                    //设置过期时间
                    jedis.expire(lockName, acquireTimeOut);
                    reIdentifier = identifier;
                    break;
                }else {
                    //如果key已经存在,查看过期时间,如果该key无过期时间则重新设置过期时间,以免发生死锁
                    Long ttl = jedis.ttl(lockName);
                    if (ttl == -1){
                        jedis.expire(lockName, acquireTimeOut);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.println("线程中断");
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (Exception e) {
            //TODO 处理异常
            e.printStackTrace();
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return reIdentifier;
    }

    /**
     * 释放锁
     * @param lockName 锁的名称
     * @param identifier 锁的标识(用来验证锁中的val是否一致)
     * @return
     */
    public boolean releaseLock(String lockName, String identifier){
        Jedis jedis = null;
        boolean flag = false;
        try {
            jedis = jedisPool.getResource();
            jedis.select(0);
            jedis.watch(lockName);
            String result = jedis.get(lockName);
            if (result != null && identifier.equals(result)){
                Transaction multi = jedis.multi();
                multi.del(lockName);
                List<Object> exec = multi.exec();
                if (exec != null && exec.size() > 0){
                    flag = true;
                }
            }
            jedis.unwatch();
            return flag;
        } catch (Exception e) {
            //TODO 处理异常
            e.printStackTrace();
            return false;
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

}
