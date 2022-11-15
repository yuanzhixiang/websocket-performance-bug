package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateClientExtensionHandshaker;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateServerExtensionHandshaker.MAX_WINDOW_SIZE;

/**
 * @author Yuan Zhixiang
 */
public class WebsocketClient {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException {

        NioEventLoopGroup group = new NioEventLoopGroup(8);
        for (int i = 0; i < 16; i++) {
            connect(group);
        }

    }

    private static void connect(NioEventLoopGroup group) throws URISyntaxException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException {

        URI uri = new URI("ws://127.0.0.1:10443/ws/v5/public");

        Bootstrap bootstrap = new Bootstrap();

        WebSocketHandShaker webSocketHandShaker = new WebSocketHandShaker(uri);
        bootstrap
                .channel(NioSocketChannel.class)
                .group(group)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new IdleStateHandler(0, 0, 20))
                                .addLast(new WebSocketFrameAggregator(65536))
                                .addLast(new CustomWebsocketClientExtensionHandler())
                                .addLast(webSocketHandShaker)
                        ;
                    }
                });
        bootstrap.connect(uri.getHost(), uri.getPort());
    }
}

class CustomWebsocketClientExtensionHandler extends WebSocketClientExtensionHandler {
    public CustomWebsocketClientExtensionHandler() {
        super(new PerMessageDeflateClientExtensionHandshaker(6, ZlibCodecFactory.isSupportingWindowSizeAndMemLevel(), MAX_WINDOW_SIZE, false, true));
    }
}
