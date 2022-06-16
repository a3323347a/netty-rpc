package com.xin.common.service;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.nio.charset.Charset;

/**
 * zookeeper写入数据时序列化，不然写入的数据都是乱码
 *
 * @author carl.zheng
 * @date 2022/6/16 14:45
 */
public class XqcZkSerializer implements ZkSerializer {
    @Override
    public byte[] serialize(Object o) throws ZkMarshallingError {
        return ((String)o).getBytes(Charset.defaultCharset());
    }

    @Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        return new String(bytes, Charset.defaultCharset());
    }
}
