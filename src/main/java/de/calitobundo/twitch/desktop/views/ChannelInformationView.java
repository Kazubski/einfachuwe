package de.calitobundo.twitch.desktop.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import com.github.twitch4j.helix.domain.ChannelInformation;
import com.github.twitch4j.helix.domain.ChannelInformationList;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.User;

import org.apache.commons.lang3.time.DurationFormatUtils;

import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.api.Fetch;
import de.calitobundo.twitch.desktop.event.EventHandler;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ChannelInformationView extends VBox {
    
    private final Label channelNameLabel = new Label("Channel");
    private final Label viewCountLabel = new Label("Viewer");
    private final Label uptimeLabel = new Label("Uptime");
    private final Label channelNameValueLabel = new Label("EinfachUwe42");
    private final Label viewCountValueLabel = new Label("0");
    private final Label uptimeValueLabel = new Label("00:00:00");

    private final Region spacer1 = new Region();
    private final Region spacer2 = new Region();

    private final TextField titelTextField = new TextField();
    private final Button saveButton = new Button("Save");

    private final Timer updateTimer = new Timer(true);
    private boolean started = false;
    private final EventHandler handler;

    public ChannelInformationView(EventHandler handler){
        this.handler = handler;

        channelNameValueLabel.setStyle(Context.cssRedColor);
        viewCountValueLabel.setStyle(Context.cssRedColor);
        uptimeValueLabel.setStyle(Context.cssRedColor);
        spacer1.setPrefWidth(10);
        spacer2.setPrefWidth(10);

        final HBox informationLayout = new HBox();
        informationLayout.setSpacing(10);
        informationLayout.getChildren().addAll(channelNameLabel, channelNameValueLabel, spacer1, viewCountLabel, viewCountValueLabel, spacer2,  uptimeLabel, uptimeValueLabel);
        HBox.setHgrow(titelTextField, Priority.ALWAYS);

        final HBox inputLayout = new HBox();
        inputLayout.setSpacing(10);
        inputLayout.getChildren().addAll(titelTextField, saveButton);
        HBox.setHgrow(titelTextField, Priority.ALWAYS);

        getChildren().addAll(informationLayout, inputLayout);
        setSpacing(10);

        saveButton.setOnAction(e -> {
            saveButton.setDisable(true);
            new Thread(() -> {
                final User user = Context.getChannelUser();
                final ChannelInformation channelInformation = new ChannelInformation()
                        .withTitle(titelTextField.getText())
                        .withBroadcasterLanguage("de");
                Context.twitchClient.getHelix().updateChannelInformation(Context.credential.getAccessToken(), user.getId(), channelInformation).execute();
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                });
            }).start();
        });
    }

    public void start(){
        if(started)
            return;
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateChannelInformation();
            }
        }, 1000, 10000);
        started = true;
    }

    public void stop(){
        updateTimer.cancel();
        started = false;
    }

    public void updateChannelInformation() {

        final User user = Context.getChannelUser();

        new Thread(() -> {

            final Optional<ChannelInformation> channelInformationOptional = fetchChannelInformationOptional(user.getId());
            if(channelInformationOptional.isPresent()){
                final ChannelInformation channelInformation = channelInformationOptional.get();
                final String titel = channelInformation.getTitle().replaceAll(Context.regexLabel, "");
                Platform.runLater(() -> {
                    titelTextField.setText(titel);
                });
            }else{
                Platform.runLater(() -> {
                    titelTextField.clear();
                });
            }

            final Optional<Stream> streamOptional = Fetch.fetchStreamInfoOptional(user.getLogin());
            final ChannelStatsItem statsItem = new ChannelStatsItem();
            if(streamOptional.isPresent()){
                final Stream stream = streamOptional.get();
                Platform.runLater(() -> {
                    channelNameValueLabel.setText(stream.getUserName());
                    viewCountValueLabel.setText(String.valueOf(stream.getViewerCount()));
                    if(stream.getUptime().toMillis() >= 0){
                        final String time = DurationFormatUtils.formatDuration(stream.getUptime().toMillis(), "HH:mm:ss");
                        uptimeValueLabel.setText(time); 
                    }
                    statsItem.viewerCount = stream.getViewerCount();
                });
            }else{
                    statsItem.viewerCount = 0;
            }
            statsItem.chatterCount = handler.getGraphUsers().stream().filter(item -> item.joined && !item.ignored).count();
            statsItem.leavedCount = handler.getGraphUsers().stream().filter(item -> !item.ignored).count();
            statsItem.followersCount = handler.getGraphUsers().stream().filter(item -> item.joined && item.followed).count();
            stats.add(statsItem);
            
        }).start();
    }

    private static Optional<ChannelInformation> fetchChannelInformationOptional(String userId){
        final ChannelInformationList infoList = Context.twitchClient.getHelix().getChannelInformation(Context.credential.getAccessToken(), Collections.singletonList(userId)).execute();
        return infoList.getChannels().stream().findFirst();
    }

    public static List<ChannelStatsItem> stats = new ArrayList<>();

    public class ChannelStatsItem {

        public final long time;
        public long viewerCount;
        public long chatterCount;
        public long leavedCount;
        public long followersCount;
        public ChannelStatsItem(){
            this.time = System.currentTimeMillis();
        }

        public long time(){
            return time;
        }
        public long viewerCount(){
            return viewerCount;
        }
        public long chatterCount(){
            return chatterCount;
        }
        public long leavedCount(){
            return leavedCount;
        }
        public long followersCount(){
            return followersCount;
        }
    }

}
