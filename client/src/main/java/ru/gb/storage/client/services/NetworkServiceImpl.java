package ru.gb.storage.client.services;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import javafx.concurrent.Task;
import ru.gb.storage.client.Client;
import ru.gb.storage.client.handlers.ClientHandler;
import ru.gb.storage.client.services.interfaces.NetworkService;
import ru.gb.storage.commons.handlers.JsonDecoder;
import ru.gb.storage.commons.handlers.JsonEncoder;
import ru.gb.storage.commons.messages.Message;

public class NetworkServiceImpl implements NetworkService {
    private Channel channel;
    final NioEventLoopGroup group = new NioEventLoopGroup(1);
    private boolean connected;
    private Client client;

    public NetworkServiceImpl(Client client) {
        this.client = client;
    }

    @Override
    public void start() {
        Task<Channel> task = new Task<Channel>() {

            @Override
            protected Channel call() throws Exception {
                Bootstrap bootstrap = new Bootstrap()
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast(
                                        new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                        new LengthFieldPrepender(3),
                                        new StringDecoder(),
                                        new StringEncoder(),
                                        new JsonDecoder(),
                                        new JsonEncoder(),
                                        new ClientHandler(client)
                                );
                            }
                        });
                System.out.println("Client started");
                return bootstrap.connect("localhost", 9000).sync().channel();
            }

            @Override
            protected void succeeded() {
                channel = getValue();
                connected = true;
            }
        };
        new Thread(task).start();
    }

    @Override
    public void stop() {
        if (connected) {
            channel.close();
        }
        group.shutdownGracefully();
        connected = false;
    }

    @Override
    public void send(Message msg) {
        if(!connected){
            start();
        }
        channel.writeAndFlush(msg);
    }
}
