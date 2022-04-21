package ru.gb.storage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.messages.*;
import ru.gb.storage.server.services.AuthServiceImpl;
import ru.gb.storage.server.services.RegisterServiceImpl;
import ru.gb.storage.server.services.interfaces.AuthService;
import ru.gb.storage.server.services.interfaces.RegisterService;

import java.io.File;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    private String username;

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        if(username != null) {
            System.out.println(username + " disconnected");
        } else {
            System.out.println("Unknown user disconnected");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (message instanceof RegisterRequestMessage) {
            RegisterRequestMessage rrm = (RegisterRequestMessage) message;
            RegisterResponseMessage response = new RegisterResponseMessage();
            RegisterService registerService = new RegisterServiceImpl();
            if (registerService.checkExistLogin(rrm.getLogin())) {
                response.setStatus(MessageStatus.ALREADY_EXISTS);
                response.setText("User with login `" + rrm.getLogin() + "' already exist");
            } else {
                registerService.register(rrm.getLogin(), rrm.getPassword());
                response.setStatus(MessageStatus.SUCCESS);
                response.setText("User with login `" + rrm.getLogin() + "' success register");
                username = rrm.getLogin();
                createUserFolder();
            }
            ctx.writeAndFlush(response);
        }
        if (message instanceof AuthRequestMessage) {
            AuthRequestMessage arm = (AuthRequestMessage) message;
            AuthService authService = new AuthServiceImpl();
            AuthResponseMessage response = new AuthResponseMessage();
            if (authService.authenticate(arm.getLogin(), arm.getPassword())) {
                response.setStatus(MessageStatus.SUCCESS);
                response.setText("Authenticate successful");
                username = arm.getLogin();
                createUserFolder();
            } else {
                response.setStatus(MessageStatus.ERROR);
                response.setText("Wrong login or password");
            }
            ctx.writeAndFlush(response);
        }
    }

    private void createUserFolder() {
        File userDir = new File("files/" + username);
        if(!userDir.exists()){
            userDir.mkdirs();
        }
    }
}
