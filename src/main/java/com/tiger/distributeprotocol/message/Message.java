package com.tiger.distributeprotocol.message;

import java.io.Serializable;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/5 17:50
 * @Description:
 * @Version: 1.0
 **/
public interface Message extends Serializable {

    MessageType getMessageType();

    long getServerId();
}
