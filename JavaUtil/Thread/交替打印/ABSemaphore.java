package com.dai.thread;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 信号量 Semaphore 实现 奇数偶数交替打印
 */
public class ABSemaphore {

    private static Semaphore semaphoreA = new Semaphore(1);
    private static Semaphore semaphoreB = new Semaphore(1);
    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
        try {
            /**
             * B线程先获取一个许可
             */
            semaphoreB.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread thread1 = new Thread(() -> {
            try {
                while(count.get() < 10) {
                    /**
                     * semaphoreA 获取一个许可, 下一次进来到这里的话
                     * 需要阻塞在这里等到 semaphoreA 的许可被释放才能继续往下执行
                     */
                    semaphoreA.acquire();
                    System.out.println("A: " + count.get());
                    count.addAndGet(1);
                    /**
                     * 释放 semaphoreB 的许可
                     */
                    semaphoreB.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                while(count.get() < 10) {
                    /**
                     * 此时因为 B 线程已经获取走了一个许可，
                     *  这个时候 semaphoreB 已经没有许可了，需要阻塞在这里等待许可的释放
                     */
                    semaphoreB.acquire();
                    System.out.println("B: " + count.get());
                    count.addAndGet(1);
                    /**
                     * 释放 semaphoreA 的许可
                     */
                    semaphoreA.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread1.start();
        thread2.start();
    }

}
