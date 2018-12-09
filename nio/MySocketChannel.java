package com.dai.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by dh on 2018/10/25.
 */
public class MySocketChannel {

    public static void main(String[] args) throws Exception {
        //获取通道
        SocketChannel socketChannel = SocketChannel.open();
        //连接对应端口和地址的服务器
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9090));
        //需要发送的消息
        ByteBuffer byteBuffer = ByteBuffer.wrap(new String("首次发送消息").getBytes());
        //将消息写出到服务端
        socketChannel.write(byteBuffer);
        //获取一个ByteBuffer
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        //读取服务端发来的消息
        int num = 0;
        if ((num = socketChannel.read(readBuffer)) > 1){
            //需要先flip一下切换到读取模式。刚才read方法是将数据写入了readBuffer
            readBuffer.flip();
            byte[] bytes = new byte[num];
            readBuffer.get(bytes);
            System.out.println(new String(bytes));
        }
        //这里需要对该通道进行关闭,不然一直保持与服务端的连接,
        //  但却没有数据发送到服务端会使服务端强行关闭该连接并抛出异常
        socketChannel.close();
    }

}
