package com.tiger.distributeprotocol.node;

import com.tiger.distributeprotocol.TwoPCServer;
import com.tiger.distributeprotocol.common.LogUtil;
import com.tiger.distributeprotocol.handler.MessageHandler;
import com.tiger.distributeprotocol.message.Message;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/5 13:26
 * @Description:
 * @Version: 1.0
 **/
public class ServerNode implements Node {
    private static final Logger LOG = LogUtil.getLogger(ServerNode.class);

    private NioEventLoopGroup worker;
    private NioEventLoopGroup boss;
    private int port;
    private String ip;

    public ServerNode(int port) {
        this.port = port;
        try {
            this.ip = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOG.error("fail to get localhost ip", e);
            ip = "127.0.0.1";
        }
        this.boss = new NioEventLoopGroup(NUM_PROCESSOR / 2);
        this.worker = new NioEventLoopGroup(NUM_PROCESSOR * 2);
    }

    /***
     * @Description 启动server 该方法为阻塞方法
     * @Auther: Zeng Hu
     * @Date: 2020/7/5 17:39
     * @return void
     **/
    @Override
    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker);
        bootstrap.channel(NioServerSocketChannel.class)
                .childHandler(new ServerChannelInitHandler())
                .option(ChannelOption.SO_BACKLOG, 1024);
        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            LOG.info("start server on port:{}", port);
            // 阻塞方法
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOG.error("start server error", e);
            stop();
        }
    }

    @Override
    public void stop() {
        worker.shutdownGracefully();
        boss.shutdownGracefully();
        LOG.info("stop server");
    }

    @Override
    public void send(Message message, Callback callback) {


    }

    @Override
    public void handle(Message message) {
        LOG.info("{}:{} receive mesage:{}", ip, port, message);


    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    private class ServerChannelInitHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new ObjectDecoder(1024 * 1024, ClassResolvers.weakCachingConcurrentResolver(ServerNode.this.getClass().getClassLoader())));
            ch.pipeline().addLast(new MessageHandler(ServerNode.this));
            ch.pipeline().addLast(new ObjectEncoder());
        }
    }

    interface MessageCallback{
        void callback(Message message);
    }
}
