package com.tiger.distributeprotocol.node;


import com.tiger.distributeprotocol.common.LogUtil;
import com.tiger.distributeprotocol.config.SystemConfig;
import com.tiger.distributeprotocol.handler.MessageHandler;
import com.tiger.distributeprotocol.message.HeartBeatMessage;
import com.tiger.distributeprotocol.message.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.FailedFuture;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/5 17:12
 * @Description:
 * @Version: 1.0
 **/
public class ClientNode implements Node, ChannelFutureListener {
    private static final Logger LOG = LogUtil.getLogger(ClientNode.class);
    private static final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);
    private String ip;
    private int port;
    private NioEventLoopGroup worker;
    private volatile Channel channel;
    private volatile State state = State.DOWN;
    private Long id;
    private Long ownerId;
    private List<MessageObserver> observers;

    public ClientNode(Long id, Long ownerId, String ip, int port) {
        this.id = id;
        this.ownerId = ownerId;
        this.ip = ip;
        this.port = port;
        this.worker = new NioEventLoopGroup(NUM_PROCESSOR * 2);
        this.observers = new ArrayList<>();
        // 开启定时发送心跳任务
        scheduledThreadPool.scheduleAtFixedRate(() -> {
            Message message = new HeartBeatMessage(ownerId);
            send(message, null);
        }, SystemConfig.tickTime, SystemConfig.tickTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public void start() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker);
        bootstrap.channel(NioSocketChannel.class)
                .handler(new ClientChannelInitHandler())
                .option(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture future = bootstrap.connect(ip, port);
        future.addListener(this);
        channel = future.channel();
        LOG.info("connect to {}:{}", ip, port);
    }

    @Override
    public void stop() {
        LOG.info("stop client");
        if (Objects.nonNull(this.channel)) {
            this.channel.close();
        }
        this.worker.shutdownGracefully();
    }

    @Override
    public void send(Message message, Callback callback) {
        if (Objects.nonNull(this.channel) && this.channel.isActive()) {
            this.channel.writeAndFlush(message).addListener(future -> {
                if (Objects.nonNull(callback))
                    callback.callback(future);
            });
        } else {
            LOG.warn("server:{}:{} channel is inactive", ip, port);
            if (Objects.nonNull(callback))
                callback.callback(new FailedFuture(this.channel.eventLoop(), new ChannelException()));
        }
    }

    @Override
    public void handle(Message message) {
    }

    @Override
    public String getIp() {
        return this.ip;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public void updateState(State state) {
        this.state = state;
    }

    @Override
    public void addObserver(MessageObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(MessageObserver observer) {
        this.removeObserver(observer);
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) { // 如果连接不成功则重连
            LOG.warn("connect fail, start reconnect after {}ms", SystemConfig.tickTime);
            future.channel().eventLoop().schedule(() -> start(), SystemConfig.tickTime, TimeUnit.MILLISECONDS);
            state = State.DOWN;
        } else {  // 连接成功
            LOG.info("connect to server:{}:{} success", ip, port);
            state = State.UP;
        }
    }

    private class ClientChannelInitHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new ObjectDecoder(1024 * 1024,
                    ClassResolvers.weakCachingConcurrentResolver(ClientNode.this.getClass().getClassLoader())));
            ch.pipeline().addLast(new MessageHandler(ClientNode.this));
            ch.pipeline().addLast(new ObjectEncoder());
        }
    }
}
