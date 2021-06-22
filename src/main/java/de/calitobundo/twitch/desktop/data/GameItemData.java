package de.calitobundo.twitch.desktop.data;

import java.util.ArrayList;
import java.util.List;


import de.calitobundo.twitch.desktop.views.LiveStreamsView.GameItem;

public class GameItemData {
    
    private List<GameItem> gameItems = new ArrayList<>();

    public GameItemData() {
    }

    public GameItemData(List<GameItem> list) {
        gameItems.addAll(list);
    }

    public List<GameItem> getGameItems() {
        return gameItems;
    }

    public void setGameItems(List<GameItem> gameItems) {
        this.gameItems = gameItems;
    }
}
