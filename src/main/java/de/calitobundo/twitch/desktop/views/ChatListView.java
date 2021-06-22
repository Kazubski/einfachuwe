package de.calitobundo.twitch.desktop.views;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.tmi.domain.BadgeSets;

import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.api.UweColors;
import de.calitobundo.twitch.desktop.chat.ChatEmote;
import de.calitobundo.twitch.desktop.chat.ChatItem;
import de.calitobundo.twitch.desktop.dto.ChatItemCell;
import de.calitobundo.twitch.desktop.event.EventHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class ChatListView extends ListView<ChatItem> {

    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
    private final ObservableList<ChatItem> observableList = FXCollections.observableArrayList();

    public static BadgeSets badgesSet = null;

    public ChatListView(EventHandler handler) {

        setItems(observableList);
        setCellFactory(listView -> new ChatListCell(handler));

        setId("chatListView");
        String css = ChatListView.class.getClassLoader().getResource("css/chat_listview.css").toExternalForm();
        getStylesheets().add(css);
        setFocusTraversable(false);
        //setMouseTransparent(true);   
    }


    public void clear() {
        observableList.clear();
    }

    public void setSize(int size) {
        observableList.forEach(item -> {
            item.itemCell.resize(size);
        });
        refresh();
    }

    public void addChatMessage(ChannelMessageEvent event) {

        final String emotesRawString = event.getMessageEvent().getTags().get("emotes");
        final Map<String, ChatEmote> emotes = new HashMap<>();

        if(emotesRawString != null){
            String[] emotesString = emotesRawString.split("/");
            for (String string : emotesString) {
                ChatEmote emote = new ChatEmote(string);
                emotes.put(emote.id, emote);
            }
        }

        // System.out.println("------getBadges---");
        // event.getMessageEvent().getBadges().forEach((key, value) -> {
        //     System.out.println(key+" "+value);
        // });
        // System.out.println("------getTags---");
        // event.getMessageEvent().getTags().forEach((key, value) -> {
        //     System.out.println(key+" "+value);
        // });
        // System.out.println("------getRawTags---");
        // event.getMessageEvent().getRawTags().forEach((key, value) -> {
        //     System.out.println(key+" "+value);
        // });
        // System.out.println("---------");

        String color = (String)event.getMessageEvent().getRawTags().get("color");

        if(color == null){
            color = UweColors.hexColorGray;
        }
        final String displayName = (String)event.getMessageEvent().getRawTags().get("display-name");
        final String formattedTime = timeFormatter.format(Date.from(event.getFiredAt().toInstant()));
        String cleanedMessage = event.getMessage().replaceAll(Context.regexLabel, "X");

        final ChatItem item = new ChatItem(displayName, cleanedMessage, formattedTime, color, event.getMessageEvent().getBadges(), emotes);

        final ChatItemCell itemCell = new ChatItemCell(item);
        item.itemCell = itemCell;

        getItems().add(item);
    }


    public static class ChatListCell extends ListCell<ChatItem> {

         public ChatListCell(EventHandler handler) {

            setStyle("-fx-padding: 3 10 3 10; -fx-margin: 0;");
            setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {   
                    if (getItem() == null)
                        return;
                    handler.findUser(getItem().name.toLowerCase());
                }
            });
        }

        @Override
        protected void updateItem(ChatItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {

                setText(null);
                setGraphic(null);
                setId("chat-item-cell-empty");

            } else {

                setText(null);
                setGraphic(item.itemCell.textFlow);
                setId("chat-item-cell");

            }
        }
    }


 

}
