package de.calitobundo.twitch.desktop.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.twitch4j.helix.domain.CategorySearchList;
import com.github.twitch4j.helix.domain.Game;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.User;

import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.api.Fetch;
import de.calitobundo.twitch.desktop.data.PersistData;
import de.calitobundo.twitch.desktop.data.GameItemData;
import de.calitobundo.twitch.desktop.event.EventHandler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LiveStreamsView extends VBox {
    
    //search game
    private final TextField gameTextField = new TextField();
    private final TextField gameSearchTextField = new TextField();
    private final ObservableList<Game> gameObservableList = FXCollections.observableArrayList();
    private final ListView<Game> gameListView = new ListView<>(gameObservableList);

    private final ObservableList<GameItem> gameObservableList2 = FXCollections.observableArrayList();
    private final ListView<GameItem> gameListView2 = new ListView<>(gameObservableList2);

    private final ObservableList<LiveStreamItem> streams = FXCollections.observableArrayList();
    private final ListView<LiveStreamItem> listView = new ListView<LiveStreamItem>(streams);

    private final Button loadButton = new Button("Load");
    private final Button clearButton = new Button("Clear");
    private final Button addButton = new Button("Add");
    private final Button removeButton = new Button("Remove");

    private final Button liveFollowsButton = new Button("Live Follows");
    private final TextField userNameTextField = new TextField();

    private final Button sortNameButton = new Button("Name");
    private final Button sortUptimeButton = new Button("Uptime");
    private final Button sortViewersButton = new Button("Viewers");
    private final Label resultSizeLabel = new Label("0");
    private final Label sortOrderLabel = new Label();

    private final EventHandler handler;

    public LiveStreamsView(EventHandler handler) {
        this.handler = handler;
        System.out.println("LiveStreamsView: ");

        listView.setCellFactory(listView -> new LiveStreamCell());
        VBox.setVgrow(listView, Priority.ALWAYS);
        gameListView.setCellFactory(listView -> new GameListCell());
        gameListView2.setCellFactory(listView -> new GameItemListCell());

        HBox listLayout = new HBox(gameListView, gameListView2);
        listLayout.setSpacing(10);
        listLayout.setPrefHeight(100);

        HBox inputsLayout = new HBox(loadButton, clearButton, addButton, removeButton);
        inputsLayout.setSpacing(10);

        HBox liveInputsLayout = new HBox(userNameTextField, liveFollowsButton);
        liveInputsLayout.setSpacing(10);

        HBox liveInputsLayout2 = new HBox(sortNameButton, sortUptimeButton, sortViewersButton, sortOrderLabel, resultSizeLabel);
        liveInputsLayout2.setSpacing(10);


        VBox box = new VBox(gameSearchTextField, listLayout, gameTextField);
        box.setSpacing(10);

        getChildren().addAll(box, inputsLayout, liveInputsLayout, liveInputsLayout2, listView);
        setSpacing(10);
        setPadding(new Insets(10, 0, 0, 0));

        resultSizeLabel.textProperty().bind(Bindings.size(streams).asString());
        sortOrderLabel.textProperty().bind(sortOrderObservable.asString());

        gameSearchTextField.setOnKeyPressed(e -> {
            String query = gameSearchTextField.getText();
            if(query == null || query.length() < 1){
                query = " ";
                gameObservableList.clear();
            }else{
                searchCategory(query);
            }
        });

        loadButton.setOnAction(e -> {         
            streams.clear();
            
            new Thread(() -> {
                final List<String> gameIds = Collections.singletonList(gameTextField.getText());
                final List<String> language = Collections.singletonList("de");

                StreamList list = Context.twitchClient.getHelix().getStreams(Context.credential.getAccessToken(), null, null, 100, gameIds, language, null, null).execute();
                List<LiveStreamItem> streamItems = list.getStreams().stream().map(s -> new LiveStreamItem(s)).collect(Collectors.toList());
                Platform.runLater(() -> {
                    streams.addAll(streamItems);
                });
            }).start();
        });

        gameListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Game>() {
            @Override
            public void changed(ObservableValue<? extends Game> observable, Game oldValue, Game newValue) {
                if(newValue == null)
                    return;
                gameTextField.setText(newValue.getId());
            }
        });

        gameListView2.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<GameItem>() {
            @Override
            public void changed(ObservableValue<? extends GameItem> observable, GameItem oldValue, GameItem newValue) {
                if(newValue == null)
                    return;
                gameTextField.setText(newValue.id);
            }
        });

        liveFollowsButton.setOnAction(e -> {
            streams.clear();
            final String username = userNameTextField.getText().toLowerCase();
            if(username.isBlank())
                return;

            new Thread(() -> {

                final User user = Fetch.fetchUserByName(username);
                if(user == null)
                    return;
                final List<Stream> streamResult = Fetch.fetchAllStreamsFromFollowByUserName(null, null, new ArrayList<>(), user);
                final List<LiveStreamItem> streamItems = streamResult.stream().map(s -> new LiveStreamItem(s)).collect(Collectors.toList());

                Platform.runLater(() -> {
                    streams.addAll(streamItems);
                });

            }).start();
        });


        clearButton.setOnAction(e -> {
            streams.clear();
        });

        addButton.setOnAction(e -> {
            Game game = gameListView.getSelectionModel().getSelectedItem();
            if(game == null){
                return;
            }
            gameObservableList2.add(new GameItem(game));
            PersistData.saveJsonObject(new GameItemData(gameObservableList2), GameItemData.class);
        });

        removeButton.setOnAction(e -> {
            GameItem gameItem = gameListView2.getSelectionModel().getSelectedItem();
            if(gameItem == null){
                return;
            }
            gameObservableList2.remove(gameItem);
            PersistData.saveJsonObject(new GameItemData(gameObservableList2), GameItemData.class);
        });

        final List<GameItem> gameItems = PersistData.loadJsonObject(GameItemData.class).getGameItems();
        gameObservableList2.addAll(gameItems);

        sortUptimeButton.setOnAction(e -> {
            final Comparator<LiveStreamItem> uptimeComparator = Comparator.comparingLong(LiveStreamItem::uptime);
            if(sortOrderObservable.get() == SortOrder.UPTIME_REV){
                streams.sort(uptimeComparator);
                sortOrderObservable.setValue(SortOrder.UPTIME);
            }else if(sortOrderObservable.get() == SortOrder.UPTIME){
                streams.sort(uptimeComparator.reversed());
                sortOrderObservable.setValue(SortOrder.UPTIME_REV);
            }else{
                streams.sort(uptimeComparator);
                sortOrderObservable.setValue(SortOrder.UPTIME);
            }
        });

        sortViewersButton.setOnAction(e -> {
            final Comparator<LiveStreamItem> viewersComparator = Comparator.comparingLong(LiveStreamItem::viewers);
            if(sortOrderObservable.get() == SortOrder.VIEWERS_REV){
                streams.sort(viewersComparator);
                sortOrderObservable.set(SortOrder.VIEWERS);
            }else if(sortOrderObservable.get() == SortOrder.VIEWERS){
                streams.sort(viewersComparator.reversed());
                sortOrderObservable.set(SortOrder.VIEWERS_REV);
            }else{
                streams.sort(viewersComparator);
                sortOrderObservable.set(SortOrder.VIEWERS);
            } 
        });


        sortNameButton.setOnAction(e -> {
            final Comparator<LiveStreamItem> nameComparator = Comparator.comparing(LiveStreamItem::name);
            if(sortOrderObservable.get() == SortOrder.NAME_REV){
                streams.sort(nameComparator);
                sortOrderObservable.setValue(SortOrder.NAME);
            }else if(sortOrderObservable.get() == SortOrder.NAME){
                streams.sort(nameComparator.reversed());
                sortOrderObservable.setValue(SortOrder.NAME_REV);
            }else{
                streams.sort(nameComparator);
                sortOrderObservable.setValue(SortOrder.NAME);
            } 
        });


    }

    private ObjectProperty<SortOrder> sortOrderObservable = new SimpleObjectProperty<SortOrder>(SortOrder.VIEWERS);
    public enum SortOrder {
        UPTIME, UPTIME_REV, VIEWERS, VIEWERS_REV, NAME, NAME_REV
    }

    private void searchCategory(String query){

        new Thread(() -> {
            final CategorySearchList list = Context.twitchClient.getHelix().searchCategories(Context.credential.getAccessToken(), query, 100, null).execute();
            if(list.getResults() != null && !list.getResults().isEmpty()){
                Platform.runLater(() -> {
                    gameObservableList.setAll(list.getResults());
                });
            }
        }).start();
    }

    public void setUser(User user){
        userNameTextField.setText(user.getLogin());
    }


    public static class LiveStreamItem {

        private final Stream stream;
        private final String name;
        private final long uptime;
        private final int viewers;
        public LiveStreamItem(Stream stream){
            this.stream = stream;
            this.name = stream.getUserLogin();
            this.uptime = stream.getUptime().toMillis();
            this.viewers = stream.getViewerCount();
        }
        public long uptime(){
            return uptime;
        }
        public int viewers(){
            return viewers;
        }
        public String name(){
            return name;
        }
    }

    public static class LiveStreamCell extends ListCell<LiveStreamItem>{

        private final Label languageLabel = new Label();
        private final Label nameLabel = new Label();
        private final Label viewerCountLabel = new Label();
        private final Label titleLabel = new Label();
        private final Label gameLabel = new Label();
        private final Label uptimeLabel = new Label();

        private final HBox box1 = new HBox(languageLabel, nameLabel, viewerCountLabel);
        private final HBox box2 = new HBox(titleLabel);
        private final HBox box3 = new HBox(gameLabel, uptimeLabel);
        private final VBox box = new VBox(box1, box2, box3);

        public LiveStreamCell(){

            nameLabel.setStyle(Context.cssRedColor);
            viewerCountLabel.setStyle(Context.cssGreenColor);
            titleLabel.setStyle(Context.cssYellowColor);
            gameLabel.setStyle(Context.cssBlueColor);

            box1.setSpacing(10);
            box2.setSpacing(10);
            box3.setSpacing(10);

        }

        @Override
        protected void updateItem(LiveStreamItem item, boolean empty) {
            super.updateItem(item, empty);
           
            if(empty){
                setGraphic(null);
            }else{

                languageLabel.setText(item.stream.getLanguage());
                nameLabel.setText(item.stream.getUserName());
                viewerCountLabel.setText(String.valueOf(item.stream.getViewerCount()));
                titleLabel.setText(item.stream.getTitle().replaceAll("[^\\x00-\\x7F]", ""));
                gameLabel.setText(item.stream.getGameName());
                uptimeLabel.setText(String.valueOf(item.stream.getUptime().toMinutes()));

                setGraphic(box);

            }
            setText(null);

        }
    }


    private static class GameListCell extends ListCell<Game> {

        @Override
        protected void updateItem(Game game, boolean empty) {
            super.updateItem(game, empty);
            if(empty){
                setText(null);
            }else{
                setText(game.getName());
            }
            setGraphic(null);
        }
    }

    private static class GameItemListCell extends ListCell<GameItem> {

        @Override
        protected void updateItem(GameItem game, boolean empty) {
            super.updateItem(game, empty);
            if(empty){
                setText(null);
            }else{
                setText(game.name);
            }
            setGraphic(null);
        }
    }

    public static class GameItem {

        public String name;
        public String id;

        public GameItem(){

        }
        public GameItem(Game game){
            this.name = game.getName();
            this.id = game.getId();
        }
    }


}
