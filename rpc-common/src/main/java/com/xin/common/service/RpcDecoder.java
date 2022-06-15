package com.xin.common.service;

import com.xin.common.service.impl.JSONSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 自定义RPC解码器
 *
 * @author carl.zheng
 * @date 2022/6/15 12:20
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> clazz;

    private Serializer serializer;

    public RpcDecoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    /**
     * 二进制数据解码成java对象
     * @param channelHandlerContext
     * @param byteBuf
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.isReadable()) {
            int dataLength = byteBuf.readableBytes();
            byte[] data = new byte[dataLength];
            byteBuf.readBytes(data);
            String s = new String(data);
            String substring = s.substring(4);
            RpcRequest rpcRequest = new JSONSerializer().deserialize(RpcRequest.class, substring.getBytes());
            System.out.println(rpcRequest);
            list.add(rpcRequest); // 将数据添加进去
        }
    }

}
