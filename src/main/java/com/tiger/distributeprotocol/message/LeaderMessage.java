package com.tiger.distributeprotocol.message;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/7 22:29
 * @Description:
 * @Version: 1.0
 **/
public class LeaderMessage implements Message {
    private static final long serialVersionUID = -3643986199501641403L;
    private MessageType messageType = MessageType.MESSAGE_LEAGER;
    private long serverId;
    private long voteEphoch;
    private long transactionId;
    private long leaderId;

    public LeaderMessage(long serverId, long voteEphoch, long transactionId, long leaderId) {
        this.serverId = serverId;
        this.voteEphoch = voteEphoch;
        this.transactionId = transactionId;
        this.leaderId = leaderId;
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public long getServerId() {
        return serverId;
    }


    public long getVoteEphoch() {
        return voteEphoch;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getLeaderId() {
        return leaderId;
    }

    @Override
    public String toString() {
        return "LeaderMessage{" +
                "messageType=" + messageType +
                ", serverId=" + serverId +
                ", voteEphoch=" + voteEphoch +
                ", transactionId=" + transactionId +
                ", leaderId=" + leaderId +
                '}';
    }
}
