package de.calitobundo.twitch.desktop.views;

import com.github.twitch4j.helix.domain.*;
import static de.calitobundo.twitch.desktop.api.Fetch.*;

import de.calitobundo.twitch.desktop.data.StreamInfoData;
import de.calitobundo.twitch.desktop.dto.StreamInfo;
import de.calitobundo.twitch.desktop.event.EventHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.*;
import java.util.stream.Collectors;

import static de.calitobundo.twitch.desktop.api.Context.*;


public class StreamInfoView extends VBox {

    private final EventHandler handler;

    // info
    private final Label streamInfoLabel = new Label("StreamInfos");
    private final Button updateStreamInfoButton = new Button("Update");
    private final Button saveStreamInfoButton = new Button("Save");
    private final ObservableList<StreamInfo> streamInfoObservableList = FXCollections.observableArrayList();
    private final ListView<StreamInfo> streamInfosListView = new ListView<>(streamInfoObservableList);
    private final Button addStreamInfoButton = new Button("Add");
    private final Button removeStreamInfoButton = new Button("Remove");

    // titel
    private final Label titelLabel = new Label("Titel");
    private final TextField titelTextField = new TextField();
    private final TextArea titelTextArea = new TextArea();

    // game
    private final Label gameLabel = new Label("Game");
    private final TextField gameTextField = new TextField();
    private final TextField gameSearchTextField = new TextField();
    private final ObservableList<Game> gameObservableList = FXCollections.observableArrayList();
    private final ListView<Game> gameListView = new ListView<>(gameObservableList);
    private final ImageView gameImageView = new ImageView();

    // tags
    private final Label tagLabel = new Label("Tag");
    private final TextField tagFilterTextField = new TextField();
    private final ObservableList<StreamTag> tagObservableList = FXCollections.observableArrayList();
    private final ListView<StreamTag> tagListView = new ListView<>(tagObservableList);
    private final TextArea tagDescriptionTextArea = new TextArea();

    // selected tags
    private final ObservableList<StreamTag> selectedTagObservableList = FXCollections.observableArrayList();
    private final ListView<StreamTag> selectedTagListView = new ListView<>(selectedTagObservableList);
    private final Button addTagButton = new Button(">");
    private final Button removeTagButton = new Button("<");


    public StreamInfoView(EventHandler handler) {
        this.handler = handler;

        setPadding(new Insets(10));

        gameListView.setCellFactory(listView -> new GameListCell());
        tagListView.setCellFactory(listView -> new StreamTagListCell());
        selectedTagListView.setCellFactory(listView -> new StreamTagListCell());
        streamInfosListView.setCellFactory(listView -> new StreamInfoListCell());

        VBox streamInfoButtonsLayout = new VBox(updateStreamInfoButton, saveStreamInfoButton, addStreamInfoButton, removeStreamInfoButton);
        streamInfoButtonsLayout.setSpacing(10);
        updateStreamInfoButton.setMaxWidth(Double.MAX_VALUE);
        saveStreamInfoButton.setMaxWidth(Double.MAX_VALUE);
        addStreamInfoButton.setMaxWidth(Double.MAX_VALUE);
        removeStreamInfoButton.setMaxWidth(Double.MAX_VALUE);

        HBox streamInfoLayout = new HBox(streamInfosListView, streamInfoButtonsLayout);
        streamInfoLayout.setSpacing(10);
        HBox.setHgrow(streamInfosListView, Priority.ALWAYS);
        streamInfosListView.setPrefHeight(100);

        //info
        GridPane gridPane = new GridPane();
        gridPane.add(streamInfoLabel, 0, 0);
        gridPane.add(streamInfoLayout, 0, 1);
        gridPane.add(titelLabel, 0, 2);
        gridPane.add(titelTextArea, 0, 3);
        gridPane.add(gameLabel, 0, 4);

        //game
        StackPane imageLayout = new StackPane(gameImageView);
        imageLayout.setPrefWidth(150);

        VBox gameLeftLayout = new VBox(gameTextField, imageLayout);
        gameLeftLayout.setSpacing(10);

        VBox gameRightLayout = new VBox(gameSearchTextField, gameListView);
        gameRightLayout.setSpacing(10);
        VBox.setVgrow(gameListView, Priority.ALWAYS);

        HBox gameLayout = new HBox(gameLeftLayout, gameRightLayout);
        gameLayout.setSpacing(10);
        HBox.setHgrow(gameRightLayout, Priority.ALWAYS);

        gridPane.add(gameLayout, 0, 5);
        VBox.setVgrow(gameRightLayout, Priority.ALWAYS);

        //tag
        gridPane.add(tagLabel, 0, 6);
        gridPane.add(tagFilterTextField, 0, 7);

        VBox tagButtonLayout = new VBox(addTagButton, removeTagButton);
        tagButtonLayout.setSpacing(10);

        HBox tagLayout = new HBox(tagListView, tagButtonLayout, selectedTagListView);
        tagLayout.setSpacing(10);
        tagLayout.setPrefHeight(160);

        gridPane.add(tagLayout, 0, 8);

        gridPane.add(tagDescriptionTextArea, 0, 9);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        getChildren().add(gridPane);

        titelTextArea.setPrefHeight(60);
        titelTextArea.setWrapText(true);
        gameListView.setPrefHeight(125);


        final FilteredList<StreamTag> filteredList = new FilteredList<>(tagObservableList, tag -> !tag.getIsAuto());
        tagListView.setPrefHeight(125);
        tagListView.setItems(filteredList);

        // select tag from list
        tagListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<StreamTag>() {
            @Override
            public void changed(ObservableValue<? extends StreamTag> observable, StreamTag oldValue, StreamTag newValue) {
                if(newValue != null) {
                    final boolean isAutoTag =  newValue.getIsAuto();
                    tagDescriptionTextArea.setText(newValue.getLocalizationDescriptions().get("de-de")+" "+isAutoTag);
                }
            }
        });


