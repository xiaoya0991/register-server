package com.zhss.demo.register.server.web;

import com.zhss.demo.register.server.cluster.PeersReqlicateBatch;
import com.zhss.demo.register.server.core.DeltaRegistry;
import com.zhss.demo.register.server.core.HeartbeatCounter;
import com.zhss.demo.register.server.core.SelfProtectionPolicy;
import com.zhss.demo.register.server.core.ServiceInstance;
import com.zhss.demo.register.server.core.ServiceRegistry;
import com.zhss.demo.register.server.core.ServiceRegistryCache;
import com.zhss.demo.register.server.core.ServiceRegistryCache.CacheKey;

import java.util.Map;


/**
 * 服务管理组件
 * @author wenliang
 */
public class ServerManagement {


    private static volatile ServerManagement instance = null;

    /**
     * 服务注册表
     */
    private ServiceRegistry registry = ServiceRegistry.getInstance();
    /**
     * 服务注册表的缓存
     */
    private ServiceRegistryCache registryCache = ServiceRegistryCache.getInstance();

    /**
     * 服务注册
     *
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    public RegisterResponse register(RegisterRequest registerRequest) {
        RegisterResponse registerResponse = new RegisterResponse();

        try {
            // 在注册表中加入这个服务实例
            ServiceInstance serviceInstance = new ServiceInstance();
            serviceInstance.setHostname(registerRequest.getHostname());
            serviceInstance.setIp(registerRequest.getIp());
            serviceInstance.setPort(registerRequest.getPort());
            serviceInstance.setServiceInstanceId(registerRequest.getServiceInstanceId());
            serviceInstance.setServiceName(registerRequest.getServiceName());
            this.registry.register(serviceInstance);

            // 更新自我保护机制的阈值
            synchronized (SelfProtectionPolicy.class) {
                SelfProtectionPolicy selfProtectionPolicy = SelfProtectionPolicy.getInstance();
                selfProtectionPolicy.setExpectedHeartbeatRate(
                        selfProtectionPolicy.getExpectedHeartbeatRate() + 2);
                selfProtectionPolicy.setExpectedHeartbeatThreshold(
                        (long) (selfProtectionPolicy.getExpectedHeartbeatRate() * 0.85));
            }

            // 过期掉注册表缓存
            registryCache.invalidate();

            registerResponse.setStatus(RegisterResponse.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            registerResponse.setStatus(RegisterResponse.FAILURE);
        }

        return registerResponse;
    }

    /**
     * 服务下线
     */
    public void cancel(CanceRequest canceRequest) {
        // 从服务注册中摘除实例
        registry.remove(canceRequest.getServiceName(), canceRequest.serviceInstanceId);

        // 更新自我保护机制的阈值
        synchronized (SelfProtectionPolicy.class) {
            SelfProtectionPolicy selfProtectionPolicy = SelfProtectionPolicy.getInstance();
            selfProtectionPolicy.setExpectedHeartbeatRate(
                    selfProtectionPolicy.getExpectedHeartbeatRate() - 2);
            selfProtectionPolicy.setExpectedHeartbeatThreshold(
                    (long) (selfProtectionPolicy.getExpectedHeartbeatRate() * 0.85));
        }

        // 过期掉注册表缓存
        registryCache.invalidate();
    }

    /**
     * 发送心跳
     *
     * @param heartbeatRequest 心跳请求
     * @return 心跳响应
     */
    public HeartbeatResponse heartbeat(HeartbeatRequest heartbeatRequest) {
        HeartbeatResponse heartbeatResponse = new HeartbeatResponse();

        try {
            // 获取服务实例
            ServiceInstance serviceInstance = registry.getServiceInstance(
                    heartbeatRequest.getServiceName(),
                    heartbeatRequest.getServiceInstanceId());
            if (serviceInstance != null) {
                serviceInstance.renew();
            }

            // 记录一下每分钟的心跳的次数
            HeartbeatCounter heartbeatMessuredRate = HeartbeatCounter.getInstance();
            heartbeatMessuredRate.increment();

            heartbeatResponse.setStatus(HeartbeatResponse.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            heartbeatResponse.setStatus(HeartbeatResponse.FAILURE);
        }

        return heartbeatResponse;
    }


    /***
     * 同步batch数据
     * @param batch
     */
    public void replicateBatch(PeersReqlicateBatch batch) {
        for (AbstractRequest request : batch.getRequests()) {
            if (request.getType().equals(AbstractRequest.REGISTER_REQUEST)) {
                register((RegisterRequest) request);
            } else if (request.getType().equals(AbstractRequest.CANCE_REGISTER_REQUEST)) {
                cancel((CanceRequest) request);

            } else if (request.getType().equals(AbstractRequest.HEARTBEAT_REQUEST)) {
                heartbeat((HeartbeatRequest) request);
            }
        }

    }




    /**
     * 拉取全量注册表
     *
     * @return
     */
    public Map<String, Map<String, ServiceInstance>>  fetchFullRegistry() {
        return registryCache.get(CacheKey.FULL_SERVICE_REGISTRY);
    }

    /**
     * 拉取增量注册表
     *
     * @return
     */
    public DeltaRegistry fetchDeltaRegistry() {
       return  registry.getDeltaRegistry();

    }


    /**
     *
     * @return
     */
    public static ServerManagement getInstance(){
        if (instance == null){
            synchronized (ServerManagement.class){
                if (instance == null){
                    instance = new ServerManagement();
                }
            }
        }

        return instance;
    }



}
