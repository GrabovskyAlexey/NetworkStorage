package ru.gb.storage.client.services.interfaces;

import ru.gb.storage.commons.messages.Message;

public interface NetworkService {
    void start() throws InterruptedException;
    void stop();
    void send(Message msg) throws InterruptedException;
}
