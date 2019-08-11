package com.zhss.demo.register.server.web;

import java.io.Serializable;

/**
 * 请求接口
 *
 * @author wenliang
 */
public class AbstractRequest implements Serializable {


    public static final Integer REGISTER_REQUEST = 1;
    public static final Integer CANCE_REGISTER_REQUEST = 2;
    public static final Integer HEARTBEAT_REQUEST = 3;


    /***
     * 请求类型
     */
    protected Integer type;

    /**
     * 服务名称
     */
    protected String serviceName;
    /**
     * 服务实例id
     */
    protected String serviceInstanceId;

    public AbstractRequest(Integer type,String serviceName,String serviceInstanceId){
        this.type = type;
        this.serviceName = serviceName;
        this.serviceInstanceId = serviceInstanceId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }



    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    @Override
    public String toString() {
        return "HeartbeatRequest [serviceName=" + serviceName + ", serviceInstanceId=" + serviceInstanceId + "]";
    }
}
