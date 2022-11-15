package org.example;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;

import java.net.URI;

/**
 * @author Yuan Zhixiang
 */
public class WebSocketHandShaker extends ChannelDuplexHandler {

    public WebSocketHandShaker(URI uri) {
        handShaker = WebSocketClientHandshakerFactory
                .newHandshaker(
                        uri,
                        WebSocketVersion.V13,
                        null,
                        true,
                        new DefaultHttpHeaders()
                );
    }

    private final WebSocketClientHandshaker handShaker;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handShaker.handshake(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FullHttpResponse response) {
                if (handShaker.isHandshakeComplete()) {
                    return;
                }

                handShaker.finishHandshake(ctx.channel(), response);
                ctx.channel().writeAndFlush(new TextWebSocketFrame("xxx"));
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
