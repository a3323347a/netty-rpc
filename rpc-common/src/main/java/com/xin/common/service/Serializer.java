package com.xin.common.service;

import java.io.IOException;

/**
 * @author carl.zheng
 * @date 2022/6/15 12:07
 */
public interface Serializer {

    /**
     * java对象转换为二进制
     *
     * @param object
     * @return
     */
    byte[] serialize(Object object) throws IOException;

    /**
     * 二进制转换成java对象
     *
     * @param clazz
     * @param bytes
     * @param <T>
     * @return
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;
}
