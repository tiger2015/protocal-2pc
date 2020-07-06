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
                ClientNode clientNode = new ClientNode(value.ip, value.port);
                clientNodes.put(key, clientNode);
            }
        });
        TwoPCServer twoPCServer = new TwoPCServer(SystemConfig.id, serverNode, clientNodes);
        twoPCServer.start();
        twoPCServer.vote();
    }
}
