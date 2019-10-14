package com.dai.thread;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 信号量 Semaphore 实现 ABC 循环输出
 */
public class ABCSemaphore {

    private static Semaphore semaphoreA = new Semaphore(1);
    private static Semaphore semaphoreB = new Semaphore(1);
    private static Semaphore semaphoreC = new Semaphore(1);
    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
        try {
            /**
             * B,C 线程先获取一个许可
             */
            semaphoreB.acquire();
            semaphoreC.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread threadA = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    semaphoreA.acquire();
                    System.out.print("A , ");
                    semaphoreB.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread threadB = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    semaphoreB.acquire();
                    System.out.print("B , ");
                    semaphoreC.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread threadC = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    semaphoreC.acquire();
                    System.out.print("C . " + (i + 1));
                    System.out.println();
                    semaphoreA.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        threadA.start();
        threadB.start();
        threadC.start();
    }

}
