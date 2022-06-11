package ru.gb.storage.commons.messages;

public class FileListRequestMessage extends Message{
    private String dirPath;

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }
}
