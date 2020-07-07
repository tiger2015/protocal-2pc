package com.tiger.distributeprotocol;

import com.tiger.distributeprotocol.config.SystemConfig;
import com.tiger.distributeprotocol.node.ClientNode;
import com.tiger.distributeprotocol.node.Node;
import com.tiger.distributeprotocol.node.ServerNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public class Application {
    public static void main(String[] args) {
        SystemConfig.loadConfig();
        SystemConfig.NodeAddress nodeAddress = SystemConfig.nodes.get(SystemConfig.id);
        ServerNode serverNode = new ServerNode(nodeAddress.port);
        Map<Long, ClientNode> clientNodes = new HashMap<>();
        SystemConfig.nodes.forEach((key, value) -> {
            if (key != SystemConfig.id) {
                ClientNode clientNode = new ClientNode(key, SystemConfig.id, value.ip, value.port);
                clientNodes.put(key, clientNode);
            }
        });
        TwoPCServer twoPCServer = new TwoPCServer(SystemConfig.id, serverNode, clientNodes);
        twoPCServer.start();
        // 开始询问leader信息
        // 当等待一段时间后，如果没有收到主的信息，则开启投票
        if (!twoPCServer.askLeader()) {
            twoPCServer.vote();
        }
    }
}
