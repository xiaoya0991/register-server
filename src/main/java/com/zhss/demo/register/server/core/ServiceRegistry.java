package com.zhss.demo.register.server.core;


import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 服务注册表
 *
 * @author zhonghuashishan
 */
public class ServiceRegistry {


    public static final Long RECENTLY_CHANGED_TIEM_CHECK_INTERVAL = 3000L;
    public static final Long RECENTLY_CHANGED_ITEM_EXPIRED = 3 * 60 * 1000L;


    private static volatile ServiceRegistry instance = null;


    /**
     * 核心的内存数据结构：注册表
     * <p>
     * Map：key是服务名称，value是这个服务的所有的服务实例
     * Map<String, ServiceInstance>：key是服务实例id，value是服务实例的信息
     */
    private Map<String, Map<String, ServiceInstance>> registry;


    /***
     * 最近变更的服务实例的队列
     */
    private Queue<RecentlyChangedServiceInstance> recentlyChangedQueue;


    /**
     * 服务注册的表
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();


    private ServiceRegistry() {
        this.registry  = new ConcurrentHashMap<>();
        this.recentlyChangedQueue = new ConcurrentLinkedQueue<>();
        RecentluChangedQueueMonitor recentluChangedQueueMonitor =
                new RecentluChangedQueueMonitor();
        recentluChangedQueueMonitor.setDaemon(true);
        recentluChangedQueueMonitor.start();
    }


    /**
     * 服务注册
     *
     * @param serviceInstance 服务实例
     */
    public  synchronized  void register(ServiceInstance serviceInstance) throws Exception {


            //将服务实例放入最近变更的队列中
            RecentlyChangedServiceInstance recentlyChangedItem = new RecentlyChangedServiceInstance(
                    serviceInstance,
                    System.currentTimeMillis(),
                    ServiceInstanceOperation.REGISTER
            );

        this.recentlyChangedQueue.offer(recentlyChangedItem);



            Map<String, ServiceInstance> serviceInstanceMap =
                    registry.get(serviceInstance.getServiceName());

            if (serviceInstanceMap == null) {
                serviceInstanceMap = new HashMap<>();
                registry.put(serviceInstance.getServiceName(), serviceInstanceMap);
            }

            serviceInstanceMap.put(serviceInstance.getServiceInstanceId(),
                    serviceInstance);

            System.out.println("服务实例，完成注册......【" + serviceInstance + "】");
            System.out.println("注册表：" + registry);

    }


    /**
     * 获取服务实例
     *
     * @param serviceName       服务名称
     * @param serviceInstanceId 服务实例id
     * @return 服务实例
     */
    public synchronized ServiceInstance getServiceInstance(String serviceName,
                                                           String serviceInstanceId) {
        Map<String, ServiceInstance> serviceInstanceMap = registry.get(serviceName);
        return serviceInstanceMap.get(serviceInstanceId);
    }


    /**
     * 获取整个注册表
     *
     * @return
     */
    public  Map<String, Map<String, ServiceInstance>> getRegistry() {
        return registry;
    }




    /***
     * 获取增量的注册表
     * @return
     */
    public DeltaRegistry getDeltaRegistry() {
        Long totalCount = 0L;
        for (Map<String, ServiceInstance> serviceInstanceMap : registry.values()) {
            totalCount += serviceInstanceMap.size();
        }
        DeltaRegistry deltaRegistry = new DeltaRegistry(
                recentlyChangedQueue, totalCount
        );

        return deltaRegistry;

    }


    /**
     * 从注册表删除一个服务实例
     *
     * @param serviceName
     * @param serviceInstanceId
     */
    public synchronized void remove(String serviceName, String serviceInstanceId) {

            System.out.println("服务实例从注册表中摘除【" + serviceName + ", " + serviceInstanceId + "】");
            Map<String, ServiceInstance> serviceInstanceMap = registry.get(serviceName);
            ServiceInstance serviceInstance = serviceInstanceMap.get(serviceInstanceId);


            //将服务实例变更信息放入队列中
            RecentlyChangedServiceInstance recentlyChangedItem = new RecentlyChangedServiceInstance(
                    serviceInstance,
                    System.currentTimeMillis(),
                    ServiceInstanceOperation.REMOVE
            );

            serviceInstanceMap.remove(serviceInstanceId);

    }


    /**
     * 获取服务注册表的单例实例
     *
     * @return
     */
    public static ServiceRegistry getInstance() {
       if (instance == null){
           synchronized (ServiceRegistry.class){
               if (instance== null){
                   instance = new ServiceRegistry();
               }
           }
       }

        return instance;
    }


    /***
     * 加读锁
     */
    public void readLock() {
        this.readLock.lock();
    }


    /**
     * 释放读锁
     */
    public void readUnlock() {
        this.readLock.unlock();
    }


    /**
     * 加写锁
     */
    public void writeLock() {
        this.writeLock = writeLock;
    }


    /**
     * 释放写锁
     */
    public void writeUnlock() {
        this.writeLock.unlock();

    }


    /***
     * 最近变化的服务实例
     */
    class RecentlyChangedServiceInstance {

        /***
         * 服务实例
         */
        ServiceInstance serviceInstance;


        /***
         * 发生变更的时间戳
         */
        Long changedTimestamp;


        String serviceInstanceOperation;

        public RecentlyChangedServiceInstance(ServiceInstance serviceInstance, Long changedTimestamp, String serviceInstanceOperation) {
            this.serviceInstance = serviceInstance;
            this.changedTimestamp = changedTimestamp;
            this.serviceInstanceOperation = serviceInstanceOperation;
        }
    }


    /***
     * 服务实例操作
     */
    class ServiceInstanceOperation {

        private ServiceInstanceOperation() {

        }

        /**
         * 注册
         */
        public static final String REGISTER = "register";


        /***
         * 删除
         */
        public static final String REMOVE = "remove";

    }


    /****
     * 最近变更的队列的监控线程
     */
    class RecentluChangedQueueMonitor extends Thread {
        @Override
        public void run() {

            while (true) {
                try {
                    synchronized (instance) {
                        RecentlyChangedServiceInstance recentlyChangedItem = null;
                        Long currentTimestamp = System.currentTimeMillis();
                        while ((recentlyChangedItem = recentlyChangedQueue.peek()) != null) {

                            if (currentTimestamp - recentlyChangedItem.changedTimestamp >
                                    RECENTLY_CHANGED_ITEM_EXPIRED) {
                                recentlyChangedQueue.poll();
                            }
                        }
                    }
                    Thread.sleep(RECENTLY_CHANGED_TIEM_CHECK_INTERVAL);

                } catch (Exception e) {

                }
            }
        }
    }

}
