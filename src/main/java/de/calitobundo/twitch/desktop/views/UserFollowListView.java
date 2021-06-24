package de.calitobundo.twitch.desktop.views;

import static de.calitobundo.twitch.desktop.api.Context.credential;
import static de.calitobundo.twitch.desktop.api.Context.twitchClient;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.twitch4j.helix.domain.Follow;
import com.github.twitch4j.helix.domain.FollowList;
import com.github.twitch4j.helix.domain.User;

import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.api.Fetch;
import de.calitobundo.twitch.desktop.api.FollowUtils;
import de.calitobundo.twitch.desktop.dto.FollowItem;
import de.calitobundo.twitch.desktop.event.EventHandler;
import de.calitobundo.twitch.desktop.graph.GraphUser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;


public class UserFollowListView extends HBox {

    private final Button loadFromButton = new Button("Load");
    private final ProgressBar fromProgressBar = new ProgressBar();
    private final Label fromTotalLable = new Label("follows from user");
    private final Label chattersFromLabel = new Label("chatters");

    private final ObservableList<FollowItem> observableFrom = FXCollections.observableArrayList();
    private final ListView<FollowItem> fromListView = new ListView<>(observableFrom);

    private final Button loadToButton = new Button("Load");
    private final ProgressBar toProgressBar = new ProgressBar();
    private final Label toTotalLable = new Label("follows to user");
    private final Label chattersToLabel = new Label("chatters");

    private final ObservableList<FollowItem> observableTo = FXCollections.observableArrayList();
    private final ListView<FollowItem> toListView = new ListView<>(observableTo);
    
    private final EventHandler handler;

    private static Set<String> result = new HashSet<>();

    public UserFollowListView(EventHandler handler) {
        this.handler = handler;

        chattersFromLabel.setStyle(Context.cssGreenColor);
        chattersToLabel.setStyle(Context.cssGreenColor);

        fromListView.setCellFactory(listview ->new UserFollowFromListCell());
        toListView.setCellFactory(listview ->new UserFollowToListCell());

        final VBox fromlayout = new VBox(loadFromButton, fromProgressBar, chattersFromLabel, fromTotalLable, fromListView);
        fromlayout.setSpacing(10);
        VBox.setVgrow(fromListView, Priority.ALWAYS);
        HBox.setHgrow(fromlayout, Priority.ALWAYS);
        fromProgressBar.setMaxWidth(Double.MAX_VALUE);

        final VBox tolayout = new VBox(loadToButton, toProgressBar, chattersToLabel, toTotalLable, toListView);
        tolayout.setSpacing(10);
        VBox.setVgrow(toListView, Priority.ALWAYS);
        HBox.setHgrow(tolayout, Priority.ALWAYS);
        toProgressBar.setMaxWidth(Double.MAX_VALUE);

        HBox box = new HBox(fromlayout, tolayout);
        box.setSpacing(10);
        VBox.setVgrow(box, Priority.ALWAYS);

        getChildren().addAll(box);
        setSpacing(10);
        setPadding(new Insets(10, 0, 0, 0));

        loadFromButton.setOnAction(e -> {
                
            new Thread(() -> {

                final List<Follow> followerList = Fetch.followersFromUserId(new FetchEvent(fromProgressBar), null, new ArrayList<>(), user.getId(), Context.twitchClient, Context.credential);
                List<FollowItem> followItems = FollowUtils.mapToFollowItemsFrom(followerList);
                updateFrom(followItems);

            }).start();
        });

        loadToButton.setOnAction(e -> {

            new Thread(() -> {

                final List<Follow> followerList = Fetch.followersToUserId(new FetchEvent(toProgressBar), null, new ArrayList<>(), user.getId(), Context.twitchClient, Context.credential);
                List<FollowItem> followItems = FollowUtils.mapToFollowItemsTo(followerList);
                updateTo(followItems);

            }).start();
        });
    }

    private void updateFrom(List<FollowItem> followItems){
        Platform.runLater(() -> {
            observableFrom.clear();
            observableFrom.addAll(followItems);
            fromTotalLable.setText(followItems.size()+" from "+user.getDisplayName());
            ready1 = true;
            ready2 = true;
            compare();
        });
    }

    private void updateTo(List<FollowItem> followItems){
        Platform.runLater(() -> {
            observableTo.clear();
            observableTo.addAll(followItems);
            toTotalLable.setText(followItems.size()+" to "+user.getDisplayName());
            ready1 = true;
            ready2 = true;
            compare();
        });
    }

    public static class FetchEvent {
        private final ProgressBar progressBar;
        public FetchEvent(ProgressBar progressBar){
            this.progressBar = progressBar;
        }
        public void onFetch(int size, int total) {
            System.out.println("FetchEvent "+size+" von "+total);   
            Platform.runLater(() -> {
                progressBar.setProgress((double)size/(double)total);       
            }); 
        }
    }

    private void clear(String name){

        chattersFromLabel.setText(" ");
        chattersToLabel.setText(" ");
        fromProgressBar.setProgress(0);
        toProgressBar.setProgress(0);
        observableFrom.clear();
        fromTotalLable.setText("0 from "+name);
        observableTo.clear();
        toTotalLable.setText("0 to "+name);
    }

    private boolean ready1 = false;
    private boolean ready2 = false;
    private User user = null;

    public void setUser(User user){

        this.user = user;
        ready1 = false;
        ready2 = false;

        clear(user.getLogin());

        new Thread(() -> {

            changedFollowFrom(user.getLogin(), user.getId());
            changedFollowTo(user.getLogin(), user.getId());

        }).start();
    }


