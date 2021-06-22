package de.calitobundo.twitch.desktop.event;

import com.github.twitch4j.chat.events.channel.ChannelJoinEvent;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.calitobundo.twitch.desktop.dto.UserItem;
import de.calitobundo.twitch.desktop.graph.GraphUser;
import javafx.stage.Stage;

import java.net.URI;
import java.util.List;

public interface EventHandler {


    void joined(ChannelJoinEvent event);
    void leaved(ChannelLeaveEvent event);


    void joinChannel(String channelName);

    void findUser(String userName);

    void addChatMessage(ChannelMessageEvent event);
    void addLogMessage(String message);

    EventService getService();

    void openWebsite(URI uri);

    void addChattersTab(String channelName);

    void ignoreUser(String name, boolean ignore);
    void removeUser(String name, boolean ignore);


    
    void onFollowToSize(int size);
    void onFollowFromSize(int size);
    void onClipsSize(int size);

    List<GraphUser> getGraphUsers();

    void showUserListView(boolean show);

    Stage getPrimaryStage();

   

}
