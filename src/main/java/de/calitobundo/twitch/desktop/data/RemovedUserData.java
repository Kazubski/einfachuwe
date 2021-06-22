package de.calitobundo.twitch.desktop.data;

import java.util.ArrayList;
import java.util.List;

public class RemovedUserData {
    
    private List<String> list = new ArrayList<>();

    public RemovedUserData() {
    }

    public RemovedUserData(List<String> list) {
        this.list.addAll(list);
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public boolean contains(String string){
        return list.contains(string);
    }
}
