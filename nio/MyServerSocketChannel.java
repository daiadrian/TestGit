package com.dai.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dh on 2018/10/25.
 */
public class MyServerSocketChannel {

    public static void main(String[] args) throws Exception {
        //获取多路复用器
        Selector selector = Selector.open();
        //获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(9090));
        //开启非阻塞模式并且注册通道到selector中，监听OP_ACCEPT(接受 TCP 连接)的事件
        /**
         * OP_READ:通道中有数据可以读取
         * OP_WRITE:可以往通道中写入数据
         * OP_CONNECT:成功建立TCP连接
         * OP_ACCEPT:接受TCP连接
         */
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int select = selector.select();
            if (select <= 0) {
                continue;
            }
            //获取多路复用器中的所有事件,遍历这些事件
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
            while (selectionKeyIterator.hasNext()) {
                //获取事件,并且将该事件从多路复用器中移除
                SelectionKey key = selectionKeyIterator.next();
                selectionKeyIterator.remove();

                if (key.isAcceptable()) {
                    //有新的链接接入进来,但是此时不一定有数据过来。
                    //将该通道注册到多路复用器中,并且注册事件为OP_READ
                    SocketChannel accept = serverSocketChannel.accept();
                    accept.configureBlocking(false);
                    accept.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    //获取该selectionKey中的通道
                    SocketChannel accept = (SocketChannel) key.channel();
                    //读取通道中的数据
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    int num = 0;
                    if ((num = accept.read(readBuffer)) > 0) {
                        readBuffer.flip();
                        byte[] bytes = new byte[num];
                        readBuffer.get(bytes);
                        System.out.println("接收到的数据----> " + new String(bytes));
                        //写数据回去客户端
                        ByteBuffer writeBuffer = ByteBuffer.wrap(new String("我是服务端,给你写点数据让你知道下").getBytes());
                        accept.write(writeBuffer);
                    } else if (num == -1){
                        //可能通道关闭了,没有了数据
                        accept.close();
                    }
                }
            }
        }

    }

}
