package de.calitobundo.twitch.desktop.chat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.calitobundo.twitch.desktop.dto.ChatItemCell;



public class ChatItem {

    public ChatItemCell itemCell = null;

    public final String name;
    public final String message;
    public final String date;
    public final String color;
    public final Map<String, String> badges;
    public final Map<String, ChatEmote> emotes;

    public final List<ChatEmotePosition> positions = new ArrayList<>();
    public final List<ChatElement> elements = new ArrayList<>();
  
    public ChatItem(String name, String message, String date, String color, Map<String, String> badges, Map<String, ChatEmote> emotes){
        
        this.name = name;
        this.message = message;
        this.date = date;
        this.color = color;
        this.badges = badges;
        this.emotes = emotes;

        emotes.forEach((id, emote) -> {
            emote.positions.forEach(pos -> {
                positions.add(pos);
            });
        });

        positions.addAll(positions.stream().sorted(Comparator.comparingInt(ChatEmotePosition::getStart)).collect(Collectors.toList()));

        int start = 0;
        for (int end = 0; end < message.length(); end++) {
            
            final char c = message.charAt(end);
            if(end == message.length() - 1 || Character.isWhitespace(c)){

                String word;
                if(end == message.length() - 1){
                    word = message.substring(start);
                }else{
                    word = message.substring(start, end);
                }

                if(!word.isBlank()){
                    elements.add(new ChatElement(start, end, word));
                }
                start = end + 1;
            }
         }

    }


}
