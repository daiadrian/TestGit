package com.dai.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Zookeeper 实现分布式锁
 */
public class ZooKeeperLock implements Watcher {

    // ZK对象
    private ZooKeeper zk = null;
    // 分布式锁的根节点
    private String rootLockNode;
    // 竞争资源，用来生成子节点名称
    private String lockName;
    // 当前锁
    private String currentLock;
    // 等待的锁（前一个锁）
    private String waitLock;
    // 计数器（用来在加锁失败时阻塞加锁线程）
    private CountDownLatch countDownLatch;
    // 超时时间
    private int sessionTimeout = 30000;


    /**
     *  构造器中创建ZK链接，创建锁的根节点
     *
     * @param zkAddress     ZK的地址
     * @param rootLockNode  根节点名称
     * @param lockName      子节点名称
     */
    public ZooKeeperLock(String zkAddress, String rootLockNode, String lockName) {
        this.rootLockNode = rootLockNode;
        this.lockName = lockName;
        try {
            /**
             * 创建连接，zkAddress格式为：IP:PORT
             * watcher监听器为自身
             */
            zk = new ZooKeeper(zkAddress, this.sessionTimeout, this);
            /**
             * 检测锁的根节点是否存在，不存在则创建
             */
            Stat stat = zk.exists(rootLockNode, false);
            if (null == stat) {
                zk.create(rootLockNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != zk) {
                try {
                    zk.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 加锁方法，先尝试加锁，不能加锁则等待上一个锁的释放
     *
     * @return
     */
    public boolean lock() {
        if (this.tryLock()) {
            System.out.println("线程【" + Thread.currentThread().getName() + "】加锁（" + this.currentLock + "）成功！");
            return true;
        } else {
            return waitOtherLock(this.waitLock, this.sessionTimeout);
        }
    }

    public boolean tryLock() {
        // 分隔符
        String split = "_lock_";
        if (this.lockName.contains("_lock_")) {
            throw new RuntimeException("lockName can't contains '_lock_' ");
        }
        try {
            /**
             * 创建锁节点（临时有序节点）并且得到节点名称
             *
             * path: 根节点/子锁名称+分隔符
             */
            this.currentLock = zk.create(this.rootLockNode + "/" + this.lockName + split, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            System.out.println("线程【" + Thread.currentThread().getName()
                    + "】创建锁节点（" + this.currentLock + "）成功，开始竞争...");

            /**
             * 获取所有子节点
             */
            List<String> nodes = zk.getChildren(this.rootLockNode, false);
            /**
             * 获取所有正在竞争lockName的锁
             */
            List<String> lockNodes = new ArrayList<String>();
            for (String nodeName : nodes) {
                if (nodeName.split(split)[0].equals(this.lockName)) {
                    lockNodes.add(nodeName);
                }
            }
            Collections.sort(lockNodes);

            /**
             * 获取最小节点与当前锁节点比对加锁
             *
             *      比对最小节点的名称是否跟刚才创建的临时节点名称一致
             *      一致则证明当前加锁成功
             */
            String currentLockPath = this.rootLockNode + "/" + lockNodes.get(0);
            if (this.currentLock.equals(currentLockPath)) {
                return true;
            }

            /**
             * 加锁失败，设置前一节点为等待锁节点
             */
            String currentLockNode = this.currentLock.substring(this.currentLock.lastIndexOf("/") + 1);
            int preNodeIndex = Collections.binarySearch(lockNodes, currentLockNode) - 1;
            this.waitLock = lockNodes.get(preNodeIndex);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 等待获取锁，带超时时间
     *
     * @param waitLock          当前节点的前一个锁
     * @param sessionTimeout    等待获取锁的超时时间
     * @return
     */
    private boolean waitOtherLock(String waitLock, int sessionTimeout) {
        boolean islock = false;
        try {
            // 监听等待锁节点
            String waitLockNode = this.rootLockNode + "/" + waitLock;
            Stat stat = zk.exists(waitLockNode, true);
            if (null != stat) {
                System.out.println("线程【" + Thread.currentThread().getName()
                        + "】锁（" + this.currentLock + "）加锁失败，等待锁（" + waitLockNode + "）释放...");
                /**
                 * 设置计数器，使用计数器阻塞线程,带超时时间
                 */
                this.countDownLatch = new CountDownLatch(1);
                islock = this.countDownLatch.await(sessionTimeout, TimeUnit.MILLISECONDS);
                this.countDownLatch = null;
                if (islock) {
                    System.out.println("线程【" + Thread.currentThread().getName() + "】锁（"
                            + this.currentLock + "）加锁成功，锁（" + waitLockNode + "）已经释放");
                } else {
                    System.out.println("线程【" + Thread.currentThread().getName() + "】锁（"
                            + this.currentLock + "）加锁失败...");
                }
            } else {
                islock = true;
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return islock;
    }

    /**
     * 释放分布式锁
     *
     * @throws InterruptedException
     */
    public void unlock() throws InterruptedException {
        try {
            Stat stat = zk.exists(this.currentLock, false);
            if (null != stat) {
                System.out.println("线程【" + Thread.currentThread().getName() + "】释放锁 " + this.currentLock);
                zk.delete(this.currentLock, -1);
                this.currentLock = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } finally {
            zk.close();
        }
    }

    /**
     * 节点监听器回调
     *
     * @param watchedEvent
     */
    @Override
    public void process(WatchedEvent watchedEvent) {
        /**
         * 监听节点删除的事件
         *      计数器减一，恢复线程操作
         */
        if (null != this.countDownLatch && watchedEvent.getType() == Event.EventType.NodeDeleted) {
            this.countDownLatch.countDown();
        }
    }
}