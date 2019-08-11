package com.zhss.demo.register.server.web;

import com.zhss.demo.register.server.core.ServiceInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 完整的服务实例的信息
 *
 * @author wenliang
 */
public class Applications {

    private Map<String, Map<String, ServiceInstance>> registry
            = new ConcurrentHashMap<String, Map<String, ServiceInstance>>();

    public Applications() {

    }

    public Applications(Map<String, Map<String, ServiceInstance>> registry) {
        this.registry = registry;
    }

    public Map<String, Map<String, ServiceInstance>> getRegistry() {
        return registry;
    }

    public void setRegistry(Map<String, Map<String, ServiceInstance>> registry) {
        this.registry = registry;
    }

}
