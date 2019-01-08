package com.dai.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.UnsupportedEncodingException;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /*
     * 这里我们覆盖了chanelRead()事件处理方法。
     * 每当从客户端收到新的数据时，
     * 这个方法会在收到消息时被调用，
     * 这个例子中，收到的消息的类型是ByteBuf
     * @param ctx 通道处理的上下文信息
     * @param msg 接收的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;

        String recieved = getMessage(buf);
        System.err.println("服务器接收到客户端消息：" + recieved);
        try {
            /*
             * ctx.write(Object)方法不会使消息写入到通道上，
             * 他被缓冲在了内部，你需要调用ctx.flush()方法来把缓冲区中数据强行输出
             * 或者cxt.writeAndFlush(msg)以达到同样的目的
             */
            ctx.writeAndFlush(getSendByteBuf("你好，客户端"));
            System.err.println("服务器回复消息：你好，客户端");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /*
         * ByteBuf是一个引用计数对象，这个对象必须显示地调用release()方法来释放。
         * 请记住处理器的职责是释放所有传递到处理器的引用计数对象
         * 如果有写出操作,那么netty会自动关闭
         * ReferenceCountUtil.release(msg);
         */
    }

    /*
     * 这个方法会在发生异常时触发
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        /*
         * 发生异常后，关闭连接
         */
        cause.printStackTrace();
        ctx.close();
    }

    /*
     * 从ByteBuf中获取信息 使用UTF-8编码返回
     */
    private String getMessage(ByteBuf buf) {

        byte[] con = new byte[buf.readableBytes()];
        buf.readBytes(con);
        try {
            return new String(con, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private ByteBuf getSendByteBuf(String message)
            throws UnsupportedEncodingException {

        byte[] req = message.getBytes("UTF-8");
        ByteBuf pingMessage = Unpooled.buffer();
        pingMessage.writeBytes(req);

        return pingMessage;
    }
}