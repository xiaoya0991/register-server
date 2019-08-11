package com.zhss.demo.register.server.web;

/**
 * 心跳请求
 *
 * @author wenliang
 */
public class HeartbeatRequest extends AbstractRequest {
    public HeartbeatRequest(Integer type, String serviceName, String serviceInstanceId) {
        super(type, serviceName, serviceInstanceId);
    }
}
