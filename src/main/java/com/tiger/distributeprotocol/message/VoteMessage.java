package com.tiger.distributeprotocol.message;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/5 17:50
 * @Description:
 * @Version: 1.0
 **/
public class VoteMessage implements Message {
    private static final long serialVersionUID = 193914724054660357L;
    private MessageType messageType = MessageType.MESSAGE_VOTE;
    private long voteEpoch; // 投票周期
    private long serverId; //
    private long transactionId; // 事务ID

    public VoteMessage(long voteEpoch, long serverId, long transactionId) {
        this.voteEpoch = voteEpoch;
        this.serverId = serverId;
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "VoteMessage{" +
                "type=" + messageType +
                ", voteEpoch=" + voteEpoch +
                ", serverId=" + serverId +
                ", transactionId=" + transactionId +
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

    public long getVoteEpoch() {
        return voteEpoch;
    }

    public long getTransactionId() {
        return transactionId;
    }
}
