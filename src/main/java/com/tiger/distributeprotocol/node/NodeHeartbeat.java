package com.tiger.distributeprotocol.node;


import com.tiger.distributeprotocol.TwoPCServer;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/7 21:21
 * @Description:
 * @Version: 1.0
 **/
public class NodeHeartbeat {
    private static final long TTL = TwoPCServer.heartbeatMessageTimeout;
    private long id;
    private long lastTime;

    public NodeHeartbeat(long id) {
        this.id = id;
    }

    public void updateTime(long time) {
        this.lastTime = time;
    }

    public boolean isAlive(long time) {
        return time - lastTime <= TTL;
    }

}
