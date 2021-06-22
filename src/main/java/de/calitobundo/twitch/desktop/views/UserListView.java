package de.calitobundo.twitch.desktop.views;

import static de.calitobundo.twitch.desktop.api.Fetch.fetchStreamInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.helix.domain.BannedUser;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.User;

import de.calitobundo.twitch.desktop.api.Fetch;
import de.calitobundo.twitch.desktop.api.UweColors;
import de.calitobundo.twitch.desktop.data.PersistData;
import de.calitobundo.twitch.desktop.event.EventHandler;
import de.calitobundo.twitch.desktop.graph.GraphCanvas;
import de.calitobundo.twitch.desktop.graph.GraphHandler;
import de.calitobundo.twitch.desktop.graph.GraphUser;
import de.calitobundo.twitch.desktop.graph.GraphUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class UserListView extends GridPane implements GraphHandler {

    private final Button streamInfoButton = new Button("Info");
    private final Button bannedInfoButton = new Button("Banned");

    private final Label ignorlistSizeLabel = new Label("0");
    private final TextField filterTextField = new TextField();

    private final Circle circleYellow = new Circle(8);
    private final Circle circleBlue = new Circle(8);
    private final Circle circleRed = new Circle(8);
    private final Circle circleDarkRed = new Circle(8);
    private final Button filterAllButton = new Button("All");
    private final Button filterJoinedButton = new Button();
    private final Button filterLeavedButton = new Button();
    private final Button filterRemovedButton = new Button();
    private final Button filterIgnoredButton = new Button();

    private static final ListView<GraphUser> userListView = new ListView<>();
    private static final ObservableList<GraphUser> observableChannelUsers = FXCollections.observableArrayList();
    private final FilteredList<GraphUser> filteredList = new FilteredList<>(observableChannelUsers, item -> true);

    private final GraphCanvas graphCanvas;

    private boolean hide = true;
    private VBox testBox;
    private String style = "-fx-selection-bar:  transparent; -fx-selection-bar-non-focused: transparent;";


    private final EventHandler handler;

    public UserListView(EventHandler handler) {
        this.handler = handler;

        userListView.setFocusTraversable(false);
        userListView.setStyle(style);
        graphCanvas = new GraphCanvas(this);

        filterTextField.textProperty().addListener(obs -> {
            final String filter = filterTextField.getText();
            if(filter == null || filter.length() == 0) {
                if(hide)
                    filteredList.setPredicate(item -> !item.ignored);
                else
                    filteredList.setPredicate(item -> true);
            }
            else {
                if(hide)
                    filteredList.setPredicate(item -> item.login.contains(filter) && !item.ignored);
                else
                    filteredList.setPredicate(item -> item.login.contains(filter));
            }
        });

        userListView.setPrefWidth(200);
        userListView.setMinWidth(320);
        userListView.setCellFactory(listview -> new UserListCell(handler));
        userListView.setItems(filteredList);

        HBox buttonLayout = new HBox(streamInfoButton, bannedInfoButton);
        buttonLayout.setSpacing(10);

        circleYellow.setFill(UweColors.colorYellow);
        filterJoinedButton.setGraphic(circleYellow);

        circleBlue.setFill(UweColors.colorBlue);
        filterLeavedButton.setGraphic(circleBlue);

        circleRed.setFill(UweColors.colorRed);
        filterRemovedButton.setGraphic(circleRed);

        circleDarkRed.setFill(UweColors.colorDarkRed);
        filterIgnoredButton.setGraphic(circleDarkRed);

        HBox buttonLayout2 = new HBox(filterAllButton, filterJoinedButton, filterLeavedButton, filterRemovedButton, filterIgnoredButton);
        buttonLayout2.setSpacing(10);

        testBox = new VBox(graphCanvas);
        VBox.setVgrow(graphCanvas, Priority.ALWAYS);
        
        add(buttonLayout, 0, 0);
        add(buttonLayout2, 0, 1);
        add(filterTextField, 0, 2);
        add(ignorlistSizeLabel, 0, 3);
        add(userListView, 0, 4);
        add(testBox,0 ,5);
        setVgap(10);
        setVgrow(userListView, Priority.ALWAYS);
        setHgrow(filterTextField, Priority.ALWAYS);

        filteredList.addListener(new ListChangeListener<GraphUser>() {
            @Override
            public void onChanged(Change<? extends GraphUser> c) {
                ignorlistSizeLabel.setText("Chatters: "+c.getList().size());
            }
        });

        userListView.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2){
                GraphUser item = userListView.getSelectionModel().getSelectedItem();
                if(item == null)
                    return;
                handler.findUser(item.login);
            }
        });

        filterAllButton.setOnAction(e -> {
            filteredList.setPredicate(user -> !user.removed && !user.ignored);
        });

        filterJoinedButton.setOnAction(e -> {
            filteredList.setPredicate(user -> user.joined && !user.removed && !user.ignored);
        });

        filterLeavedButton.setOnAction(e -> {
            filteredList.setPredicate(user -> !user.joined && !user.removed && !user.ignored);
        });

        filterRemovedButton.setOnAction(e -> {
            filteredList.setPredicate(user -> user.removed && !user.ignored);
        });

        filterIgnoredButton.setOnAction(e -> {
            filteredList.setPredicate(user -> user.ignored);
        });

        streamInfoButton.setOnAction(e -> {
            new Thread(() -> {
                final List<Stream> streams = new ArrayList<>();
                final List<String> channelNames = observableChannelUsers.stream().map(s -> s.login).collect(Collectors.toList()); 
                for (List<String> subList : GraphUtils.getSubLists(100, channelNames)) {
                    streams.addAll(fetchStreamInfo(subList));
                }
                Platform.runLater(() -> {
                    getGraphUsers().stream().forEach(graphUser -> {
                        graphUser.setStream(null);
                        streams.forEach(stream -> {
                            if(stream.getUserName().equalsIgnoreCase(graphUser.login)) {
                                graphUser.setStream(stream);
                            }
                        });
                        graphUser.update();
                    });
                });
            }).start();
        });

        bannedInfoButton.setOnAction(e -> {
            new Thread(() -> {
                final List<BannedUser> bannedUsers = new ArrayList<>();
                final List<String> userIds = getGraphUsers().stream().map(graphUser -> graphUser.id).collect(Collectors.toList());
                for (List<String> subList : GraphUtils.getSubLists(100, userIds)) {
                    bannedUsers.addAll(Fetch.getBannedUserByIds(subList));
                }
                Platform.runLater(() -> {
                    getGraphUsers().stream().forEach(graphUser -> {
                        graphUser.setBanned(false);
                        bannedUsers.stream().forEach(bannedUser -> {
                            if(graphUser.id.equals(bannedUser.getUserId())){
                                graphUser.setBanned(true);
                            }
                        });
                        graphUser.update();
                    });
                });
            }).start();
        });
        

    }

    public void join(String name){

        if(fetching){
            fetchLater.add(name);
            return;
        }
         
        Optional<GraphUser> graphUserOption = observableChannelUsers.stream().filter(graphUser -> graphUser.login.equalsIgnoreCase(name)).findFirst();
        if(graphUserOption.isEmpty()){
            new Thread(() -> {
                final User user = Fetch.fetchUserByName(name);
                if(user != null){
                    final GraphUser graphUser = new GraphUser(user, new ArrayList<>());
                    Platform.runLater(() -> {
                        graphUser.joined();
                        observableChannelUsers.add(0, graphUser);
                        graphUser.startAnimation();
                    });
                    new Thread(() -> {
                        graphUser.fetchFollowers();
                        Platform.runLater(() -> {
                            graphUser.update();
                        });
                    }).start();
                }else{
                    System.out.println("fetchUserByName from ChannelJoinEvent failed! name: "+name);
                }
            }).start();
        }else{
            final GraphUser graphUser = graphUserOption.get();
            if(!graphUser.joined){
                graphUser.joined();
                graphUser.update();
                graphUser.startAnimation();
            }else{
                System.out.println("hat schon gejoint! name: "+name);
            }
        }
    }

    public void leave(String name) {
        Optional<GraphUser> graphUserOption = observableChannelUsers.stream().filter(graphUser -> graphUser.login.equalsIgnoreCase(name)).findFirst();
        if(graphUserOption.isPresent()){
            final GraphUser graphUser = graphUserOption.get();
            graphUser.leaved();
            graphUser.update();
            graphUser.startAnimation();
        }else{
            System.out.println("findByName from ChannelLeaveEvent failed! name: "+name);
        }
    }

    public void onChatMessage(ChannelMessageEvent event) {

        final String colorHex = (String)event.getMessageEvent().getRawTags().get("color");
        final String name = event.getUser().getName();

        Optional<GraphUser> graphUserOption = observableChannelUsers.stream().filter(graphUser -> graphUser.login.equalsIgnoreCase(name)).findFirst();
        if(graphUserOption.isPresent()){
            GraphUser graphUser = graphUserOption.get();
            graphUser.setColor(colorHex);
            graphUser.update();
        }else{
            new Thread(() -> {
                final User user = Fetch.fetchUserByName(event.getUser().getName());
                if(user != null){
                    final GraphUser graphUser = new GraphUser(user, new ArrayList<>());
                    Platform.runLater(() -> {
                        graphUser.setColor(colorHex);
                        graphUser.joined();
                        graphUser.startAnimation();
                        observableChannelUsers.add(0, graphUser);
                    });
                    new Thread(() -> {
                        graphUser.fetchFollowers();
                        Platform.runLater(() -> {
                            graphUser.update();
                        });
                    }).start();
                }else{
                    System.out.println("fetchUserByName onChatMessage from ChannelJoinEvent failed! name: "+event.getUser().getName());
                }
            }).start();      
        }

    }

    private final List<String> fetchLater = new ArrayList<>();
    public volatile boolean fetching = true;

    //from channelChanged
    public void setUserItems(List<String> items) {

        observableChannelUsers.clear();
         new Thread(() -> {
            fetching = true;
            for (String name : items) {
                final GraphUser graphUser = GraphUtils.fetchGraphUser(name);
                if(graphUser != null){
                    Platform.runLater(() -> {
                        graphUser.joined();
                        graphUser.startAnimation();
                        observableChannelUsers.add(0, graphUser);
                    });  
                }else{
                    System.out.println("fetchGraphUser from Chatters failed! name: "+name);
                }
            }
            fetching = false;
            System.out.println("fetchLater size ("+fetchLater.size()+") "+fetchLater);
            for (String name : fetchLater) {
                join(name);
            }
            fetchLater.clear();
        }).start();
    }

    public void ignoreUser(String name, boolean ignore) {
        observableChannelUsers.forEach(graphUser -> {
            if(graphUser.login.equals(name)){
                graphUser.setIgnored(ignore);
                graphUser.update();            }
        });
    }

    public void removeUser(String name, boolean remove) {
        observableChannelUsers.forEach(graphUser -> {
            if(graphUser.login.equals(name)){
                graphUser.setRemoved(remove);
                graphUser.update();
            }
        });
    }


    public static class UserListCell extends ListCell<GraphUser> {

        private final ContextMenu contextMenu = new ContextMenu();
        private final MenuItem ignoreMenuItem = new MenuItem();

        public UserListCell(EventHandler handler) {

            contextMenu.getItems().add(ignoreMenuItem);
            emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                setContextMenu(isNowEmpty ? null : contextMenu);
            });

            ignoreMenuItem.setOnAction(event -> {
                final GraphUser graphUser = getItem();
                if(graphUser == null){
                    return;
                }
                if(graphUser.ignored){
                    PersistData.removeFromIgnoredUserAndSave(graphUser.login);
                    handler.ignoreUser(graphUser.login, false);
                }else{
                    PersistData.addToIgnoredUserAndSave(graphUser.login);
                    handler.ignoreUser(graphUser.login, true);
                }
            });
       }
       
        public void update(GraphUser item){
            updateItem(item, false);
        }

        @Override
        protected void updateItem(GraphUser item, boolean empty) {
            super.updateItem(item, empty);
            if(empty) {
                setText(null);
                setGraphic(null);
            }else{
                ignoreMenuItem.setText("ignore "+item.login);
                //item.listCell = this;
                setText(null);
                setGraphic(item.layout.update());
            }
        }
    }


    /**
     * 
     * 
     * GraphHandler implementation
     *
     */

    @Override
    public List<GraphUser> getGraphUsers() {
        return observableChannelUsers.stream().collect(Collectors.toList());
    }

    @Override
    public void setRotation(boolean rotation) {
        graphCanvas.rotation = rotation;
        
    }

    @Override
    public double getCenter() {
        return graphCanvas.getCenter();
    }

    @Override
    public void openGraphInStage() {
        testBox.getChildren().remove(graphCanvas);
        boolean isPopout = graphCanvas.setPopout();
        if(!isPopout){
            new GraphCanvasStage(this, graphCanvas, handler.getPrimaryStage());

        }
    }

    @Override
    public void closeGraphInStage() {
        graphCanvas.setPopin();
        testBox.getChildren().add(graphCanvas);
    }

    private static class GraphCanvasStage extends Stage {

        public GraphCanvasStage(GraphHandler handler, GraphCanvas graphCanvas, Stage owner){

            final StackPane pane = new StackPane(graphCanvas);
            pane.setPadding(new Insets(10));

            final Scene scene = new Scene(pane);
            scene.getStylesheets().add(UweColors.darkStyle);
            setScene(scene);
            setTitle("GraphCanvasStage");
            initOwner(owner);
            initModality(Modality.NONE);
            sizeToScene();
            centerOnScreen();
            show();

            setOnCloseRequest(e -> {
                handler.closeGraphInStage();
            });
        }
    }


    @Override
    public void onUserRemoved(GraphUser graphUser) {
        userListView.refresh();
    }

    public static void calc(){
        GraphUtils.calculateFollowLines(observableChannelUsers.stream().collect(Collectors.toList()));
       // GraphUtils.calculateFollowLines(observableChannelUsers.stream().filter(g -> !g.removed).collect(Collectors.toList()));
    }


}
