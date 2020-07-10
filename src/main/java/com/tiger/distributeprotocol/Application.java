package com.tiger.distributeprotocol;

import com.tiger.distributeprotocol.common.LogUtil;
import com.tiger.distributeprotocol.config.SystemConfig;
import com.tiger.distributeprotocol.node.ClientNode;
import com.tiger.distributeprotocol.node.Node;
import com.tiger.distributeprotocol.node.ServerNode;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 */
public class Application {
    private static final Logger LOG = LogUtil.getLogger(Application.class);

    public static void main(String[] args) {
        SystemConfig.loadConfig();
        SystemConfig.NodeAddress nodeAddress = SystemConfig.nodes.get(SystemConfig.id);
        ServerNode serverNode = new ServerNode(SystemConfig.id, nodeAddress.port);
        Map<Long, ClientNode> clientNodes = new HashMap<>();
        SystemConfig.nodes.forEach((key, value) -> {
            if (key != SystemConfig.id) {
                ClientNode clientNode = new ClientNode(key, SystemConfig.id, value.ip, value.port);
                clientNodes.put(key, clientNode);
            }
        });
        TwoPCServer twoPCServer = new TwoPCServer(SystemConfig.id, serverNode, clientNodes);
        twoPCServer.start();
        while (true) {
            if (!twoPCServer.isLeaderAlive()) { // leader的信息为空或者Leader挂掉
                LOG.info("leader is down");
                // 询问leader,如果等待一段时间未收到，则开启投票
                if (!twoPCServer.askLeader()) {
                    twoPCServer.vote();
                    twoPCServer.statisticVote();
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.info("handle transaction");
        }
    }
}
