package com.tiger.distributeprotocol.message;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/6 22:40
 * @Description:
 * @Version: 1.0
 **/
public class HeartBeatMessage implements Message {
    private static final long serialVersionUID = 6257356414583184042L;
    private MessageType messageType = MessageType.MESSAGE_HEART_BEAT;
    private Long serverId;

    public HeartBeatMessage(Long serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        return "HeartBeatMessage{" +
                "messageType=" + messageType +
                ", serverId=" + serverId +
                '}';
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public long getServerId() {
        return serverId;
    }
}
