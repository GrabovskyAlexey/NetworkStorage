package ru.gb.storage.client.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import ru.gb.storage.client.Client;
import ru.gb.storage.commons.messages.*;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    private Client client;

    public ClientHandler(Client client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        if (message instanceof AuthResponseMessage) {
            AuthResponseMessage arm = (AuthResponseMessage) message;
            if (arm.getStatus() == MessageStatus.ERROR) {
                Platform.runLater(() -> client.getLoginController().authenticateError(arm.getText()));
            } else if (arm.getStatus() == MessageStatus.SUCCESS) {
                Platform.runLater(() -> client.getLoginController().authenticateSuccess());
            }
        }
        if (message instanceof RegisterResponseMessage) {
            RegisterResponseMessage rrm = (RegisterResponseMessage) message;
            if (rrm.getStatus() != MessageStatus.SUCCESS) {
                Platform.runLater(() -> client.getLoginController().registerError(rrm.getText()));
            } else {
                Platform.runLater(() -> client.getLoginController().registerSuccess());
            }
        }
    }
}
