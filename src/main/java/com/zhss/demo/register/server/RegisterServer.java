package com.zhss.demo.register.server;

import ai.houyi.dorado.rest.server.DoradoServerBuilder;
import com.zhss.demo.register.server.core.ServiceAliveMonitor;

/**
 * 代表了服务注册中心的这么一个东西
 *
 * @author
 */
public class RegisterServer {


    public static volatile Boolean isRuning = true;

    private static ServiceAliveMonitor serviceAliveMonitor = ServiceAliveMonitor.getInstance();


    public static void main(String[] args) throws Exception {



        DoradoServerBuilder.forPort(8888).build().start();
        serviceAliveMonitor.start();

    }

}
