package com.tiger.distributeprotocol;

import com.tiger.distributeprotocol.common.LogUtil;
import com.tiger.distributeprotocol.message.LeaderMessage;
import com.tiger.distributeprotocol.message.Message;
import com.tiger.distributeprotocol.message.RequestLeaderMessage;
import com.tiger.distributeprotocol.message.VoteMessage;
import com.tiger.distributeprotocol.node.*;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/6 20:54
 * @Description:
 * @Version: 1.0
 **/
public class TwoPCServer implements MessageObserver {


    enum Role {
        LEADER,
        FOLLOWER,
        NONE;
    }

    private static final Logger LOG = LogUtil.getLogger(TwoPCServer.class);
    private static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(8);

    private final long id;
    private final ServerNode serverNode; // 服务器节点
    private final Map<Long, ClientNode> clientNodes; // 客户端节点
    private AtomicLong voteEpoch = new AtomicLong(0); // 选举周期
    private AtomicLong transactionId = new AtomicLong(0); // 事务ID
    private Role role = Role.NONE; // 角色
    private LinkedBlockingQueue<Message> messageQueue;
    private LinkedBlockingQueue<Message> haerbeatMessageQueue;
    private Map<Long, NodeHeartbeat> nodeHeartbeatMap;
    private volatile LeaderMessage leaderMessage = null;

    public TwoPCServer(long id, ServerNode serverNode, Map<Long, ClientNode> clientNodes) {
        this.id = id;
        this.serverNode = serverNode;
        this.serverNode.addObserver(this);
        this.clientNodes = clientNodes;
        this.clientNodes.forEach((key, value) -> value.addObserver(this));
        this.messageQueue = new LinkedBlockingQueue<>();
        this.haerbeatMessageQueue = new LinkedBlockingQueue<>();
        this.nodeHeartbeatMap = new ConcurrentHashMap<>();
        clientNodes.forEach((key, value) -> {
            this.nodeHeartbeatMap.put(key, new NodeHeartbeat(key));
            this.nodeHeartbeatMap.get(key).isAlive(System.currentTimeMillis());
        });
    }

    public void start() {
        scheduledThreadPool.execute(() -> {
            serverNode.start();
        });
        clientNodes.forEach((key, value) -> {
            value.start();
        });
        scheduledThreadPool.scheduleAtFixedRate(new HeartbeatCheckTask(), 3000, 3000, TimeUnit.MILLISECONDS);
    }

    /**
     * 投票
     *
     * @return void
     * @Description
     * @Auther: Zeng Hu
     * @Date: 2020/7/7 22:46
     **/
    public void vote() {
        Message message = new VoteMessage(voteEpoch.getAndIncrement(), id, transactionId.getAndIncrement());
        clientNodes.forEach((key, value) -> value.send(message, new SendMessageRetryCallback(message, value, 15,
                2000)));
    }

    /**
     * 发送主的信息
     *
     * @return void
     * @Description
     * @Auther: Zeng Hu
     * @Date: 2020/7/7 22:47
     **/
    public void sendLeader() {
        Message message = new LeaderMessage(id, voteEpoch.get(), transactionId.get(), id);
        clientNodes.forEach((key, value) -> value.send(message, new SendMessageRetryCallback(message, value, 15,
                1000)));

    }

    /**
     * 询问主的信息，此方法会阻塞线程最多60秒
     *
     * @return void
     * @Description
     * @Auther: Zeng Hu
     * @Date: 2020/7/7 22:48
     **/
    public boolean askLeader() {
        Message message = new RequestLeaderMessage(id);
        clientNodes.forEach((key, value) -> value.send(message, new SendMessageRetryCallback(message, value, 30,
                1000)));
        int times = 0;
        while (times < 60) {
            if (leaderMessage == null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                    times++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void notify(Message message) {
        try {
            switch (message.getMessageType()) {
                case MESSAGE_HEART_BEAT:  // 心跳消息
                    haerbeatMessageQueue.put(message);
                    break;
                case MESSAGE_REQUEST_LEADER: // 请求leader消息
                    if (role == Role.LEADER) {
                        sendLeader();
                    }
                    break;
                case MESSAGE_LEAGER:   // leader消息
                    LeaderMessage leaderMessage = (LeaderMessage) message;
                    voteEpoch.set(leaderMessage.getVoteEphoch());
                    transactionId.set(leaderMessage.getTransactionId());
                    this.leaderMessage = leaderMessage; // 更新主的信息
                    LOG.info("node:{} update leader info", id);
                    break;
                default:
                    messageQueue.put(message);
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    class SendMessageRetryCallback implements Node.Callback {
        private Message message;
        private ClientNode clientNode;
        private int maxTimes = 10;
        private long interval = 3000;
        private int retryTimes = 0;

        public SendMessageRetryCallback(Message message, ClientNode clientNode, int maxTimes, long interval) {
            this.message = message;
            this.clientNode = clientNode;
            this.maxTimes = maxTimes;
            this.interval = interval;
        }

        public SendMessageRetryCallback(Message message, ClientNode clientNode) {
            this.message = message;
            this.clientNode = clientNode;
        }

        @Override
        public void callback(Future future) {
            if (!future.isSuccess()) {
                try {
                    LOG.info("send vote fail, retry....");
                    TimeUnit.MILLISECONDS.sleep(interval);
                    retryTimes = retryTimes + 1;
                    if (retryTimes < maxTimes) {
                        clientNode.send(message, this);
                    } else {
                        LOG.warn("after try {} times, can't send vote to {}:{}", maxTimes, clientNode.getIp(),
                                clientNode.getPort());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class HeartbeatCheckTask implements Runnable {

        @Override
        public void run() {
            try {
                Message message = haerbeatMessageQueue.poll(60000, TimeUnit.MILLISECONDS);
                long current = System.currentTimeMillis();
                if (message == null) {
                    clientNodes.forEach((key, value) -> value.updateState(Node.State.DOWN));
                    LOG.warn("all nodes down");
                } else {
                    nodeHeartbeatMap.get(message.getServerId()).updateTime(current);
                }
                clientNodes.forEach((key, value) -> {
                    if (nodeHeartbeatMap.get(key).isAlive(current)) {
                        clientNodes.get(key).updateState(Node.State.UP);
                    } else {
                        clientNodes.get(key).updateState(Node.State.DOWN);
                        LOG.warn("node:{} is down", key);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
