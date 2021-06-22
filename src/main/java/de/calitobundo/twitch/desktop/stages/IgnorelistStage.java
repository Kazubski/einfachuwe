package de.calitobundo.twitch.desktop.stages;

import de.calitobundo.twitch.desktop.api.UweColors;
import de.calitobundo.twitch.desktop.data.PersistData;
import de.calitobundo.twitch.desktop.event.EventHandler;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class IgnorelistStage extends Stage {

    private final TextField ignoredFilterTextField = new TextField();
    private final ListView<String> ignoredListView = new ListView<>();

    private final TextField removedFilterTextField = new TextField();
    private final ListView<String> removedListView = new ListView<>();


    public IgnorelistStage(Stage owner, EventHandler handler) {

        setTitle("PersistData");
        initOwner(owner);
        initModality(Modality.NONE);
        

        final VBox ignoredLayout = new VBox(ignoredFilterTextField, ignoredListView);
        ignoredLayout.setSpacing(10);
        VBox.setVgrow(ignoredListView, Priority.ALWAYS);

        final VBox removedLayout = new VBox(removedFilterTextField, removedListView);
        removedLayout.setSpacing(10);
        VBox.setVgrow(removedListView, Priority.ALWAYS);

        final HBox layout = new HBox(ignoredLayout, removedLayout);
        layout.setSpacing(10);
        layout.setPadding(new Insets(10));
        HBox.setHgrow(ignoredLayout, Priority.ALWAYS);
        HBox.setHgrow(removedLayout, Priority.ALWAYS);

        final List<String> ignoredList = PersistData.ignoredUserData.getList();
        ignoredListView.setCellFactory(listView -> new IgnoredListCell(handler));
        ignoredListView.setItems(FXCollections.observableArrayList(ignoredList));

        final List<String> removedList = PersistData.removedUserData.getList();
        removedListView.setCellFactory(listView -> new RemovedListCell(handler));
        removedListView.setItems(FXCollections.observableArrayList(removedList));

        Scene scene = new Scene(layout, 600, 600);
        scene.getStylesheets().add(UweColors.darkStyle);

        setScene(scene);
        centerOnScreen();
        setMinWidth(500);
        setMinHeight(400);
        setMaxWidth(800);
        setMaxHeight(900);
        show();

    }

    private class IgnoredListCell extends ListCell<String> {

        private final ContextMenu contextMenu = new ContextMenu();
        private final MenuItem ignoreItem = new MenuItem();

        public IgnoredListCell(EventHandler handler) {

            ignoreItem.setOnAction(event -> {
                String login = getListView().getSelectionModel().getSelectedItem();
                PersistData.removeFromIgnoredUserAndSave(login);
                handler.ignoreUser(login, false);
                getListView().refresh();
            });

            contextMenu.getItems().addAll(ignoreItem);
            emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                setContextMenu(isNowEmpty ? null : contextMenu);
            });


            // ignoreItem.setOnAction(e -> {

            // });


        }

        @Override
        protected void updateItem(String name, boolean empty) {
            super.updateItem(name, empty);
            if(empty) {
                setText(null);
            }else{
                if( PersistData.ignored(name)){
                    setStyle(UweColors.cssRedColor);
                    ignoreItem.setText("unignore "+name);
                }else{
                    setStyle(UweColors.cssBlueColor);
                    ignoreItem.setText("ignore "+name);
                }
                setText(name);
            }
            setGraphic(null);
        }
    }

    private class RemovedListCell extends ListCell<String> {

        private final ContextMenu contextMenu = new ContextMenu();
        private final MenuItem ignoreItem = new MenuItem();

        public RemovedListCell(EventHandler handler) {

            ignoreItem.setOnAction(event -> {
                String name = getListView().getSelectionModel().getSelectedItem();
                PersistData.removeFromIgnoredUserAndSave(name);
                handler.removeUser(name, false);
                getListView().refresh();
            });

            contextMenu.getItems().addAll(ignoreItem);
            emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                setContextMenu(isNowEmpty ? null : contextMenu);
            });
        }

        @Override
        protected void updateItem(String name, boolean empty) {
            super.updateItem(name, empty);
            if(empty) {
                setText(null);
            }else{
                if(PersistData.removed(name)){
                    setStyle(UweColors.cssRedColor);
                    ignoreItem.setText("add "+name);
                }else{
                    setStyle(UweColors.cssBlueColor);
                    ignoreItem.setText("remove "+name);
                }
                setText(name);
            }
            setGraphic(null);
        }
    }
}
