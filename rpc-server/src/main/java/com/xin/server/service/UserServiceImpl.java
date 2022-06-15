package com.xin.server.service;

import com.xin.common.service.UserService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author carl.zheng
 * @date 2022/6/15 12:05
 */
public class UserServiceImpl implements UserService {

    @Override
    public String sayHello(String word) {
        System.out.println("调用成功，参数为：" + word);
        return "成功";
    }

    public static void startServer(String hostName, int port) {
        try {
            NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    //暂时忽略半包粘包的问题，先不做处理
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            //添加业务处理
                            pipeline.addLast(new UserServerHandler());
                        }
                    });
            bootstrap.bind(hostName, port).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
