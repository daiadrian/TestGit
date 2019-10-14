package com.dai.thread;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用Condition实现 ：交替输出 ABC, 输出十次
 */
public class ABCCondition {

    private static ReentrantLock lock = new ReentrantLock();
    private static Condition cA = lock.newCondition();
    private static Condition cB = lock.newCondition();
    private static Condition cC = lock.newCondition();
    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {

        Thread threadA = new Thread(() -> {
            try {
                lock.lock();
                for (int i = 0; i < 10; i++) {
                    if (count.get() != 0) {
                        cA.await();
                    }
                    System.out.print("A , ");
                    count.set(1);
                    cB.signal();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });

        Thread threadB = new Thread(() -> {
            try {
                lock.lock();
                for (int i = 0; i < 10; i++) {
                    if (count.get() != 1) {
                        cB.await();
                    }
                    System.out.print("B , ");
                    count.set(2);
                    cC.signal();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });

        Thread threadC = new Thread(() -> {
            try {
                lock.lock();
                for (int i = 0; i < 10; i++) {
                    if (count.get() != 2) {
                        cC.await();
                    }
                    System.out.print("C . ");
                    System.out.println();
                    count.set(0);
                    cA.signal();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });

        threadA.start();
        threadB.start();
        threadC.start();

    }

}
