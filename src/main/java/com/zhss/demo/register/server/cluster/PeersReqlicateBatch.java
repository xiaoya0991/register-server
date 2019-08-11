package com.zhss.demo.register.server.cluster;

import com.zhss.demo.register.server.web.AbstractRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 集群同步的batch
 *
 * @author wenliang
 */
public class PeersReqlicateBatch {

    private List<AbstractRequest> requests;


    public PeersReqlicateBatch(){
        this.requests =new ArrayList<AbstractRequest>();
    }


    public void add(AbstractRequest request) {
        this.requests.add(request);
    }


    public List<AbstractRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<AbstractRequest> requests) {
        this.requests = requests;
    }
}
