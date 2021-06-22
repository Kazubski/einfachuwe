package de.calitobundo.twitch.desktop.views;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.github.twitch4j.helix.domain.ChannelInformation;
import com.github.twitch4j.helix.domain.ChannelInformationList;
import com.github.twitch4j.helix.domain.ChannelSearchList;
import com.github.twitch4j.helix.domain.ChannelSearchResult;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamTag;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.tmi.domain.Chatters;

import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.api.Fetch;
import de.calitobundo.twitch.desktop.event.EventHandler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class UserChannelView extends VBox {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private final Label isLiveLabelLabel = new Label("isLive");
    private final Label viewerCountLabel = new Label("viewerCount");
    private final Label languageLabel = new Label("language");
    private final Label titleLabel = new Label("title");
    private final Label gameNameLabel = new Label("gameName");
    private final Label startedAtLabel = new Label("startedAt");
    private final Label tagsLabel = new Label("tags");

    private final Label chattersLabel = new Label("chatters");

    private final TextField isLiveTextField = new TextField("isLive");
    private final TextField viewerCountTextField = new TextField("viewerCount");
    private final TextField languageTextField = new TextField("language");
    private final TextArea titleTextArea = new TextArea("title");
    private final TextField gameNameTextField = new TextField("gameName");
    private final TextField startedAtTextField = new TextField("startedAt");
    private final TextField tagsTextField = new TextField("tags");

    private final TextArea chattersTextArea = new TextArea("chatters");

    private final EventHandler handler;

    public  UserChannelView(EventHandler handler){
        this.handler = handler;

        final GridPane streamLayout = new GridPane();
        VBox.setVgrow(streamLayout, Priority.ALWAYS);

        streamLayout.setVgap(5);
        //streamLayout.setHgap(5);
        isLiveLabelLabel.setMinWidth(100);
        streamLayout.add(isLiveLabelLabel, 0, 0);
        streamLayout.add(isLiveTextField, 1, 0);
        streamLayout.add(viewerCountLabel, 0, 1);
        streamLayout.add(viewerCountTextField, 1, 1);
        streamLayout.add(languageLabel, 0, 2);
        streamLayout.add(languageTextField, 1, 2);
        streamLayout.add(titleLabel, 0, 3);
        streamLayout.add(titleTextArea, 1, 3);
        streamLayout.add(gameNameLabel, 0, 4);
        streamLayout.add(gameNameTextField, 1, 4);
        streamLayout.add(startedAtLabel, 0, 5);
        streamLayout.add(startedAtTextField, 1, 5);
        streamLayout.add(tagsLabel, 0, 6);
        streamLayout.add(tagsTextField, 1, 6);
        streamLayout.add(chattersLabel, 0, 7);
        streamLayout.add(chattersTextArea, 1, 7);

        isLiveTextField.setEditable(false);
        viewerCountTextField.setEditable(false);
        languageTextField.setEditable(false);
        titleTextArea.setEditable(false);
        titleTextArea.setWrapText(true);
        titleTextArea.setMinHeight(60);
        titleTextArea.setPrefHeight(60);
        gameNameTextField.setEditable(false);
        startedAtTextField.setEditable(false);
        tagsTextField.setEditable(false);
        chattersTextArea.setEditable(false);

        getChildren().addAll(streamLayout);
        setSpacing(10);
        setPadding(new Insets(10, 0, 0, 0));

        GridPane.setVgrow(chattersTextArea, Priority.ALWAYS);



    }


    public void setUser(User user){
        fetchUserChannelSearch(user);
        fetchUserChannelInformation(user);
        fetchUserStreamTags(user);
        fetchChatters(user);

            new Thread(() -> {
                final Stream stream = Fetch.fetchStreamInfo(user.getLogin());

                if(stream == null){
                    Platform.runLater(() -> {
                        viewerCountTextField.setText("");
                    });
                }else{
                    Platform.runLater(() -> {
                        //stream.getViewerCount();
                        //stream.getUptime();
                        viewerCountTextField.setText(String.valueOf(stream.getViewerCount()));

                    });
                }
            }).start();
    }
    

    private void fetchUserChannelSearch(User user) {

        new Thread(() -> {

            // users channel search result
            final ChannelSearchList list = Context.twitchClient.getHelix().searchChannels(Context.credential.getAccessToken(), user.getLogin(), null,  null, false).execute();
            final List<ChannelSearchResult> results = list.getResults().stream().filter(s -> s.getDisplayName().equalsIgnoreCase(user.getLogin())).collect(Collectors.toList());
            if(!results.isEmpty()){
                final ChannelSearchResult channelSearchResult = results.get(0);
                Platform.runLater(() -> {
                    //stream.getTagsIds();
                    //stream.getThumbnailUrl();
                    //stream.getDisplayName();
                    //stream.getGameId();
                    //liveTumbnailImageView.setImage(new Image(channelSearchResult.getThumbnailUrl(), 96, 96, false, false));

                    isLiveTextField.setText(String.valueOf(channelSearchResult.getIsLive()));

                    if (channelSearchResult.getStartedAt() != null) {

                        final Date myDate = Date.from(channelSearchResult.getStartedAt());
                        final String formattedDate = formatter.format(myDate);
                        startedAtTextField.setText(formattedDate);
                        //liveImageView.setImage(Context.liveImage);

                    }else{
                        startedAtTextField.setText("Innerhalb der letzen 6 Monaten");
                        //liveImageView.setImage(Context.notLiveImage);
                    }
                });
            }else{
                Platform.runLater(() -> {
                    startedAtTextField.clear();
                    isLiveTextField.clear();

                    //liveTumbnailImageView.setImage(null);
                });
            }
        }).start();

    }

    private void fetchUserChannelInformation(User user){

        new Thread(() -> {
            // users channel information
            final ChannelInformationList infoList = Context.twitchClient.getHelix().getChannelInformation(Context.credential.getAccessToken(), Collections.singletonList(user.getId())).execute();
            final List<ChannelInformation> infoList2 = infoList.getChannels();
            if(infoList2 != null && !infoList2.isEmpty()){
                final ChannelInformation info = infoList2.get(0);
                Platform.runLater(() -> {
                    languageTextField.setText(info.getBroadcasterLanguage());
                    gameNameTextField.setText(info.getGameName());

                    String resultString = info.getTitle().replaceAll(Context.regexLabel, "");
                    titleTextArea.setText(resultString);
                    //titleTextArea.setText(info.getTitle().replace("\n", ""));
                });
            }else{
                Platform.runLater(() -> {
                    languageTextField.clear();
                    gameNameTextField.clear();
                    titleTextArea.clear();
                });
            }
        }).start();
    }

    private void fetchChatters(User user) {

        chattersTextArea.clear();
        Utils.getChatters(chatters -> {
            chattersTextArea.appendText("Chatters "+chatters.getViewerCount()+"\n");
             if(!chatters.getBroadcaster().isEmpty()){
                chattersTextArea.appendText("\n Broadcaster:".concat("\n"));
                chatters.getBroadcaster().forEach(chatter -> {
                    chattersTextArea.appendText(chatter.concat("\n"));
                });
            }
            if(!chatters.getModerators().isEmpty()){
                chattersTextArea.appendText("\n Moderators:".concat("\n"));
                chatters.getModerators().forEach(chatter -> {
                    chattersTextArea.appendText(chatter.concat("\n"));
                });
            }
            if(!chatters.getVips().isEmpty()){
                chattersTextArea.appendText("\n Vips:".concat("\n"));
                chatters.getVips().forEach(chatter -> {
                    chattersTextArea.appendText(chatter.concat("\n"));
                });
            }
            if(!chatters.getViewers().isEmpty()){
                chattersTextArea.appendText("\n Viewers:".concat("\n"));
                chatters.getViewers().forEach(chatter -> {
                    chattersTextArea.appendText(chatter.concat("\n"));
                });
            }
       }, user);
    }

    public static class Utils {

        public static void getChatters(CallMe<Chatters> callme, User user){

            new Thread(() -> {
                final Chatters chatters = Context.twitchClient.getMessagingInterface().getChatters(user.getLogin()).execute();
                Platform.runLater(() -> {
                    callme.weiter(chatters);
                });
    
            }).start();
        }

    }

    @FunctionalInterface
    public interface CallMe<T> {
        void weiter(T result);
    }

    private void fetchUserStreamTags(User user) {
        new Thread(() -> {
            final List<StreamTag> tags = Fetch.fetchTagsByUserId(user.getId());
            final String str = tags.stream().map(tag -> tag.getLocalizationNames().get("de-de")).collect(Collectors.joining(" | "));
            Platform.runLater(() -> {
                tagsTextField.setText(str);
            });
        }).start();
    }
}
