package com.zhss.demo.register.server.web;

import java.io.Serializable;

/**
 * @author wenliang
 */
public class RegisterRequest extends AbstractRequest implements Serializable {


    private String hostname;

    private String ip;
    private int port;

    public RegisterRequest(String hostname,String ip,int port,Integer type,String serviceName,String serviceInstanceId){
        super(type,serviceName,serviceInstanceId);
        this.hostname = hostname;
        this.ip = ip;
        this.port = port;

    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }



    @Override
    public String toString() {
        return "RegisterRequest{" +
                "hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

}
