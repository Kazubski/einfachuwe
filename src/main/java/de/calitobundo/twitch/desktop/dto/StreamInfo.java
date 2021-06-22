package de.calitobundo.twitch.desktop.dto;


import com.github.twitch4j.helix.domain.ChannelInformation;
import com.github.twitch4j.helix.domain.StreamTag;

import java.util.List;

public class StreamInfo {

    private String broadcasterId;
    private String broadcasterName;
    private String broadcasterLanguage;
    private String gameId;
    private String gameName;
    private String boxArtUrl;
    private String title;
    private List<StreamTag> tags;

    public StreamInfo() {
    }

    public StreamInfo(ChannelInformation channelInformation, List<StreamTag> tags) {
        this.broadcasterId = channelInformation.getBroadcasterId();
        this.broadcasterName = channelInformation.getBroadcasterName();
        this.broadcasterLanguage = channelInformation.getBroadcasterLanguage();
        this.gameId = channelInformation.getGameId();
        this.gameName = channelInformation.getGameName();
        this.title = channelInformation.getTitle();
        this.tags = tags;
    }

    public String getBroadcasterId() {
        return broadcasterId;
    }

    public void setBroadcasterId(String broadcasterId) {
        this.broadcasterId = broadcasterId;
    }

    public String getBroadcasterName() {
        return broadcasterName;
    }

    public void setBroadcasterName(String broadcasterName) {
        this.broadcasterName = broadcasterName;
    }

    public String getBroadcasterLanguage() {
        return broadcasterLanguage;
    }

    public void setBroadcasterLanguage(String broadcasterLanguage) {
        this.broadcasterLanguage = broadcasterLanguage;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getBoxArtUrl() {
        return boxArtUrl;
    }

    public void setBoxArtUrl(String boxArtUrl) {
        this.boxArtUrl = boxArtUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<StreamTag> getTags() {
        return tags;
    }

    public void setTags(List<StreamTag> tags) {
        this.tags = tags;
    }
}
