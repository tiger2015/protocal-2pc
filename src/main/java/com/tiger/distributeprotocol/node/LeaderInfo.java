package com.tiger.distributeprotocol.node;

import com.tiger.distributeprotocol.message.LeaderMessage;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/9 22:37
 * @Description:
 * @Version: 1.0
 **/
public class LeaderInfo {
    private LeaderMessage message = null;
    private boolean alive = false;

    public synchronized void updateLeaderMessage(LeaderMessage leaderMessage) {
        if (message == null) {
            message = leaderMessage;
        } else {
            if (leaderMessage.getVoteEphoch() > message.getVoteEphoch()) {  // 更新投票最新的
                message = leaderMessage;
            } else if (leaderMessage.getTransactionId() > message.getTransactionId()) { // 事务最新的
                message = leaderMessage;
            } else if (leaderMessage.getLeaderId() != message.getLeaderId()) {
                message = leaderMessage;
            }
        }
        alive = true;
    }

    public synchronized LeaderMessage getMessage() {
        return message;
    }

    public synchronized void setAlive(boolean alive) {
        this.alive = alive;
        if(!this.alive) message = null;
    }

    public synchronized boolean isAlive() {
        return alive;
    }


}
