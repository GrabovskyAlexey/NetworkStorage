package ru.gb.storage.commons.messages;

public class FileRequestMessage extends Message{
    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
