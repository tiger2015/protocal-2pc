package com.tiger.distributeprotocol.handler;

import com.tiger.distributeprotocol.node.Node;
import com.tiger.distributeprotocol.common.LogUtil;
import com.tiger.distributeprotocol.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/5 20:41
 * @Description:
 * @Version: 1.0
 **/
public class MessageHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LogUtil.getLogger(MessageHandler.class);
    private Node node;

    public MessageHandler(Node node) {
        this.node = node;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        node.handle((Message) msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("node - {}:{} is inactive", node.getIp(), node.getPort());
        ctx.channel().eventLoop().schedule(()->node.start(), 3, TimeUnit.SECONDS);
    }
}
