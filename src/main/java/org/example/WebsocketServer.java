package org.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketServerExtensionHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateServerExtensionHandshaker;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author Yuan Zhixiang
 */
public class WebsocketServer {

    public static void main(String[] args) {
        NioEventLoopGroup bosses = new NioEventLoopGroup();
        NioEventLoopGroup workers = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(bosses, workers)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new CustomWebSocketServerExtensionHandler())
                                .addLast(new WebSocketServerProtocolHandler(
                                        WebSocketServerProtocolConfig.newBuilder()
                                                .websocketPath("/")
                                                .subprotocols(null)
                                                .allowExtensions(true)
                                                .checkStartsWith(true)
                                                .build()
                                ))
                                .addLast(new WebSocketFrameAggregator(65536))
                                .addLast(new ChannelDuplexHandler() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        if (msg instanceof TextWebSocketFrame textFrame) {
                                            try {
                                                ctx.channel().eventLoop()
                                                        .scheduleAtFixedRate(() -> writeAndFlush(body, ctx.channel()), 0, 1, TimeUnit.MILLISECONDS);
                                            } finally {
                                                ReferenceCountUtil.release(msg);
                                            }
                                        }
                                    }
                                })
                        ;
                    }
                });
        serverBootstrap.bind(10443);
    }

    static void writeAndFlush(String body, Channel channel) {
        for (int i = 0; i < 1; i++) {
            channel.write(new TextWebSocketFrame(body));
        }
        channel.flush();
    }

    private static final String body = "1111121231231231231kj3k1iub3b1pi2u3i123p12839h32z9rhs9uned19u23x-bn1e3- f1" +
            "aslkfj23j41n23n qwkefj9-9nn1`]-03i-0-k;;l23morijaf98jakj f 2 2enoinadpfn230909'a'efmoi23no;asf[oqeworn" +
            ";sdofjperhq-34r238jr-03nfn1111121231231231231kj3k1iub3b1pi2u3i123p12839h32z9rhs9uned19u23x-bn1e3- f1iusndvuuwenf9ie" +
            "a;odsnfqn3npdpwmqef-qwrjq'fmk129839hfsaidhf0erqieuhfniabvoadh09q2b34r qwiuefh0q8h239n[q'f'fj[foi1p29norouebf9q" +
            ";sdofjperhq-34r238jr-03nfn1111121231231231231kj3k1iub3b1pi2u3i123p12839h32z9rhs9uned19u23x-bn1e3- f1iusndvuuwenf9ie" +
            "1o2i3j12938u9ahncoa dbv80hqh349r8hb92qu4roj qwbf9uqh39pfhjnerouh93rj'o[kpoq" +
            "mfq'pforjmqpoinfoquh23r[oqn 4f'oq3k n4g'oqn3i4hgf[nqj3bng foq ebgfgqi;n43 foqiu3hg[3qi4gnorbgoiquhfnq;e jflqjo3 rkj fg;gr " +
            " qe foqb flqfe lqe fqk ejwf;kqj ef g;iqb rgiq3 fipqbnfqo3nf'q;fenqipubfoqjk23ub4hr9  ub23ripqb fihberiuqbn4rfoiuq294ubf qj2fiqhwfhuq3b4f oqewf" +
            "";
}

class CustomWebSocketServerExtensionHandler extends WebSocketServerExtensionHandler {
    public CustomWebSocketServerExtensionHandler() {
        super(new PerMessageDeflateServerExtensionHandshaker(
                1,
                ZlibCodecFactory.isSupportingWindowSizeAndMemLevel(),
                PerMessageDeflateServerExtensionHandshaker.MAX_WINDOW_SIZE,
                true,
                true
        ));
    }
}
