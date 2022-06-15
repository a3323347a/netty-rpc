package com.xin.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Callable;

/**
 * @author carl.zheng
 * @date 2022/6/15 14:43
 */
public class UserClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    private Object lock = new Object();
    private ChannelHandlerContext context;
    private String result;
    private String param;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
    }

    /**
     * 收到服务端数据，唤醒等待线程
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        result = msg.toString();
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * 写出数据，开始等待唤醒
     * @return
     * @throws Exception
     */
    @Override
    public Object call() throws Exception {
        System.out.println(Thread.currentThread().getName() + "：执行数据调用");
        context.writeAndFlush(param);
        synchronized (lock) {
            lock.wait();
        }
        return result;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
