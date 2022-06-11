package ru.gb.storage.client.handlers;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import ru.gb.storage.client.Client;
import ru.gb.storage.commons.helpers.FileTransferHelper;
import ru.gb.storage.commons.messages.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    private Client client;

    public ClientHandler(Client client) {
        this.client = client;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("Channel inactive");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (message instanceof AuthResponseMessage) {
            processAuthMessage((AuthResponseMessage) message);
        }
        if (message instanceof RegisterResponseMessage) {
            processRegisterMessage((RegisterResponseMessage) message);
        }
        if (message instanceof FileResponseMessage) {
            processFileResponseMessage((FileResponseMessage) message);
        }
        if (message instanceof FileRequestMessage) {
            processFileRequestMessage(ctx, (FileRequestMessage) message);
        }

    }

    private void processRegisterMessage(RegisterResponseMessage message) {
        RegisterResponseMessage rrm = message;
        if (rrm.getStatus() != MessageStatus.SUCCESS) {
            Platform.runLater(() -> client.getLoginController().registerError(rrm.getText()));
        } else {
            Platform.runLater(() -> client.getLoginController().registerSuccess());
        }
    }

    private void processAuthMessage(AuthResponseMessage message) {
        AuthResponseMessage arm = message;
        if (arm.getStatus() == MessageStatus.ERROR) {
            Platform.runLater(() -> client.getLoginController().authenticateError(arm.getText()));
        } else if (arm.getStatus() == MessageStatus.SUCCESS) {
            Platform.runLater(() -> client.getLoginController().authenticateSuccess());
        }
    }

    private void processFileResponseMessage(FileResponseMessage message){
        try (RandomAccessFile file = new RandomAccessFile("D:\\test\\" + message.getFilename(), "rw")) {
            file.seek(message.getStartPosition());
            file.write(message.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processFileRequestMessage(ChannelHandlerContext ctx, FileRequestMessage message) throws IOException {
        File file = new File(message.getFilename());
        if (file.exists()) {
            sendFile(ctx, new FileTransferHelper(file));
        }
    }

    private void sendFile(ChannelHandlerContext ctx, FileTransferHelper helper) throws IOException {
        FileResponseMessage response = helper.getNextPart();
        ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
            if (helper.hasNextPart()) {
                sendFile(ctx, helper);
            } else {
                helper.close();
            }
        });
    }
}
