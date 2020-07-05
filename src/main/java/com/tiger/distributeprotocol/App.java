package com.tiger.distributeprotocol;

import com.tiger.distributeprotocol.node.Node;
import com.tiger.distributeprotocol.node.ServerNode;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {


        Node node = new ServerNode(9000);
        node.start();
        node.stop();
        node.start();

        /**
        Node client = new ClientNode("127.0.0.1", 9000);
        client.start();
        **/

    }
}
