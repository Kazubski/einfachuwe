package de.calitobundo.twitch.desktop.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;

public class ChatEmote {

    public final static Map<String, Image> images = new HashMap<>();
    public final static String url = "https://static-cdn.jtvnw.net/emoticons/v1/%s/%s";

    public final String id;
    public List<ChatEmotePosition> positions = new ArrayList<>();

    public ChatEmote(String string) {

        String[] emote = string.split(":");
        id = emote[0];

        String[] pos_array = emote[1].split(",");

        for (String arr : pos_array) {
            String[] pos = arr.split("-");
            int start = Integer.parseInt(pos[0]);
            int end = Integer.parseInt(pos[1]);
            ChatEmotePosition cep = new ChatEmotePosition(start, end, this);
            positions.add(cep);
        }

    }

    private String getUrl() {
        return String.format(url, id, "1.0");
    }

    private String getUrl(String version) {
        return String.format(url, id, version);
    }

    public Image getImage(int size2, String version){
        int size = size2+4;
        Image image = images.get(id+size);
        if(image == null){
            image = new Image(getUrl(version), size, size, true, true);
            images.put(id+size, image);
        }
        return image;
    }

    public Image getImage(int size2){
        int size = size2+4;
        Image image = images.get(id+size);
        if(image == null){
            image = new Image(getUrl(), size, size, true, true);
            images.put(id+size, image);
        }
        return image;
    }


}