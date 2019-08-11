package com.zhss.demo.register.server.cluster;

import com.zhss.demo.register.server.RegisterServer;
import com.zhss.demo.register.server.web.AbstractRequest;
import com.zhss.demo.register.server.web.CanceRequest;
import com.zhss.demo.register.server.web.HeartbeatRequest;
import com.zhss.demo.register.server.web.RegisterRequest;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 集群同步组件
 *
 * @author wenliang
 */
public class PeerReplicator {


    private static final long PEERS_REPLICATE_BATH_INTERVAL = 500;

    /***
     * 第一层队列：接收请求的高并发写入，无界队列
     */
    private ConcurrentLinkedQueue<AbstractRequest> acceptorQueue;


    /***
     * 第二层队列：有界队列，用于batch生成
     */
    private LinkedBlockingDeque<AbstractRequest> batchQueue;


    /**
     * 第三层队列：有界队列，用于batch同步发送
     */
    private LinkedBlockingDeque<PeersReqlicateBatch> replicatQueue ;


    private PeerReplicator() {
        this.acceptorQueue = new ConcurrentLinkedQueue<AbstractRequest>();
        this.batchQueue = new LinkedBlockingDeque<AbstractRequest>();
        this.replicatQueue = new LinkedBlockingDeque<PeersReqlicateBatch>();

        //启动接收请求和打包batch的线程
        AcceptorBatchThread acceptorBatchThread = new AcceptorBatchThread();
        acceptorBatchThread.setDaemon(true);
        acceptorBatchThread.start();

        //启动同步发送batch的线程
        PeersReqlicateThread peersReqlicateThread = new PeersReqlicateThread();
        peersReqlicateThread.setDaemon(true);
        peersReqlicateThread.start();

    }


    private static volatile PeerReplicator instance = null;




    public static PeerReplicator getInstance() {
      if (instance == null){
          synchronized (PeerReplicator.class){
              if (instance== null){
                  instance = new PeerReplicator();

              }
          }
      }
        return instance;
    }




    /***
     * 同步服务注册请求
     * @param request
     */
    public void replicateRegister(RegisterRequest request) {
        request.setType(AbstractRequest.REGISTER_REQUEST);
        acceptorQueue.offer(request);

    }


    /***
     * 同步服务下线请求
     * @param request
     */
    public void replicateCancel(CanceRequest request) {
        request.setType(AbstractRequest.CANCE_REGISTER_REQUEST);
        acceptorQueue.offer(request);

    }


    /***
     * 同步服务发送心跳请求
     * @param request
     */
    public void replicateHeartbeat(HeartbeatRequest request) {
        request.setType(AbstractRequest.HEARTBEAT_REQUEST);
        acceptorQueue.offer(request);

    }


    /***
     *
     */
    class AcceptorBatchThread extends Thread {

        long latestBatchGeneration = System.currentTimeMillis();

        @Override
        public void run() {

            while (RegisterServer.isRuning) {

                try {
                    AbstractRequest request = acceptorQueue.poll();
                    if (request != null) {
                        batchQueue.put(request);
                    }
                    long now = System.currentTimeMillis();
                    if (now - latestBatchGeneration >= PEERS_REPLICATE_BATH_INTERVAL) {

                        //此时如果第二层队列里面有数据的，生成一个batch
                        if (batchQueue.size() > 0) {

                            PeersReqlicateBatch batch = createBatch();
                            replicatQueue.offer(batch);

                        }
                        this.latestBatchGeneration = System.currentTimeMillis();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }


        private PeersReqlicateBatch createBatch() {
            PeersReqlicateBatch batch = new PeersReqlicateBatch();

            Iterator<AbstractRequest> iterator = batchQueue.iterator();
            while (iterator.hasNext()) {
                AbstractRequest request = iterator.next();
                batch.add(request);

            }
            batchQueue.clear();

            return batch;
        }
    }


    /***
     *
     */
    class PeersReqlicateThread extends Thread {

        @Override
        public void run() {
            while (RegisterServer.isRuning) {
                try {
                    PeersReqlicateBatch batch = replicatQueue.take();
                    if (batch != null) {
                        System.out.println("给所有其他的register-server发送请求，同步batch过去......");
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }
    }


}