    // wenn beide listen geladen sind
    private void compare(){

        if(!ready1 || !ready2)
            return;

        // compare user follow lists
        final Set<String> fromTo = fromListView.getItems().stream().map(f -> f.follow.getToLogin()).collect(Collectors.toSet());
        final Set<String> toFrom = toListView.getItems().stream().map(f -> f.follow.getFromLogin()).collect(Collectors.toSet());
        result = new HashSet<>();
        fromTo.forEach(f -> {
            if(toFrom.contains(f)){
                result.add(f);
            }
        });

        //compare user follow list mit chatters
        List<FollowItem> fromToList = observableFrom.stream().collect(Collectors.toList()); 
        List<FollowItem> toFromList = observableTo.stream().collect(Collectors.toList());
        List<GraphUser> chatters = handler.getGraphUsers();
        
        int chattersCountFrom = 0;
        int chattersCountTo = 0;

        for (GraphUser userItem : chatters) {
            final FollowItem fromToItem = FollowUtils.getFollowItemByUserLogin(userItem, fromToList);
            final FollowItem toFromItem = FollowUtils.getFollowItemByUserLogin(userItem, toFromList);
            if(fromToItem != null){
                fromToItem.chatter = true;
                chattersCountFrom++;           
            }
            if(toFromItem != null){
                toFromItem.chatter = true;
                chattersCountTo++;           
            }     
        }

        chattersFromLabel.setText(String.valueOf(chattersCountFrom));
        chattersToLabel.setText(String.valueOf(chattersCountTo));
        fromListView.refresh();
        toListView.refresh();
        //System.out.println("compare follows: result size: "+result.size());
     }


    private void changedFollowFrom(String userName, String userId){
        new Thread(() -> {
            final FollowList followerList = twitchClient().getHelix().getFollowers(credential.getAccessToken(), userId, null, null, 100).execute();
            final List<FollowItem> followItems = FollowUtils.mapToFollowItemsFrom(followerList.getFollows());
            Platform.runLater(() -> {
                observableFrom.addAll(followItems);
                fromTotalLable.setText(followerList.getTotal()+" from "+userName);
                handler.onFollowFromSize(followerList.getTotal());
                ready1 = true;
                compare();
            });
        }).start();
    }


    private void changedFollowTo(String userName, String userId) {
        new Thread(() -> {
            final FollowList followerList = twitchClient().getHelix().getFollowers(credential.getAccessToken(), null, userId, null, 100).execute();
            final List<FollowItem> followItems = FollowUtils.mapToFollowItemsTo(followerList.getFollows());
            Platform.runLater(() -> {
                observableTo.addAll(followItems);
                toTotalLable.setText(followerList.getTotal()+" to "+userName);
                handler.onFollowToSize(followerList.getTotal());
                ready2 = true;
                compare();
            });
        }).start();
    }



    private static class UserFollowFromListCell extends ListCell<FollowItem> {

        private final Label chatterLabel = new Label();
        private final Label userNameLabel = new Label();
        private final Label agoLabel = new Label();
        private final Region spacer = new Region();
        private final Region spacer2 = new Region();
        private final HBox layout = new HBox(chatterLabel, spacer2, userNameLabel, spacer, agoLabel);

        public UserFollowFromListCell(){
            chatterLabel.setStyle(Context.cssGreenColor);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer2.setPrefWidth(10);
        }

        @Override
        protected void updateItem(FollowItem item, boolean empty) {
            super.updateItem(item, empty);
            if(empty) {
                setGraphic(null);
            }else{

                if(item.chatter){
                    chatterLabel.setText("C");
                }else{
                    chatterLabel.setText(" ");
                }

                userNameLabel.setText(item.follow.getToName());
                if(result.contains(item.follow.getToLogin())){
                    userNameLabel.setStyle(Context.cssYellowColor);
                }else{
                    userNameLabel.setStyle(Context.cssWhiteColor);
                }

                Duration duration = Duration.between(item.follow.getFollowedAtInstant(), Instant.now());
                agoLabel.setText(String.valueOf(duration.toHours()));

                setGraphic(layout);

            }
            setText(null);

        }
    }

    private static class UserFollowToListCell extends ListCell<FollowItem> {

        private final Label chatterLabel = new Label();
        private final Label userNameLabel = new Label();
        private final Label agoLabel = new Label();
        private final Region spacer = new Region();
        private final Region spacer2 = new Region();
        private final HBox layout = new HBox(chatterLabel, spacer2, userNameLabel, spacer, agoLabel);

        public UserFollowToListCell(){
            chatterLabel.setStyle(Context.cssGreenColor);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer2.setPrefWidth(10);
        }

        @Override
        protected void updateItem(FollowItem item, boolean empty) {
            super.updateItem(item, empty);

            if(empty) {
                setGraphic(null);
            }else{

                if(item.chatter){
                    chatterLabel.setText("C");
                }else{
                    chatterLabel.setText(" ");
                }

                userNameLabel.setText(item.follow.getFromName());
                if(result.contains(item.follow.getFromLogin())){
                    userNameLabel.setStyle(Context.cssYellowColor);
                }else{
                    userNameLabel.setStyle(Context.cssWhiteColor);
                }

                Duration duration = Duration.between(item.follow.getFollowedAtInstant(), Instant.now());
                agoLabel.setText(String.valueOf(duration.toHours()));

                setGraphic(layout);

            }
            setText(null);

        }
    }

}
