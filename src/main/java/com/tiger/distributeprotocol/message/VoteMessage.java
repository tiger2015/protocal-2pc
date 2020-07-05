package com.tiger.distributeprotocol.message;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/5 17:50
 * @Description:
 * @Version: 1.0
 **/
public class VoteMessage implements Message {
    private static final long serialVersionUID = 193914724054660357L;
    private MessageType type = MessageType.MESSAGE_VOTE;
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
                "type=" + type +
                ", voteEpoch=" + voteEpoch +
                ", serverId=" + serverId +
                ", transactionId=" + transactionId +
                '}';
    }
}
