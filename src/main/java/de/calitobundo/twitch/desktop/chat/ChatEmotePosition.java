package de.calitobundo.twitch.desktop.chat;

public class ChatEmotePosition {

    public final int start;
    public final int end;
    public final ChatEmote emote;

    public ChatEmotePosition(int start, int end, ChatEmote emote){
        this.start = start;
        this.end = end;
        this.emote = emote;
    }

    public int getStart(){
        return start;
    }
}