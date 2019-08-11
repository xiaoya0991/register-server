package com.zhss.demo.register.server.core;

import com.zhss.demo.register.server.web.Applications;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 服务注册表的缓存
 *
 * @author wenliang
 */
public class ServiceRegistryCache {


    private ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();


    public static class CacheKey {

        /**
         * 全量注册表缓存key
         */
        public static final String FULL_SERVICE_REGISTRY = "full_service_registry";


        /**
         * 增量注册表缓存key
         */
        public static final String DELTA_SERVICE_REGISTRY = "delta_service_registry";


    }

    /***
     * 单例
     */
    private static final ServiceRegistryCache instance = new ServiceRegistryCache();

    private static final Long CACHE_MAP_SYNC_INTERVAL = 30 * 1000L;

    /***
     * 注册表数据
     */
    private ServiceRegistry registry = ServiceRegistry.getInstance();


    /****
     * 只读缓存
     */
    private Map<String, Object> readOnlyMap;


    /**
     * 读写缓存
     */
    private Map<String, Object> readWriteMap;

    /***
     * 内部锁
     */
    private Object lock;


    /**
     * cache map同步后台线程
     */
    private CacheMapSyncDaemon cacheMapSyncDaemon;


    /**
     * 对readOnluMap的读写锁
     */
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();


    public ServiceRegistryCache() {
        this.readOnlyMap = new HashMap<>();
        this.readWriteMap = new HashMap<>();
        this.lock = new Object();
        this.cacheMapSyncDaemon = new CacheMapSyncDaemon();
        this.cacheMapSyncDaemon.setDaemon(true);
        this.cacheMapSyncDaemon.start();
    }

    /***
     * 根据缓存key来获取数据
     * @param
     * @return
     */
    public Map<String, Map<String, ServiceInstance>>  get(String cacheKey) {


            return  serviceRegistry.getRegistry();





//        Object cacheValue = null;
//
//        try {
//
//            readLock.lock();
//            cacheValue = readOnlyMap.get(cacheKey);
//            if (cacheValue == null) {
//                synchronized (lock) {
//                    if (readOnlyMap.get(cacheKey) == null) {
//                        cacheValue = readWriteMap.get(cacheKey);
//                        if (cacheValue == null) {
//                            readWriteMap.put(cacheKey, cacheValue);
//                        }
//                        readOnlyMap.put(cacheKey, cacheValue);
//                    }
//                }
//            }
//
//        } finally {
//            readLock.unlock();
//        }
//
//        return cacheValue;










    }


    /**
     * 获取实际的缓存数据
     *
     * @param cacheKey
     * @return
     */
    public Object getCacheValue(String cacheKey) {

        try {
            registry.readLock();
            if (CacheKey.FULL_SERVICE_REGISTRY.equals(cacheKey)) {
                return new Applications(registry.getRegistry());
            } else if (CacheKey.DELTA_SERVICE_REGISTRY.equals(cacheKey)) {
                return registry.getDeltaRegistry();
            }

        } finally {
            registry.readUnlock();

        }
        return null;
    }


    public void invalidate() {
        synchronized (lock) {
            readWriteMap.remove(CacheKey.FULL_SERVICE_REGISTRY);
            readWriteMap.remove(CacheKey.DELTA_SERVICE_REGISTRY);

        }
    }


    /***
     * 同步两个缓存map的后台线程
     */
    class CacheMapSyncDaemon extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (lock) {
                        if (readWriteMap.get(CacheKey.FULL_SERVICE_REGISTRY) == null) {
                            try {
                                writeLock.lock();
                                readOnlyMap.put(CacheKey.FULL_SERVICE_REGISTRY, null);

                            } finally {
                                writeLock.unlock();
                            }
                        }
                        if (readWriteMap.get(CacheKey.DELTA_SERVICE_REGISTRY) == null) {
                            try {
                                writeLock.lock();
                                readOnlyMap.put(CacheKey.DELTA_SERVICE_REGISTRY, null);
                            } finally {
                                writeLock.unlock();

                            }
                        }
                    }
                    Thread.sleep(2000);


                } catch (Exception e) {
                    e.printStackTrace();


                }
            }

        }
    }


    /**
     * 获取单例
     *
     * @return
     */
    public static ServiceRegistryCache getInstance() {
        return instance;
    }


}
