package com.xin.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.xin.common.service.Serializer;

import java.io.IOException;

/**
 * @author carl.zheng
 * @date 2022/6/15 12:18
 */
public class JSONSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) throws IOException {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException {
        return JSON.parseObject(bytes, clazz);
    }
}
