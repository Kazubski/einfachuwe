package de.calitobundo.twitch.desktop;

import com.github.twitch4j.chat.events.channel.ChannelJoinEvent;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.helix.domain.User;

import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.api.Fetch;
import de.calitobundo.twitch.desktop.api.UweColors;
import de.calitobundo.twitch.desktop.data.PersistData;
import de.calitobundo.twitch.desktop.data.EmotesData;
import de.calitobundo.twitch.desktop.dto.UserItem;
import de.calitobundo.twitch.desktop.event.EventHandler;
import de.calitobundo.twitch.desktop.event.EventService;
import de.calitobundo.twitch.desktop.graph.GraphUser;
import de.calitobundo.twitch.desktop.stages.ChannelConfigStage;
import de.calitobundo.twitch.desktop.stages.EmoteDiashowStage;
import de.calitobundo.twitch.desktop.stages.FollowCompareStage;
import de.calitobundo.twitch.desktop.stages.IgnorelistStage;
import de.calitobundo.twitch.desktop.stages.MegaEmoteShowStage;
import de.calitobundo.twitch.desktop.views.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.apache.log4j.Logger;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class MainApp extends Application implements EventHandler {

    private static final Logger logger = Logger.getLogger(MainApp.class);

    public static int WIDTH = 1400;
    public static int HEIGHT = 1000;

    private final UserListView userListView = new UserListView(this);
    private final ChannelView channelView = new ChannelView(this);
   
    private final UserInfoView userInfoView = new UserInfoView(this);
    private final UserFollowListView userFollowListView = new UserFollowListView(this);
    private final UserChannelView userChannelView = new UserChannelView(this);
    private final UserClipsInfoView userClipsInfoView = new UserClipsInfoView(this);
    private final LiveStreamsView liveStreamsView = new LiveStreamsView(this);

    private final ChannelInformationView channelInformationView = new ChannelInformationView(this);
    private final ChannelHistoryView channelHistoryView = new ChannelHistoryView();

    //private final StreamInfoView streamInfoView = new StreamInfoView(this);
    //private FollowerGraphStage followerGraphStage = null;
    //public static String darkStyle = Objects.requireNonNull(MainApp.class.getClassLoader().getResource("css/modena_dark.css")).toExternalForm();

    private HBox content;
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        stage.setOnCloseRequest(e -> {
            logger.info("OnCloseRequest");
            Platform.exit();
            System.exit(0);
        });

        final MenuItem menuItem1 = new MenuItem("Ignoreliste bearbeiten");
        final MenuItem menuItem2 = new MenuItem("Follower vergleichen");
        final MenuItem menuItem3 = new MenuItem("Channel Config");
        final Menu menu = new Menu("Extras", null, menuItem1, menuItem2, menuItem3);
        //menu.getItems().addAll(menuItem1, menuItem2, menuItem3);
        menuItem1.setOnAction(e -> {
            new IgnorelistStage(stage, this);
        });
        menuItem2.setOnAction(e -> {
            new FollowCompareStage(stage);
        });
        menuItem3.setOnAction(e -> {
            new ChannelConfigStage(stage);
        });

        final MenuItem userlistViewMenuItem = new MenuItem("Show Userlist");
        final Menu viewMenu = new Menu("View", null, userlistViewMenuItem);
        //viewMenu.getItems().addAll(userlistViewMenuItem);
        userlistViewMenuItem.setOnAction(e -> {
            if(content.getChildren().contains(userListView)){
                content.getChildren().remove(userListView);
                userlistViewMenuItem.setText("Show Userlist"); 
            }else{
                content.getChildren().add(0, userListView); 
                userlistViewMenuItem.setText("Hide Userlist"); 
            }
        });

        final Menu emoteShowMenu = new Menu("EmoteShow");
        final MenuItem emoteShowOnelistViewMenuItem = new MenuItem("open 1 stage");
        final MenuItem emoteShowMaxlistViewMenuItem = new MenuItem("open 18 stages");
        final MenuItem emoteShowlistViewMenuItem = new MenuItem("close all stages");
        final MenuItem megaViewMenuItem = new MenuItem("open mega stages");
        final MenuItem loadEmotesMenuItem = new MenuItem("load emotes");
        emoteShowMenu.getItems().addAll(emoteShowOnelistViewMenuItem, emoteShowMaxlistViewMenuItem, emoteShowlistViewMenuItem, megaViewMenuItem, loadEmotesMenuItem);
        emoteShowOnelistViewMenuItem.setOnAction(e -> {
            EmoteDiashowStage.open(1, 1, stage);
        }); 
        emoteShowMaxlistViewMenuItem.setOnAction(e -> {
            EmoteDiashowStage.open(6, 3, stage);
        });
        emoteShowlistViewMenuItem.setOnAction(e -> {
            EmoteDiashowStage.closeAllStages();
        });
        megaViewMenuItem.setOnAction(e -> {
            MegaEmoteShowStage.open(stage);
        });
        loadEmotesMenuItem.setOnAction(e -> {
            new Thread(() ->  {
                Context.loadEmoticons();
                System.out.println("loadEmoticons size: "+Context.emoticons.size());
                PersistData.saveJsonObject(new EmotesData(Context.emoticons), EmotesData.class);
            }).start();
        });

        // final Menu followerShowMenu = new Menu("Follower");
        // final MenuItem followerMenuItem = new MenuItem("open tool");
        // followerShowMenu.getItems().add(followerMenuItem);
        // followerMenuItem.setOnAction(e -> {
        //     followerGraphStage = new FollowerGraphStage(stage);
        // });

        final MenuBar menuBar = new MenuBar(menu, viewMenu, emoteShowMenu);

        // infoTabPane
        final Tab tab1 = new Tab("User", userInfoView);
        final Tab tab2 = new Tab("Channel", userChannelView);
        final Tab tab3 = new Tab("Follower", userFollowListView);
        final Tab tab4 = new Tab("Clips", userClipsInfoView);
        final Tab tab5 = new Tab("LiveStreams", liveStreamsView);
        final TabPane infoTabPane = new TabPane(tab1, tab2, tab3, tab4, tab5);

        tab1.setClosable(false);
        tab2.setClosable(false);
        tab3.setClosable(false);
        tab4.setClosable(false);
        tab5.setClosable(false);

        // findUser
        final TextField searchTextField = new TextField();
        final Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> {
            if(!searchTextField.getText().isBlank())
                findUser(searchTextField.getText());
        });
        searchTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if(!searchTextField.getText().isBlank())
                    findUser(searchTextField.getText());
            }
        });


        final HBox searchLayout = new HBox(searchTextField, searchButton);
        searchLayout.setSpacing(10);

        final VBox tabPaneLayout = new VBox(searchLayout, infoTabPane);
		tabPaneLayout.setSpacing(10);
        tabPaneLayout.setMinWidth(500);
        tabPaneLayout.setMaxWidth(500);
        tabPaneLayout.setPrefWidth(500);
        VBox.setVgrow(infoTabPane, Priority.ALWAYS);


        final VBox channelLayout = new VBox(channelInformationView, channelView, channelHistoryView);
        channelLayout.setSpacing(10);
        VBox.setVgrow(channelView, Priority.ALWAYS);

        //main hbox
		content = new HBox(userListView, channelLayout, tabPaneLayout);
		content.setSpacing(25);
		content.setPadding(new Insets(10));
        HBox.setHgrow(channelLayout, Priority.ALWAYS);

        final VBox root = new VBox(menuBar, content);
        root.setMinSize(600, 400);
        VBox.setVgrow(content, Priority.ALWAYS);

        final Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.getStylesheets().add(UweColors.darkStyle);
        stage.setScene(scene);
        stage.setTitle("Uwe Twitch Desktop");
        stage.getIcons().add(Context.liveImage);
		stage.setX(0);
		stage.setY(0);
		stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT-100);
        stage.show();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("ShutdownHook");
            channelInformationView.stop();
            //userInfoView.stop();
            Context.close();
        }));

        new Thread(() ->  {
            Context.init(this);
            Context.emoticons.clear();
            //EmotesData data = PersistData.loadJsonObject(EmotesData.class);
            //Context.emoticons.addAll(data.getList());
            
            Platform.runLater(() -> {
                joinChannel(Context.current_channel);
            });
        }).start();

    }



    @Override
    public void stop() throws Exception {
        System.out.println("stop");
        logger.info("stop");
    }


    public static void main(String[] args) {
        launch(args);
    }

    /**
     *
     *      EventHandler Implementation
     *
     */


    @Override
    public void joinChannel(String channelName) {

        new Thread(() -> {

            final User channelUser = Context.setChannelUser(channelName);
            if(channelUser == null){
                System.out.println("joinChannel failed: "+channelName);
                return;
            }
            channelInformationView.start();
            ChatListView.badgesSet = Fetch.getChannelBadges("einfachuwe42");
            final List<UserItem> items = Fetch.getChatters(channelName, PersistData.ignoredUserData.getList());
            Platform.runLater(() -> {
                stage.setTitle("Uwe Twitch Desktop ("+channelName+")");
                userListView.setUserItems(items.stream().map(item -> item.userName).collect(Collectors.toList()));
            });
        }).start();
        channelView.clear();
    }

    @Override
    public void findUser(String userName) {

        if(userName == null)
            return;

        new Thread(() -> {

            final User user = Fetch.fetchUserByName(userName);
            
            if(user == null)
                return;
 
            Platform.runLater(() -> {
                userInfoView.loadUser(user);
                userFollowListView.setUser(user);
                userChannelView.setUser(user);
                userClipsInfoView.loadClips(user);
                liveStreamsView.setUser(user);
                channelHistoryView.setUser(user);
            });

        }).start();
    }

    @Override
    public void addChatMessage(ChannelMessageEvent event) {
        Platform.runLater(() -> {

            userListView.onChatMessage(event);
            channelView.addChatMessage(event);
        });
    }

    @Override
    public void addLogMessage(String message) {
        Platform.runLater(() -> channelView.addLogMessage(message));
    }

    @Override
    public EventService getService() {
        return Context.getService();
    }

    @Override
    public void openWebsite(URI uri) {
        getHostServices().showDocument(uri.toString());
    }

    @Override
    public void addChattersTab(String channelName) {
        channelView.addChattersTab(channelName);
        
    }

    @Override
    public void ignoreUser(String name, boolean ignore) {
        userListView.ignoreUser(name, ignore);
    }

    @Override
    public void removeUser(String name, boolean ignore) {
        userListView.removeUser(name, ignore);
    }

    @Override
    public void onFollowFromSize(int size) {
        userInfoView.onFollowFromSize(size);
    }

    @Override
    public void onFollowToSize(int size) {
        userInfoView.onFollowToSize(size);
    }

    @Override
    public void onClipsSize(int size) {
        userInfoView.onClipsSize(size);
     }

     @Override
     public List<GraphUser> getGraphUsers() {
         return userListView.getGraphUsers();
     }

     @Override
     public void showUserListView(boolean show) {
        
        if(content.getChildren().contains(userListView)){
            content.getChildren().remove(userListView);
        }else{
            content.getChildren().add(0, userListView); 
        }
     }


    @Override
    public void joined(ChannelJoinEvent event) {
        userListView.join(event.getUser().getName());
    }

    @Override
    public void leaved(ChannelLeaveEvent event) {
        userListView.leave(event.getUser().getName());
    }

    @Override
    public Stage getPrimaryStage() {
        return stage;
    }


    
}