        // filter tag list
        tagFilterTextField.setOnKeyPressed(e -> {
            final String query = tagFilterTextField.getText().toLowerCase();
            filteredList.setPredicate(tag -> tag.getLocalizationNames().get("de-de") != null && !tag.getIsAuto() && tag.getLocalizationNames().get("de-de").toLowerCase().contains(query));

        });

        // search game
        gameSearchTextField.setOnKeyPressed(e -> {
            String query = gameSearchTextField.getText();
            if(query == null || query.length() < 1)
                query = " ";
            searchCategory(query);
        });

        tagDescriptionTextArea.setPrefHeight(70);
        tagDescriptionTextArea.setWrapText(true);

        // select game from list
        gameListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Game>() {
            @Override
            public void changed(ObservableValue<? extends Game> observable, Game oldValue, Game newValue) {
                if(newValue == null)
                    return;
                gameImageView.setImage(new Image(newValue.getBoxArtUrl(), 2*57, 2*76, true, false));
                gameTextField.setText(newValue.getName());
            }
        });

        // add tag to list
        addTagButton.setOnAction(e -> {
            StreamTag selectedTag = tagListView.getSelectionModel().getSelectedItem();
            if(selectedTag == null)
                return;
            selectedTagListView.getItems().add(selectedTag);
        });

        // remove tag from list
        removeTagButton.setOnAction(e -> {
            StreamTag selectedTag = selectedTagListView.getSelectionModel().getSelectedItem();
            if(selectedTag == null)
                return;
            selectedTagListView.getItems().remove(selectedTag);
        });

        // update StreamInfo on twitch
        updateStreamInfoButton.setOnAction(e -> {
            updateStreamInfoButton.setDisable(true);
            new Thread(() -> {
                final User user = fetchUserByName("einfachuwe42");
                if(user == null)
                    return;
                final StreamInfo streamInfo = streamInfosListView.getSelectionModel().getSelectedItem();
                if(streamInfo == null)
                    return;
                final ChannelInformation channelInformation = new ChannelInformation()
                        .withTitle(streamInfo.getTitle())
                        .withBroadcasterLanguage(streamInfo.getBroadcasterLanguage())
                        .withGameId(streamInfo.getGameId());
                final List<UUID> streamTagIds = Collections.emptyList();
                //final List<UUID> streamTagIds = streamInfo.getTags().stream().map(StreamTag::getTagId).collect(Collectors.toList());
                System.out.println(streamTagIds);
                twitchClient.getHelix().updateChannelInformation(credential.getAccessToken(), user.getId(), channelInformation).execute();
                twitchClient.getHelix().replaceStreamTags(credential.getAccessToken(), user.getId(), streamTagIds).execute();
                Platform.runLater(() -> {
                    updateStreamInfoButton.setDisable(false);
                });
            }).start();
        });

        // save StreamInfos to json file
        saveStreamInfoButton.setOnAction(e -> {
            if(streamInfoData == null)
                return;
            final List<StreamInfo> list = streamInfosListView.getItems();
            StreamInfoData data = new StreamInfoData(list);
               // saveStreamInfo(data);
            loadStreamInfoFromFile();
        });


        // load StreamInfos from json file
        loadStreamInfoFromFile();


        // add new StreamInfo to list
        addStreamInfoButton.setOnAction(e -> {
            final Game game = gameListView.getSelectionModel().getSelectedItem();
            if(game == null)
                return;
            final ChannelInformation channelInformation = new ChannelInformation()
                    .withTitle(titelTextArea.getText())
                    .withGameId(game.getId())
                    .withGameName(game.getName())
                    .withBroadcasterLanguage("de");

            final List<StreamTag> selectedTags = selectedTagListView.getItems();
            StreamInfo streamInfo = new StreamInfo(channelInformation, selectedTags);

            //Game game = gameListView.getSelectionModel().getSelectedItem();
            streamInfo.setBoxArtUrl(game.getBoxArtUrl());
            streamInfosListView.getItems().add(streamInfo);

            if(streamInfoData == null)
                return;
            final List<StreamInfo> list = streamInfosListView.getItems();
            StreamInfoData data = new StreamInfoData(list);
            //saveStreamInfo(data);

            loadStreamInfoFromFile();
        });

        // remove streamInfo from list
        removeStreamInfoButton.setOnAction(e -> {
            streamInfosListView.getItems().remove(streamInfosListView.getSelectionModel().getSelectedItem());
        });

        // select StreamInfo from list
        streamInfosListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<StreamInfo>() {
            @Override
            public void changed(ObservableValue<? extends StreamInfo> observable, StreamInfo oldValue, StreamInfo newValue) {

                if(newValue == null)
                    return;
                selectedTagObservableList.setAll(newValue.getTags());
                gameTextField.setText(newValue.getGameName());
                gameSearchTextField.clear();
                //searchCategory(" ");
                //searchCategory(newValue.getGameName());
                titelTextArea.setText(newValue.getTitle());
                gameImageView.setImage(new Image(newValue.getBoxArtUrl(), 2*57, 2*76, true, false));

            }
        });
    }

    /**
     *
     *
     *
     */
    private void loadStreamInfoFromFile(){
        streamInfoData = null; //loadStreamInfo();
        if(streamInfoData != null) {
            streamInfosListView.getItems().clear();
            streamInfosListView.getItems().addAll(streamInfoData.getStreamInfo());
        }
    }
    private void searchCategory(String query){
        //String query = gameTextField.getText();

        new Thread(() -> {
            final CategorySearchList list = twitchClient.getHelix().searchCategories(credential.getAccessToken(), query, 100, null).execute();
            if(list.getResults() != null && !list.getResults().isEmpty()){
                Platform.runLater(() -> {

                    final List<Game> sortedList = list.getResults().stream()
                            .filter(g -> g.getName().toLowerCase().contains(query))
                            .sorted(Comparator.comparing(Game::getName))
                            .collect(Collectors.toList());

                    gameObservableList.setAll(sortedList);
                });
            }
        }).start();
    }

    private StreamInfoData streamInfoData = null;

    public void init(){
        new Thread(() -> {

            final List<StreamTag> allStreamTags = fetchAllStreamTags(null, new ArrayList<>());
                Platform.runLater(() -> {
                    tagObservableList.addAll(allStreamTags);
                });

            final User broadcaster = fetchUserByName("einfachuwe42");
            if(broadcaster == null)
                return;
            final ChannelInformation channelInformation = fetchChannelInformationByUserId(broadcaster.getId());
            if(channelInformation == null)
                return;
            Platform.runLater(() -> {
                titelTextArea.setText(channelInformation.getTitle());
                gameSearchTextField.setText(channelInformation.getGameName());
                //channelInformation.getBroadcasterLanguage();
            });

        }).start();
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

    private static class StreamTagListCell extends ListCell<StreamTag> {

        @Override
        protected void updateItem(StreamTag streamTag, boolean empty) {
            super.updateItem(streamTag, empty);
            if(empty){
                setText(null);
            }else{
                setText(streamTag.getLocalizationNames().get("de-de"));
            }
            setGraphic(null);
        }
    }

    private static class StreamInfoListCell extends ListCell<StreamInfo> {

        @Override
        protected void updateItem(StreamInfo streamInfo, boolean empty) {
            super.updateItem(streamInfo, empty);
            if(empty){
                setText(null);
            }else{
                setText(streamInfo.getTitle());
            }
            setGraphic(null);
        }
    }
}
