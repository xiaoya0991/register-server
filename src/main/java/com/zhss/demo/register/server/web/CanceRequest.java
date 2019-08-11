package com.zhss.demo.register.server.web;

/**
 * 服务下线
 *
 * @author wenliang
 */
public class CanceRequest extends AbstractRequest {
    public CanceRequest(Integer type, String serviceName, String serviceInstanceId) {
        super(type, serviceName, serviceInstanceId);
    }
}
