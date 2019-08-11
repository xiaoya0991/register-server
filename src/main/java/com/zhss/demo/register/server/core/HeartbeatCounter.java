package com.zhss.demo.register.server.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 心跳测量计数器
 *
 * @author wenliang
 */

public class HeartbeatCounter {

    /***
     * 单例实例
     */
    private static volatile HeartbeatCounter instance = null;

    /***
     * 最近一分钟的心跳次数
     */
    private AtomicLong latestMinuteHeartbeatRate;

    /***
     * 最近一分钟的时间戳
     */
    private long latestMinuteTmestamp= System.currentTimeMillis();


    private HeartbeatCounter() {
        this.latestMinuteHeartbeatRate = new AtomicLong(0L);
        Daemon daemon = new Daemon();
        daemon.setDaemon(true);
        daemon.start();
    }


    /***
     * 获取单例实例
     * @return
     */
    public static HeartbeatCounter getInstance() {

        if (instance == null){
            synchronized (HeartbeatCounter.class){
                if (instance== null){
                    instance = new HeartbeatCounter();
                }
            }
        }
        return instance;
    }


    /***
     * 增加一次最近一分钟的心跳次数
     */
    public void increment() {
        latestMinuteHeartbeatRate.incrementAndGet();
    }


    /***
     * 获取最近一分钟的心跳次数
     * @return
     */
    public long get() {
        return latestMinuteHeartbeatRate.get();
    }


     class Daemon extends Thread {

        @Override
        public void run() {

            while (true) {

                try {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - latestMinuteTmestamp > 60 * 1000) {
                        latestMinuteHeartbeatRate = new AtomicLong(0L);
                        latestMinuteTmestamp = System.currentTimeMillis();
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }


}
