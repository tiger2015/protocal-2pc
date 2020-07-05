package com.tiger.distributeprotocol.node;

import com.tiger.distributeprotocol.message.Message;
import io.netty.util.concurrent.Future;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/5 12:40
 * @Description:
 * @Version: 1.0
 **/
public interface Node {
    int NUM_PROCESSOR = Runtime.getRuntime().availableProcessors();

    void start();

    void stop();

    void send(Message message, Callback callback);

    void handle(Message message);

    interface Callback {
        void callback(Future future);
    }
}
