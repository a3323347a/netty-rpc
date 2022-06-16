package com.xin.client;

import com.xin.common.service.ZkConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.I0Itec.zkclient.ZkClient;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author carl.zheng
 * @date 2022/6/15 14:42
 */
public class RpcConsumer {

    private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static UserClientHandler client;

    public Object createProxy(final Class<?> serviceClass, final String providerName) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{serviceClass},
                (proxy, method, args) -> {
                    if (client == null) {
                        initClient();
                    }
                    // 设置参数
                    client.setParam(providerName + args[0]);
                    return executorService.submit(client).get();
                });
    }

    private static void initClient() {
        //从zk获取服务端的ip和端口
        ZkClient zkClient = new ZkClient(ZkConstant.ZK_SERVER_STR);
        List<String> children = zkClient.getChildren(ZkConstant.RPC_PARENT_NODE_NAME);

        //根据ZK中存储的服务端节点列表启动RPC服务端连接
        startServerByZkNodeList(children, zkClient);
        //注册节点监听，如果下面的子节点列表发生变化，则重新进行服务端连接
        zkClient.subscribeChildChanges(ZkConstant.RPC_PARENT_NODE_NAME, (parentPath, currentChilds) -> {
            System.out.println("检测到有子节点发生改变，重新获取子节点列表");
            startServerByZkNodeList(currentChilds, zkClient);
        });
    }

    private static void startServerByZkNodeList(List<String> children, ZkClient zkClient) {
        client = new UserClientHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(client);
                    }
                });

        //获取节点数据(服务器响应时间)，由于节点下保存的就是消耗时间，对比一下选取就行
        String ip = null;
        int port = 0;
        int chooseTime = Integer.MAX_VALUE;
        for (String child : children) {
            Object o = zkClient.readData(ZkConstant.RPC_PARENT_NODE_NAME + "/" + child);
            if (o == null) {
                continue;
            }
            int time = Integer.parseInt(o.toString());
            if (time < chooseTime) {
                chooseTime = time;
                ip = child.split("-")[0];
                port = Integer.parseInt(child.split("-")[1]);
            }
        }

        try {
            //如果ZK服务器上还未保存服务端的响应时间信息，则随机选择一台服务端进行绑定
            if (chooseTime == 0 || ip == null) {
                //随机选择一台服务器进行绑定
                int i = new Random().nextInt(children.size());
                String randomServerStr = children.get(i);
                ip = randomServerStr.split("-")[0];
                port = Integer.parseInt(randomServerStr.split("-")[1]);
            }

            bootstrap.connect(ip, port).sync();
            System.out.println("绑定了>..." + ip + ":" + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
