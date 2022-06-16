package com.xin.server.service;

/**
 * @author carl.zheng
 * @date 2022/6/15 14:40
 */
public class ServerBootstrap8090 {
    public static void main(String[] args) {
        UserServiceImpl.startServer("127.0.0.1",8090);
    }
}
