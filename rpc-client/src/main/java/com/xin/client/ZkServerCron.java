package com.xin.client;

import com.xin.common.service.RpcEncoder;
import com.xin.common.service.RpcRequest;
import com.xin.common.service.XqcZkSerializer;
import com.xin.common.service.ZkConstant;
import com.xin.common.service.impl.JSONSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import org.I0Itec.zkclient.ZkClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 客户端维护一个定时任务，每个5秒去获取ZK上的服务端列表，
 * 进行连接尝试，并记录每个服务的耗时时间，将其耗时时间记录在自身节点上，
 * 并且如果5秒未连接上某一服务器，则将该节点从ZK节点上清除
 *
 * @author carl.zheng
 * @date 2022/6/16 9:45
 */
public class ZkServerCron {
    public static void main(String[] args) throws Exception{
        ZkClient zkClient = new ZkClient(ZkConstant.ZK_SERVER_STR);
        zkClient.setZkSerializer(new XqcZkSerializer());
        while (true) {
            //每隔5秒去连接zk
            List<String> children = zkClient.getChildren(ZkConstant.RPC_PARENT_NODE_NAME);
            for (String child : children) {
                String ip = child.split("-")[0];
                String port = child.split("-")[1];
                Bootstrap bootstrap = new Bootstrap();
                //创建连接池对象
                EventLoopGroup group = new NioEventLoopGroup();
                bootstrap.group(group).channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY,true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                pipeline.addLast(new StringDecoder());
                                pipeline.addLast(new RpcEncoder(RpcRequest.class, new JSONSerializer()));
                            }
                        });
                long start = System.currentTimeMillis();
                //获取unix时间戳至今的秒数

                bootstrap.connect(ip, Integer.parseInt(port));
                long end = System.currentTimeMillis();
                long time = (end - start);

                //如果耗时超过5秒，则认为该节点失效，从ZK节点上删除
                if(time > 5000) {
                    zkClient.delete(ZkConstant.RPC_PARENT_NODE_NAME + "/" + ip + "-" + port);
                    System.out.println("节点宕机，已删除节点：" + ZkConstant.RPC_PARENT_NODE_NAME + "/" + ip + "-" + port);
                }
                String timeStr = String.valueOf(time);
                //将该节点数据修改为耗时时间
                zkClient.writeData(ZkConstant.RPC_PARENT_NODE_NAME + "/" + ip + "-" + port, timeStr);
            }
            Thread.sleep(5000);
        }
    }
}
