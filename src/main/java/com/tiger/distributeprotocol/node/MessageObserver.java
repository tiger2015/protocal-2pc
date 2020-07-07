package com.tiger.distributeprotocol.node;

import com.tiger.distributeprotocol.message.Message;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/7 20:53
 * @Description:
 * @Version: 1.0
 **/
public interface MessageObserver {
    void notify(Message message);
}
