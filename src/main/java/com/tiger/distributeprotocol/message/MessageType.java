package com.tiger.distributeprotocol.message;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/5 17:36
 * @Description:
 * @Version: 1.0
 **/
public enum MessageType {

    MESSAGE_VOTE,
    MESSAGE_TRANSACTION,
    MESSAGE_VOTE_RESULT,
    MESSAGE_TRANSACTION_EXECUTE_RESULT,
    MESSAGE_TRANSACTION_COMMIT,
    MESSAGE_TRANSACTION_COMMIT_RESULT,
    MESSAGE_HEART_BEAT,
    MESSAGE_UNKNOW;
}
