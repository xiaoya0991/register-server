package com.zhss.demo.register.server.http;

import ai.houyi.dorado.rest.annotation.*;
import com.alibaba.fastjson.JSONObject;
import com.zhss.demo.register.server.core.DeltaRegistry;
import com.zhss.demo.register.server.core.ServiceInstance;
import com.zhss.demo.register.server.web.HeartbeatRequest;
import com.zhss.demo.register.server.web.RegisterRequest;
import com.zhss.demo.register.server.web.ServerManagement;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wenliang
 */
@Controller
public class HttpController {



    private ServerManagement serverManagement;


    public HttpController(){
        this.serverManagement = ServerManagement.getInstance();
    }

    /**
     * 服务注册
     * @param
     * @return
     */
    @Path("/register")
    @POST
    public void register(@RequestBody JSONObject registerRequest) {
        RegisterRequest request = new RegisterRequest(registerRequest.getString("hostname"),
                registerRequest.getString("ip"),registerRequest.getInteger("port"),
                registerRequest.getInteger("type"),registerRequest.getString("serviceName"),
                registerRequest.getString("serviceInstanceId"));
        this.serverManagement.register(request);

    }


    /***
     * 发送心跳
     * @param heartbeatRequest
     * @return
     */
    @Path("/heartbeat")
    @POST
    public void heartbeat(@RequestBody JSONObject heartbeatRequest) {

        HeartbeatRequest request = new HeartbeatRequest(heartbeatRequest.getInteger("type"),
                heartbeatRequest.getString("serviceName"),
                heartbeatRequest.getString("serviceInstanceId"));

        this.serverManagement.heartbeat(request);
    }


    /***
     * 集群同步
     */
    @Path("/cluster")
    @GET
    public void clusterReplicator(){

    }


    /**
     * 全量拉去注册表
     * @return
     */
    @Path("/fetchFullRegistry")
    @GET
    public JSONObject fetchFullRegistry(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fetchFullRegistry", this.serverManagement.fetchFullRegistry());
        return jsonObject;

    }


    /**
     * 增量拉去注册表
     * @return
     */
    @Path("/fetchDeltaRegistry")
    @GET
    public Map<String,DeltaRegistry> fetchDeltaRegistry(){
        Map<String, DeltaRegistry> fetchDeltaRegistry = new HashMap<>();
        fetchDeltaRegistry.put("fetchDeltaRegistry", this.serverManagement.fetchDeltaRegistry());
        return fetchDeltaRegistry;

    }





}
