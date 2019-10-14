package com.dai.thread;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用Condition实现 ：交替输出 奇数和偶数
 */
public class ABCondition {

    private static ReentrantLock lock = new ReentrantLock();
    private static Condition c1 = lock.newCondition();
    private static Condition c2 = lock.newCondition();
    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> {
            try {
                lock.lock();
                //while循环控制次数
                while(count.get() < 10) {
                    /**
                     * 如果是偶数, 那么线程 A 等待唤醒
                     */
                    if (count.get() % 2 == 0) {
                        c1.await();
                    }
                    /**
                     * 此时是奇数, 输出打印, 然后count++后唤醒 B 线程
                     */
                    System.out.println("A" + count);
                    count.addAndGet(1);
                    c2.signal();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                lock.lock();
                while(count.get() < 10) {
                    /**
                     * 如果是奇数, 那么线程 B 等待唤醒
                     */
                    if (count.get() % 2 == 1) {
                        c2.await();
                    }
                    /**
                     * 此时是偶数, 输出打印, 然后count++后唤醒 A 线程
                     */
                    System.out.println("B" + count);
                    count.addAndGet(1);
                    c1.signal();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });

        thread1.start();
        thread2.start();
    }

}
