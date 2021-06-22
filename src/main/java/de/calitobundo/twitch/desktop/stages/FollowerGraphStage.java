package de.calitobundo.twitch.desktop.stages;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import com.github.twitch4j.helix.domain.Follow;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.tmi.domain.Chatters;

import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.api.Fetch;
import de.calitobundo.twitch.desktop.api.UweColors;
import de.calitobundo.twitch.desktop.views.UserFollowListView.FetchEvent;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FollowerGraphStage extends Stage implements EventHandler<MouseEvent>{
    
    private static final Comparator<GraphUser> SIZE_COMPARATOR = Comparator.comparingInt(GraphUser::size).reversed();

    private static double WIDTH = 960;
    private static double HEIGHT = 960;
    private final Canvas canvas = new Canvas(WIDTH, HEIGHT);

    private final TextField addTextField = new TextField("montanablack88");
    private final Button addButton = new Button("Add");

    private final TextField channelTextField = new TextField("einfachuwe42");
    private final Button startButton = new Button("Start");
    private final ProgressBar chattersProgressBar = new ProgressBar();
    private final ProgressBar progressBar = new ProgressBar();

    private final Button rotationButton = new Button("Rotation");
    private final Button showAllButton = new Button("All");
    private final Button showListButton = new Button("List");
    private final Button showFollowsButton = new Button("Follows");
    private final Button calcButton = new Button("Calc");
    private final TextField maxTextField = new TextField("0");

    private final ObservableList<GraphUser> graphUserObservableList = FXCollections.observableArrayList();
    private final ListView<GraphUser> graphUserListView = new ListView<>(graphUserObservableList);

    private final ObservableList<GraphUser> graphUserRemovedObservableList = FXCollections.observableArrayList();
    private final ListView<GraphUser> graphUserRemovedListView = new ListView<>(graphUserRemovedObservableList);

    private final ObservableList<GraphUser> graphUserDetailsObservableList = FXCollections.observableArrayList();
    private final ListView<GraphUser> graphUserDetailsListView = new ListView<>(graphUserDetailsObservableList);

    private final Button removeUserButton = new Button("Remove");
    private final Button testButton = new Button("Animation Test");

    private GraphUser selectedUser = null;
    private GraphUser draggedUser = null;

    private boolean rotation = true;
    private boolean showAll = true;
    private boolean showList = false;
    private boolean showFollows = false;

    private int maxLevel = 0;

    private String style = "-fx-selection-bar:  transparent; -fx-selection-bar-non-focused: transparent;";
    private static Color colorYellow = Color.rgb(252, 228, 122);
    private static Color colorBlue = Color.rgb(124, 122, 255);
    //private static Color colorRed = Color.rgb(252, 122, 122);

    public FollowerGraphStage(Stage owner){

        graphUserListView.setCellFactory(view -> new GraphUserCellRenderer());
        graphUserRemovedListView.setCellFactory(view -> new GraphUserCellRenderer());
        graphUserDetailsListView.setCellFactory(view -> new GraphUserDetailsCellRenderer());

        graphUserListView.setStyle(style);
        graphUserRemovedListView.setStyle(style);

        final HBox inputAddLayout = new HBox(addTextField, addButton);
        inputAddLayout.setSpacing(10);

        final HBox inputShowLaout = new HBox(rotationButton, showAllButton, showListButton, showFollowsButton);
        inputShowLaout.setSpacing(10);

        final HBox inputStartLaout = new HBox(channelTextField, startButton);
        inputStartLaout.setSpacing(10);

        final HBox listLayout = new HBox(graphUserListView, graphUserRemovedListView);
        listLayout.setSpacing(10); 
        HBox.setHgrow(graphUserListView, Priority.ALWAYS);      
        HBox.setHgrow(graphUserRemovedListView, Priority.ALWAYS);      

        final HBox inputCalcLayout = new HBox(maxTextField, calcButton);
        inputCalcLayout.setSpacing(10); 

        final HBox inputTestLayout = new HBox(removeUserButton, testButton);
        inputTestLayout.setSpacing(10); 
        
        final VBox rightLayout = new VBox(chattersProgressBar, progressBar, inputAddLayout, inputShowLaout, inputCalcLayout, listLayout, inputTestLayout, graphUserDetailsListView);
        rightLayout.setSpacing(10);
        chattersProgressBar.setMinHeight(20);
        chattersProgressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setMinHeight(20);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        final Pane canvasLayout = new Pane(canvas);
        canvasLayout.minWidthProperty().bind(canvasLayout.heightProperty());
        canvas.widthProperty().bind(canvasLayout.heightProperty());
        canvas.heightProperty().bind(canvasLayout.heightProperty());
        minWidthProperty().bind(canvasLayout.heightProperty().add(650));

        final HBox mainLayout = new HBox(canvasLayout, rightLayout);
        rightLayout.setMinWidth(600);
        HBox.setHgrow(rightLayout, Priority.ALWAYS);
        mainLayout.setSpacing(10);

        final VBox root = new VBox(inputStartLaout, mainLayout);
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        final Scene scene = new Scene(root, WIDTH+340, HEIGHT+160);
        scene.getStylesheets().add(UweColors.darkStyle);
        setScene(scene);
        setTitle("FollowerGraphStage");
        initOwner(owner);
        initModality(Modality.NONE);
        setX(1920);
        setY(0);
        show();

        canvas.setOnMousePressed(this);
        canvas.setOnMouseMoved(this);
        canvas.setOnMouseReleased(this);
        canvas.setOnMouseDragged(this);
        canvas.requestFocus();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            render();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        setOnCloseRequest(e -> {
            timeline.stop();
        });

        startButton.setOnAction(e -> {
            final String channelName = channelTextField.getText().toLowerCase();
            if(!channelName.isBlank()){
                maxLevel = 0;
                startFetch(channelName);
            }
        });

        rotationButton.setOnAction(e -> rotation = !rotation);
        showAllButton.setOnAction(e -> showAll = !showAll);  
        showListButton.setOnAction(e -> showList = !showList);
        showFollowsButton.setOnAction(e -> showFollows = !showFollows);

        calcButton.setOnAction(e -> {
            maxLevel = Integer.valueOf(maxTextField.getText());
            calc();
        });

        addButton.setOnAction(e -> {
            final String name = addTextField.getText().toLowerCase();
            if(name.isBlank())
                return;
            join(name);
        });

        removeUserButton.setOnAction(e -> {
            GraphUser user = graphUserRemovedListView.getSelectionModel().getSelectedItem();
            if(user == null)
                return;
            graphUserRemovedObservableList.remove(user);
            graphUserObservableList.add(user);
        });

        testButton.setOnAction(e -> {

            final List<GraphUser> users = graphUserObservableList.filtered(user -> user.fromUsers.size() == 0 && user.toUsers.size() == 0);
            final double angle = 2*Math.PI/users.size();
            for (int i = 0; i < users.size(); i++) {
                users.get(i).angle = i * angle;
            }
            graphUserObservableList.stream().filter(g -> g.joined).forEach(GraphUser::startAnimation);
        });

        final ChangeListener<GraphUser> listener = (obs, o, newValue) -> {
            if(newValue != null){
                graphUserDetailsObservableList.setAll(newValue.fromUsers.values());
                graphUserDetailsObservableList.sort(SIZE_COMPARATOR);
            }
        };

        graphUserListView.getSelectionModel().selectedItemProperty().addListener(listener);
        graphUserRemovedListView.getSelectionModel().selectedItemProperty().addListener(listener);

        final ChangeListener<GraphUser> listener2 = (obs, oldValue, newValue) -> {
            if(newValue != null){
                newValue.selected = true;
            }
            if(oldValue != null){
                oldValue.selected = false;
            }
        };  

        graphUserListView.getSelectionModel().selectedItemProperty().addListener(listener2);

    }


    public void join(String name){

        if(!channelTextField.getText().equalsIgnoreCase("einfachuwe42"))
            return;

        final GraphUser test = findGraphAllUser(name);
        if(test == null){
            new Thread(() -> {
                final User user = Fetch.fetchUserByName(name);
                if(user != null){
                    final GraphUser graphUser = fetchGraphUser(user);
                    graphUser.joined = true;
                    //graphUser.joinCount++;
                    graphUser.angle = 2 * Math.PI * new Random().nextDouble();
                    System.out.println("join: "+graphUser);
                    Platform.runLater(() -> {
                        graphUserObservableList.add(graphUser);
                        graphUser.startAnimation();
                        DrawUserList.add(graphUser);
                        graphUserListView.refresh();
                        graphUserRemovedListView.refresh();
                   });
                }
               }).start();
        }else{
            test.joined = true;
            test.joinCount++;
            test.startAnimation();
            DrawUserList.add(test);
            graphUserListView.refresh();
            graphUserRemovedListView.refresh();
            System.out.println("join: "+test);
        }
 
    }

    public void leave(String name){
        
        if(!channelTextField.getText().equalsIgnoreCase("einfachuwe42"))
            return;

        final GraphUser graphUser = findGraphAllUser(name);
        if(graphUser != null){
            graphUser.joined = false;
            graphUser.startAnimation();
            DrawUserList.add(graphUser);
            graphUserListView.refresh();
            graphUserRemovedListView.refresh();
            System.out.println("leave: "+graphUser);
        }
    }

    private GraphUser findGraphAllUser(String name){
        for (GraphUser graphUser : graphUserObservableList) {
            if(graphUser.name.equals(name))
                return graphUser;      
        }
        for (GraphUser graphUser : graphUserRemovedObservableList) {
            if(graphUser.name.equals(name))
                return graphUser;      
        }
        return null;
    }

    private final double top = 50;
    private final double left = 50;
    private double size = (WIDTH - 2*left)/2;

    private void render(){


        final List<GraphUser> graphUsers = graphUserListView.getItems();
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        final double canvasWidth = canvas.getWidth();
        final double canvasHeight = canvas.getHeight();

        size = (canvasWidth - 2*left)/2;

        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        int max = 0;
        int maxCount = 0;
        if(maxLevel > 0){
            max = maxLevel;
        }else{
            for (GraphUser user : graphUsers) {
                if(user.fromUsers.size() > max){
                    max = user.fromUsers.size();
                    maxCount = 1;
                }else if(user.fromUsers.size() == max){
                    maxCount++;
                }
            }
            if(maxCount > 1){
                max++;
            }
        }

        // draw circles
        gc.setStroke(Color.rgb(60, 60, 60));
        gc.strokeOval(left, top, 2*size, 2*size);

        for (int i = 1; i <= max; i++) {
            double r = size - (size/(double)max) * (double)i;
            gc.strokeOval(left+size-r, top+size-r, 2*r, 2*r);
        }

        // draw users items
        if(showList){
            gc.setFill(Color.DARKGREY);
            double y = 0;
            for (GraphUser user : graphUsers) {
                y += 15;
                gc.fillText(user.toString(), 20, y);
            }
        }

        // calc user position
        for (GraphUser user : graphUsers) {
            double radius = user.first ? size : size - (size/(double)max) * user.fromUsers.size();
            if(rotation)
                user.angle += Math.PI/512;
            user.x = left + size + radius * Math.cos(user.angle);
            user.y = top + size + radius * Math.sin(user.angle);
        }   

        // draw follow lines
        if(showFollows){
            gc.setStroke(Color.FIREBRICK);
            for (GraphUser user : graphUsers) {
                for (GraphUser user2 : user.fromUsers.values()) { 
                    if(!user.equals(user2))   
                        gc.strokeLine(user.x, user.y, user2.x, user2.y);
                }
            }
            for (GraphUser user : graphUsers) {
                for (GraphUser user2 : user.fromUsers.values()) {
                    double dx2 = (user2.x - user.x)/2;
                    double dy2 = (user2.y - user.y)/2;
                    gc.setStroke(Color.TOMATO);
                    gc.strokeLine(user.x, user.y, user.x+dx2, user.y+dy2);
                }
            }
        }

        // draw user point and name
        for (GraphUser user : graphUsers) {

            final double area = 20 + user.followers.size() / 15.0;
            user.radius = Math.sqrt(area/Math.PI);

            final double radius = user.radius + user.radiusFactor;
            if(showAll || user.size() > 0 || user.toUsers.size() > 0 || user.first){

                gc.setFill(user.joined ? colorYellow : colorBlue);
                gc.fillOval(user.x-radius, user.y-radius, 2*radius, 2*radius);

                gc.setFill(user.selected ? Color.WHEAT : Color.GREEN);
                gc.fillText(user.name, user.x+10, user.y);
            }
        }

        //draw join and leave users
        DrawUserList.render(gc);
    }


    private void calc(){

        final List<GraphUser> graphUsers = graphUserListView.getItems();
        graphUsers.forEach(user -> {
            user.toUsers.clear();
        });

        for (GraphUser toUser : graphUsers) {
            toUser.fromUsers.clear();
            toUser.first = false;
            for (GraphUser fromUser : graphUsers) {
                if(!toUser.equals(fromUser)){
                    for (Follow follow : fromUser.followers) {
                         if(follow.getToLogin().equals(toUser.name)){
                            toUser.fromUsers.put(fromUser.name, fromUser);
                            fromUser.toUsers.put(toUser.name, toUser);
                        }
                    }
                }
            }
        }
        graphUsers.sort(SIZE_COMPARATOR);
    }


    private void startFetch(String channelName){
     
        graphUserObservableList.clear();
        graphUserRemovedObservableList.clear();
        graphUserDetailsObservableList.clear();
        progressBar.setProgress(0);
        chattersProgressBar.setProgress(0);

        new Thread(() -> {

            final Chatters chatters = Context.twitchClient.getMessagingInterface().getChatters(channelName).execute();
            final List<String> names = chatters.getAllViewers();

            int i = 0;
            for (String name : names) {
                i++;
                final User user = Fetch.fetchUserByName(name);
                if(user != null){

                    final GraphUser graphUser = fetchGraphUser(user);
                    graphUser.angle = 2 * Math.PI * new Random().nextDouble();
                   
                    final double namesCount = i;
                    Platform.runLater(() -> {
                        progressBar.setProgress(0);
                        chattersProgressBar.setProgress(namesCount/(double)names.size());
                        graphUserObservableList.add(graphUser);
                        graphUser.startAnimation();
                    });
                }
            }

            final double angle = 2 * Math.PI / graphUserObservableList.size();
            for (int j = 0; j < graphUserObservableList.size(); j++) {
                graphUserObservableList.get(j).angle = j * angle;
            }

            Platform.runLater(() -> {
                calc();
                graphUserObservableList.forEach(GraphUser::startAnimation);
            });

        }).start();
    }


    private GraphUser fetchGraphUser(User user) {
        final List<Follow> followers = Fetch.followersFromUserId(new FollowersFetchEvent(progressBar, user.getDisplayName()), null, new ArrayList<>(), user.getId(), Context.twitchClient, Context.credential);
        final GraphUser graphUser = new GraphUser(user, followers);
        return graphUser;
    }


    private static class TestActionEventHandler implements EventHandler<ActionEvent> {

        private GraphUser graphUser;
        private int cycleCount = 20;
        private double time = 0;
        private double delta = Math.PI/cycleCount;

        public TestActionEventHandler(GraphUser graphUser){
            this.graphUser = graphUser;
        }

        @Override
        public void handle(ActionEvent event) {
            time += delta;
            graphUser.radiusFactor = 20*Math.sin(time);
        }
    }


    private static class GraphUser {

        public boolean first = true;
        public double x;
        public double y;
        public double radius = 1;
        public double radiusFactor = 1;
        public double angle = 0;
        public boolean selected = false;
        public boolean joined =true;
        public int joinCount = 1;
        public final String name;
        public final List<Follow> followers;
        public final Map<String, GraphUser> fromUsers = new HashMap<>();
        public final Map<String, GraphUser> toUsers = new HashMap<>();
        public GraphUser(User user, List<Follow> followers){
            this.name= user.getLogin();
            this.followers = followers;
        }

        public void startAnimation(){
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), new TestActionEventHandler(this)));
            timeline.setCycleCount(20);
            timeline.play();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GraphUser user = (GraphUser) o;
            return Objects.equals(name, user.name);
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name+" / "+followers.size()+" {"+toUsers.size()+"} "+fromUsers.size()+" / "+fromUsers.keySet();
        }

        public int size(){
            return fromUsers.size();
        }
    }


    private static class FollowersFetchEvent extends FetchEvent {

        private final String name;
        private final ProgressBar progressBar;
        public FollowersFetchEvent(ProgressBar progressBar, String name) {
            super(progressBar);
            this.progressBar = progressBar;
            this.name = name;
        }

        @Override
        public void onFetch(int size, int total) {
            System.out.println("FollowersFetchEvent "+name+" "+size+" von "+total);   
            Platform.runLater(() -> {
                progressBar.setProgress((double)size/(double)total);       
            }); 
        }
    }


    @Override
    public void handle(MouseEvent event) {

        if(event.getEventType() == MouseEvent.MOUSE_MOVED){
            selectedUser = findUser(event.getX(), event.getY());
            rotation = selectedUser == null;
        }

        if(event.getEventType() == MouseEvent.MOUSE_DRAGGED){
            if(draggedUser != null){
                //final double size = WIDTH/2;
                double dx = event.getX() - (size + left);
                double dy = event.getY() - (size + top);
                draggedUser.angle = Math.atan2(dy, dx);
            }
        }

        if(event.getEventType() == MouseEvent.MOUSE_PRESSED){
            draggedUser = selectedUser;
            if (selectedUser != null && event.getButton() == MouseButton.SECONDARY){
                if(!graphUserRemovedListView.getItems().contains(selectedUser)){
                    graphUserListView.getItems().remove(selectedUser);
                    graphUserRemovedListView.getItems().add(selectedUser);
                }
                draggedUser = selectedUser = null;
            }
        }

        if(event.getEventType() == MouseEvent.MOUSE_RELEASED){
            draggedUser = null;
        }
    }


    private GraphUser findUser(double mouseX, double mouseY){

        graphUserObservableList.filtered(user -> user.selected).forEach(user -> {
            user.selected = false;
        });
        for (GraphUser user : graphUserObservableList) {
            if(showAll || user.size() > 0 || user.toUsers.size() > 0 || user.first){
                final double dx = mouseX - user.x;
                final double dy = mouseY - user.y;
                final double distance = Math.sqrt(dx*dx+dy*dy);
                final double minDistance = 4 * user.radius;
                if(distance < minDistance){
                    user.selected = true;
                    return user;
                }
            }
        }
        return null;
    }


    private static class GraphUserCellRenderer extends ListCell<GraphUser> {

        @Override
        protected void updateItem(GraphUser item, boolean empty) {
            super.updateItem(item, empty);
            if(empty){
                setText(null);
            }else{
                setStyle(item.joined ? Context.cssYellowColor : Context.cssBlueColor);
                setText(item.name+" {"+item.toUsers.size()+"} "+item.size()+" ("+item.followers.size()+")");
            }
            setGraphic(null);
        }
    }


    private static class GraphUserDetailsCellRenderer extends ListCell<GraphUser> {

        @Override
        protected void updateItem(GraphUser item, boolean empty) {
            super.updateItem(item, empty);
            if(empty){
                setText(null);
            }else{
                setText(item.name+" "+item.fromUsers.keySet().toString());
            }
            setGraphic(null);
        }
    }


    private static class DrawUserList {

        public static final List<DrawUser> list = new ArrayList<>();

        public static void add(GraphUser user){
            list.add(new DrawUser(user));
        }

        public static void remove(){
            final List<DrawUser> removeList = list.stream().filter(d -> d.remove).collect(Collectors.toList());
            list.removeAll(removeList);
        }

        public static void render(GraphicsContext gc){
            remove();
            double y = 0;
            for (DrawUser drawUser : list) {
                y += 20;
                drawUser.render(gc, y);
            }
        }

        private static class DrawUser {

            public boolean remove = false;
            public double time = 0;
            public final String text;
            public final Color color;
            public DrawUser(GraphUser user){
                this.text = user.name+" ("+user.joinCount+")";
                this.color = user.joined ? colorYellow : colorBlue;
            }

            public void render(GraphicsContext gc, double y){
                    time += 1;
                    gc.setFill(color);
                    gc.fillText(text, 10, y);
                    if(time > 2000)
                        remove = true;
            }
        }
    }
    

}
