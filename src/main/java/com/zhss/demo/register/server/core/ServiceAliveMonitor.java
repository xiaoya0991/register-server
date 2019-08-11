package com.zhss.demo.register.server.core;

import com.zhss.demo.register.server.RegisterServer;

import java.util.Map;

/**
 * 微服务存活状态监控组件
 *
 * @author wenliang
 */
public class ServiceAliveMonitor {

    /**
     * 检查服务实例是否存活的间隔
     */
    private static final Long CHECK_ALIVE_INTERVAL = 60 * 1000L;


    /**
     * 负责监控微服务存活状态的后台线程
     */
    private AliveMonitor aliveMonitor;


    private static volatile ServiceAliveMonitor instance = null;


    private ServiceAliveMonitor() {
        this.aliveMonitor = new AliveMonitor();
        this.aliveMonitor.setDaemon(true);
        this.aliveMonitor.setName("ServiceAliveMonitor");

    }

    /**
     * 启动后台线程
     */
    public void start() {
        aliveMonitor.start();
    }


    /**
     * 获取单例
     * @return
     */
    public static ServiceAliveMonitor getInstance(){
        if (instance== null){
            synchronized (ServiceAliveMonitor.class){
                if (instance == null) {
                    instance = new ServiceAliveMonitor();
                }
            }
        }

        return instance;
    }

    /**
     * 负责监控微服务存活状态的后台线程
     *
     * @author zhonghuashishan
     */
    class AliveMonitor extends Thread {

        private ServiceRegistry registry = ServiceRegistry.getInstance();


        @Override
        public void run() {
            Map<String, Map<String, ServiceInstance>> registryMap = null;


            while (RegisterServer.isRuning) {
                try {
                    // 可以判断一下是否要开启自我保护机制
                    System.out.println("后台线程");
                    SelfProtectionPolicy selfProtectionPolicy = SelfProtectionPolicy.getInstance();
                    if (selfProtectionPolicy.isEnable()) {
                        Thread.sleep(CHECK_ALIVE_INTERVAL);
                        continue;
                    }

                    registryMap = registry.getRegistry();

                    for (String serviceName : registryMap.keySet()) {
                        Map<String, ServiceInstance> serviceInstanceMap =
                                registryMap.get(serviceName);

                        for (ServiceInstance serviceInstance : serviceInstanceMap.values()) {
                            // 说明服务实例距离上一次发送心跳已经超过90秒了
                            // 认为这个服务就死了
                            // 从注册表中摘除这个服务实例
                            if (!serviceInstance.isAlive()) {
                                registry.remove(serviceName, serviceInstance.getServiceInstanceId());

                                // 更新自我保护机制的阈值
                                synchronized (SelfProtectionPolicy.class) {
                                    selfProtectionPolicy.setExpectedHeartbeatRate(
                                            selfProtectionPolicy.getExpectedHeartbeatRate() - 2);
                                    selfProtectionPolicy.setExpectedHeartbeatThreshold(
                                            (long) (selfProtectionPolicy.getExpectedHeartbeatRate() * 0.85));
                                }
                            }
                        }
                    }

                    Thread.sleep(CHECK_ALIVE_INTERVAL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
