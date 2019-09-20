package com.dai.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

/**
 * Curator 实现的分布式锁:
 *          InterProcessMutex:          分布式可重入排它锁
 *          InterProcessSemaphoreMutex: 分布式排它锁
 *          InterProcessReadWriteLock:  分布式读写锁
 *          InterProcessMultiLock:      将多个锁作为单个实体管理的容器
 */
public class CuratorLock {

    public static void main(String[] args) {
        /**
         * 设置重试策略，创建zk客户端
         * curator链接zookeeper的策略:ExponentialBackoffRetry
         *              baseSleepTimeMs：初始sleep的时间
         *              maxRetries：最大重试次数
         *              maxSleepMs：最大重试时间
         */
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        // 启动客户端
        client.start();
        /**
         * 创建分布式可重入排他锁，监听客户端为client，锁的根节点为/locks
         */
        InterProcessMutex mutex = new InterProcessMutex(client, "/locks");
        try {
            /**
             * 加锁操作
             *     public boolean acquire(long time, TimeUnit unit)
             *          第一个参数是超时时间
             *          第二个参数是时间的单位
             */
            mutex.acquire(3, TimeUnit.SECONDS);

            /**
             * 释放锁
             */
            mutex.release();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
