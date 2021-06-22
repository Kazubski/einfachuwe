package de.calitobundo.twitch.desktop.chat;

public class ChatElement {
    
    public String word;
    public int start;
    public int end;

    public ChatElement(int start, int end, String word){
        this.start = start;
        this.end = end;
        this.word = word;
    }

}
