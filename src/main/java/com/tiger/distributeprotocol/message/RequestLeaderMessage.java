package com.tiger.distributeprotocol.message;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/7 22:44
 * @Description:
 * @Version: 1.0
 **/
public class RequestLeaderMessage implements Message {
    private static final long serialVersionUID = 868675244689012475L;
    private MessageType messageType = MessageType.MESSAGE_REQUEST_LEADER;
    private long serverId;

    public RequestLeaderMessage(long serverId) {
        this.serverId = serverId;
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public long getServerId() {
        return serverId;
    }

    @Override
    public String toString() {
        return "RequestLeaderMessage{" +
                "messageType=" + messageType +
                ", serverId=" + serverId +
                '}';
    }
}
