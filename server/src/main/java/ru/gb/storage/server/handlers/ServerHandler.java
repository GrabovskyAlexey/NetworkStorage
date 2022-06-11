package ru.gb.storage.server.handlers;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.helpers.FileTransferHelper;
import ru.gb.storage.commons.messages.*;
import ru.gb.storage.server.services.AuthServiceImpl;
import ru.gb.storage.server.services.RegisterServiceImpl;
import ru.gb.storage.server.services.interfaces.AuthService;
import ru.gb.storage.server.services.interfaces.RegisterService;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    private String username;
    private String currentDir;
    private String homeDir;

    public ServerHandler() {
        System.out.println("Handler create");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (username != null) {
            System.out.println(username + " disconnected");
        } else {
            System.out.println("Unknown user disconnected");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (message instanceof RegisterRequestMessage) {
            processRegisterMessage(ctx, (RegisterRequestMessage) message);
        }
        if (message instanceof AuthRequestMessage) {
            processAuthMessage(ctx, (AuthRequestMessage) message);
        }
        if (message instanceof FileRequestMessage) {
            processFileRequestMessage(ctx, (FileRequestMessage) message);
        }
        if (message instanceof FileResponseMessage) {
            processFileResponseMessage((FileResponseMessage) message);
        }
        if(message instanceof FileListRequestMessage){
            processFileListRequestMessage(ctx, (FileListRequestMessage) message);
        }
    }

    private void processFileListRequestMessage(ChannelHandlerContext ctx, FileListRequestMessage message) {
        FileListRequestMessage msg = message;
        FileListResponseMessage response = new FileListResponseMessage();
        File dir = new File(homeDir + msg.getDirPath());
        File[] listFile = dir.listFiles();
        response.setFileList(Arrays.asList(listFile));
        ctx.writeAndFlush(response);
    }

    private void processRegisterMessage(ChannelHandlerContext ctx, RegisterRequestMessage rrm) {
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
            homeDir = "files/" + username;
            currentDir = homeDir;
            createUserFolder();
        }
        ctx.writeAndFlush(response);
    }

    private void processAuthMessage(ChannelHandlerContext ctx, AuthRequestMessage arm) {
        AuthService authService = new AuthServiceImpl();
        AuthResponseMessage response = new AuthResponseMessage();
        if (authService.authenticate(arm.getLogin(), arm.getPassword())) {
            response.setStatus(MessageStatus.SUCCESS);
            response.setText("Authenticate successful");
            username = arm.getLogin();
            homeDir = "files/" + username + "/";
            currentDir = homeDir;
            createUserFolder();
        } else {
            response.setStatus(MessageStatus.ERROR);
            response.setText("Wrong login or password");
        }
        ctx.writeAndFlush(response);
    }

    private void processFileRequestMessage(ChannelHandlerContext ctx, FileRequestMessage message) throws IOException {
        File file = new File(currentDir + message.getFilename());
        System.out.println(file.toString());
        if (!file.exists()) {
            FileNotFoundMessage response = new FileNotFoundMessage();
            response.setErrorText("File " + file.getName() + " not found");
            ctx.writeAndFlush(response);
        } else {
            sendFile(ctx, new FileTransferHelper(file));
        }
    }


    private void createUserFolder() {
        File userDir = new File(currentDir);
        if (!userDir.exists()) {
            userDir.mkdirs();
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

    private void processFileResponseMessage(FileResponseMessage message){
        try (RandomAccessFile file = new RandomAccessFile(message.getFilename(), "rw")) {
            file.seek(message.getStartPosition());
            file.write(message.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


