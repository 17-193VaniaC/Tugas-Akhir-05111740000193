package com.example.bandungzoochatbot;

import java.util.ListIterator;

public class ChatMessage {

    private Boolean isMine;
    private String message;

    public ChatMessage(boolean isMine, String message){
        this.isMine = isMine;
        this.message = message;
    }

    public void setMine(boolean mine){
        isMine = mine;
    }

    public void setMessage(String text){
        message = text;
    }

    public boolean isMine(){
        return isMine;
    }

    public String getMessage(){
        return message;
    }

}
