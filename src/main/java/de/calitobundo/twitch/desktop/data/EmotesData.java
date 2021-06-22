package de.calitobundo.twitch.desktop.data;

import java.util.ArrayList;
import java.util.List;

import com.github.twitch4j.kraken.domain.SimpleEmoticon;

public class EmotesData {
    
    private List<SimpleEmoticon> list = new ArrayList<>();

    public EmotesData() {
    }

    public EmotesData(List<SimpleEmoticon> list) {
        this.list.addAll(list);
    }

    public List<SimpleEmoticon> getList() {
        return list;
    }

    public void setList(List<SimpleEmoticon> list) {
        this.list = list;
    }
}
