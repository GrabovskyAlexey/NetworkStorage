package ru.gb.storage.commons.messages;

public class FileNotFoundMessage extends Message{
    private String errorText;

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
}
