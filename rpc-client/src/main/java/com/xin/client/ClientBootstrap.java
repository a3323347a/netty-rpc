package com.xin.client;

import com.xin.common.service.UserService;

/**
 * @author carl.zheng
 * @date 2022/6/15 16:39
 */
public class ClientBootstrap {

    public static final String providerName = "UserService#sayHello#";

    public static void main(String[] args) throws InterruptedException {
        RpcConsumer consumer = new RpcConsumer(); // 创建一个代理对象
        UserService service = (UserService) consumer.createProxy(UserService.class, providerName);
        for (; ; ) {
            Thread.sleep(1000);
            System.out.println(service.sayHello("are you ok ?"));
        }
    }
}
