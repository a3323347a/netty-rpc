package com.xin.server.service;

import com.xin.common.service.UserService;
import com.xin.common.service.ZkConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.I0Itec.zkclient.ZkClient;

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

            //将ip和端口注册到zk上
            bindIpAndPortToZk(hostName, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //客户端启动的时候将自身的IP以及绑定的端口以ip-port的格式存储到ZK上指定节点下
    private static void bindIpAndPortToZk(String hostName, Integer port) {
        ZkClient zkClient = new ZkClient(ZkConstant.ZK_SERVER_STR);
        //创建一个临时节点，节点名称：IP-端口号
        if (!zkClient.exists(ZkConstant.RPC_PARENT_NODE_NAME + "/" + hostName + "-" + port)) {
            zkClient.createEphemeral(ZkConstant.RPC_PARENT_NODE_NAME + "/" + hostName + "-" + port);
        }
        System.out.println(" create zk node in zk, node name :" + ZkConstant.RPC_PARENT_NODE_NAME + "/" + hostName + "-" + port);
    }
}
