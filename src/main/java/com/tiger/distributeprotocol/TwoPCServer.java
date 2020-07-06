package com.tiger.distributeprotocol;

import com.tiger.distributeprotocol.common.LogUtil;
import com.tiger.distributeprotocol.message.Message;
import com.tiger.distributeprotocol.message.VoteMessage;
import com.tiger.distributeprotocol.node.ClientNode;
import com.tiger.distributeprotocol.node.Node;
import com.tiger.distributeprotocol.node.ServerNode;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/6 20:54
 * @Description:
 * @Version: 1.0
 **/
public class TwoPCServer {

    enum Role {
        LEADER,
        FOLLOWER,
        NONE;
    }

    private static final Logger LOG = LogUtil.getLogger(TwoPCServer.class);

    private static ExecutorService fixThreadPool = Executors.newFixedThreadPool(8);

    private final long id;
    private final ServerNode serverNode; // 服务器节点
    private final Map<Long, ClientNode> clientNodes; // 客户端节点
    private AtomicLong evoteEpoch = new AtomicLong(0); // 选举周期
    private AtomicLong transactionId = new AtomicLong(0); // 事务ID
    private Role role = Role.NONE; // 角色

    public TwoPCServer(long id, ServerNode serverNode, Map<Long, ClientNode> clientNodes) {
        this.id = id;
        this.serverNode = serverNode;
        this.clientNodes = clientNodes;
    }

    public void start() {
        fixThreadPool.execute(() -> {
            serverNode.start();
        });
        clientNodes.forEach((key, value) -> {
            value.start();
        });
    }

    public void vote() {
        Message message = new VoteMessage(evoteEpoch.getAndIncrement(), id, transactionId.getAndIncrement());
        clientNodes.forEach((key, value) -> value.send(message, new VoteMessageSendCallback(message, value)));
    }


    class VoteMessageSendCallback implements Node.Callback {
        private Message message;
        private ClientNode clientNode;
        private volatile int tryTimes;

        public VoteMessageSendCallback(Message message, ClientNode clientNode) {
            this.message = message;
            this.clientNode = clientNode;
        }

        @Override
        public void callback(Future future) {
            if (!future.isSuccess()) {
                try {
                    LOG.info("send vote fail, retry....");
                    TimeUnit.MILLISECONDS.sleep(1000);
                    tryTimes = tryTimes + 1;
                    if (tryTimes < 180) {  // 重试60次
                        clientNode.send(message, this);
                    } else {
                        LOG.warn("after try 180 times, can't send vote to {}:{}", clientNode.getIp(), clientNode.getPort());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
