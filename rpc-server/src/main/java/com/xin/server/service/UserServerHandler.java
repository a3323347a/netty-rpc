package com.xin.server.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author carl.zheng
 * @date 2022/6/15 14:38
 */
public class UserServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg.toString().startsWith("UserService")) {
            String result = new UserServiceImpl().sayHello(msg.toString());
            ctx.writeAndFlush(result);
        }
    }
}
