package ru.gb.storage.commons.messages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListResponseMessage extends Message{
    List<File> fileList = new ArrayList<>();

    public List<File> getFileList() {
        return fileList;
    }

    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
    }
}
