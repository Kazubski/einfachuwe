package de.calitobundo.twitch.desktop.graph;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.calitobundo.twitch.desktop.api.UweColors;
import de.calitobundo.twitch.desktop.views.ChannelInformationView;
import de.calitobundo.twitch.desktop.views.UserListView;
import de.calitobundo.twitch.desktop.views.ChannelInformationView.ChannelStatsItem;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class GraphCanvas extends VBox {
    
    private static double POPIN_SIZE = 320;
    private static double POPOUT_SIZE = 600;
    private static double WIDTH = POPIN_SIZE;
    private static double HEIGHT = POPIN_SIZE;
    private static final double LEFT = 25;
    private static final double TOP = 25;
    private double radius;

    private final GraphHandler handler;
    private final Canvas canvas = new Canvas(WIDTH, HEIGHT);

    private final Button showAllButton = new Button("A");
    private final Button showFollowsButton = new Button("F");
    private final Button showNamesButton = new Button("N");

    private final Button calcButton = new Button("Calc");
    private final Button orderButton = new Button("O");
    private final Button statsButton = new Button("Stats");

    private final Button gridButton = new Button("Grid");
    private final Button clearButton = new Button("Clear");

    private final Region spacer = new Region();


    // public static Color colorYellow = Color.rgb(252, 228, 122);
    // public static Color colorBlue = Color.rgb(124, 122, 255);
    // public static Color colorRed = Color.rgb(252, 122, 122);
    // public static Color colorDarkGray = Color.rgb(40, 40, 40);

    public boolean popout = false;

    public boolean rotation = true;
    public static boolean showAll = true;
    public boolean showList = false;
    public boolean showFollows = false;
    public boolean showNames = false;
    public boolean showStats = false;


    //stats
    public boolean showGrid = false;

    private final Pane pane;

    public GraphCanvas(GraphHandler handler){
        this.handler = handler;
        
        radius = getRadius();


        HBox.setHgrow(spacer, Priority.ALWAYS);

        final HBox inputGraphLayout = new HBox(showAllButton, showFollowsButton, showNamesButton, orderButton, calcButton);
        inputGraphLayout.setSpacing(10);

        final HBox inputStatsLayout = new HBox(gridButton);
        inputStatsLayout.setSpacing(10);

        final HBox inputLayoutWrapper = new HBox(inputGraphLayout, spacer, statsButton);
        inputGraphLayout.setSpacing(10);


        pane = new Pane(canvas);
        
        getChildren().addAll(inputLayoutWrapper, pane);
        setSpacing(10);

        showAllButton.setOnAction(e -> showAll = !showAll);  
        showFollowsButton.setOnAction(e -> showFollows = !showFollows);
        showNamesButton.setOnAction(e -> showNames = !showNames);
        statsButton.setOnAction(e ->  {
            showStats = !showStats;
            if(showStats){
                inputLayoutWrapper.getChildren().remove(inputGraphLayout);
                inputLayoutWrapper.getChildren().add(0, inputStatsLayout);
            }else{               
                inputLayoutWrapper.getChildren().remove(inputStatsLayout);
                inputLayoutWrapper.getChildren().add(0, inputGraphLayout);
            }
        });
        gridButton.setOnAction(e ->  {
            showGrid = !showGrid;
        });
        clearButton.setOnAction(e ->  {
            ChannelInformationView.stats.clear();
        });
        
        calcButton.setOnAction(e -> {
            UserListView.calc();
        });

        orderButton.setOnAction(e -> {
            GraphUtils.orderUsers(handler.getGraphUsers());
        });

        final GraphMouseEventHandler mouseEventHandler = new GraphMouseEventHandler(handler);

        canvas.setOnMouseClicked(mouseEventHandler);
        canvas.setOnMousePressed(mouseEventHandler);
        canvas.setOnMouseMoved(mouseEventHandler);
        canvas.setOnMouseReleased(mouseEventHandler);
        canvas.setOnMouseDragged(mouseEventHandler);
        canvas.requestFocus();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            render();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public boolean setPopout(){
        if(popout)
            return true;
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.widthProperty());
        pane.setPrefSize(POPOUT_SIZE, POPOUT_SIZE);
        popout = true;
        return false;
    }

    public void setPopin(){
        if(!popout)
            return;
        canvas.widthProperty().unbind();
        canvas.heightProperty().unbind();
        pane.setPrefSize(POPIN_SIZE, POPIN_SIZE);
        canvas.setWidth(POPIN_SIZE);
        canvas.setHeight(POPIN_SIZE);
        popout = false;
    }

    public double getCenter(){
        return WIDTH/2;
    }

    public double getRadius(){
        return (WIDTH-2*LEFT)/2;
    }

    private void render(){

        final GraphicsContext gc = canvas.getGraphicsContext2D();
        WIDTH = canvas.getWidth();
        HEIGHT = canvas.getHeight();
        radius = getRadius();
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        if(showStats){
            renderStats(gc);
        }else{
            renderGraphUsers(gc);
        }
    }


    private void renderStats(GraphicsContext gc){

        final List<ChannelStatsItem> stats = ChannelInformationView.stats.stream().collect(Collectors.toList());

        final int size = stats.size();

        if(size < 2)
            return;

        final ChannelStatsItem lastItem = stats.get(size-1);

        final double min = stats.get(0).time;
        final double max = stats.get(size-1).time;
        final double factor = (WIDTH-2*LEFT) / (max - min);
        final Data timeData = new Data(min, max);


        //grid
        gc.setStroke(UweColors.colorDarkGray);
        gc.strokeLine(LEFT, HEIGHT-TOP, WIDTH-LEFT, HEIGHT-TOP);
        gc.strokeLine(LEFT, TOP, LEFT, HEIGHT-TOP);
        if(showGrid && size > 10){
            final double deltaX = (max-min)/10;
            for (int i = 1; i < 10; i++) {
                double x = LEFT + (deltaX*i) * factor;
                gc.strokeLine(x, HEIGHT-TOP+5, x, HEIGHT-TOP-5);
            }
        }

        double maxLeavedCount = (double)GraphUtils.getMax(1, stats.stream().map(s -> s.leavedCount));
        double maxViewerCount = (double)GraphUtils.getMax(1, stats.stream().map(s -> s.viewerCount));
        double maxChatterCount = (double)GraphUtils.getMax(1, stats.stream().map(s -> s.chatterCount));
        double maxFollowersCount = (double)GraphUtils.getMax(1, stats.stream().map(s -> s.followersCount));
        
        double maxValue = Math.max(maxViewerCount, maxChatterCount);
        maxValue = Math.max(maxValue, maxFollowersCount);

        if(showGrid){
            gc.setFill(UweColors.colorGray);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf((int)maxValue), LEFT, 19);
            gc.setTextAlign(TextAlignment.LEFT);
        }

        final List<Data> leavedDataList = stats.stream().map(stat2 -> new Data(stat2.time, stat2.leavedCount)).collect(Collectors.toList());
        renderGraph(gc, UweColors.colorDarkGray, leavedDataList, timeData, factor, maxLeavedCount);

        final List<Data> viewerCountDataList = stats.stream().map(stat2 -> new Data(stat2.time, stat2.viewerCount)).collect(Collectors.toList());
        renderGraph(gc, UweColors.colorRed, viewerCountDataList, timeData, factor, maxValue);
        
        final List<Data> chatterDataList = stats.stream().map(stat2 -> new Data(stat2.time, stat2.chatterCount)).collect(Collectors.toList());
        renderGraph(gc, UweColors.colorYellow, chatterDataList, timeData, factor, maxValue);

        final List<Data> followerDataList = stats.stream().map(stat2 -> new Data(stat2.time, stat2.followersCount)).collect(Collectors.toList());
        renderGraph(gc, UweColors.colorGreen, followerDataList, timeData, factor, maxValue);


        double startX = WIDTH-LEFT-10;

        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(UweColors.colorRed);
        gc.fillText("Viewers", startX-40, HEIGHT-TOP-60);
        gc.fillText(String.valueOf(lastItem.viewerCount), startX, HEIGHT-TOP-60);
        gc.setFill(UweColors.colorYellow);
        gc.fillText("Chatters", startX-40, HEIGHT-TOP-45);
        gc.fillText(String.valueOf(lastItem.chatterCount), startX, HEIGHT-TOP-45);
        gc.setFill(UweColors.colorGreen);
        gc.fillText("Followers", startX-40, HEIGHT-TOP-30);
        gc.fillText(String.valueOf(lastItem.followersCount), startX, HEIGHT-TOP-30);
        gc.setFill(UweColors.colorDarkGray);
        gc.fillText("All", startX-40, HEIGHT-TOP-15);
        gc.fillText(String.valueOf(lastItem.leavedCount), startX, HEIGHT-TOP-15);

        gc.setFill(UweColors.colorGray);
        gc.fillText("Time", startX-40, HEIGHT-5);
        gc.fillText(String.valueOf((long)((max-min)/60000)), startX, HEIGHT-5);
        gc.setTextAlign(TextAlignment.LEFT);

    }

    private void renderGraph(GraphicsContext gc, Color color, List<Data> list, Data timeData, double factor, double max){
        gc.setStroke(color);
        gc.beginPath();
        Data data = list.get(0);
        double x = LEFT + (data.x - timeData.x) * factor;
        double y = HEIGHT - TOP - (data.y / max) * (HEIGHT-2*TOP);
        gc.moveTo(x, y);
        for (int i = 1; i < list.size(); i++) {
            data = list.get(i);
            x = LEFT + (data.x - timeData.x) * factor;
            y = HEIGHT - TOP - (data.y / max) * (HEIGHT-2*TOP);
            gc.lineTo(x, y);
        }
        gc.stroke();
    }

    public static class Data {
        final double x;
        final double y;
        public Data(double x, double y){
            this.x = x;
            this.y = y;
        }
    }


    private void renderGraphUsers(GraphicsContext gc){


        final List<GraphUser> graphUserList = handler.getGraphUsers();

        long countJoined = graphUserList.stream().filter(g -> g.joined && !g.removed && !g.ignored).count();
        long countLeaved = graphUserList.stream().filter(g -> !g.joined && !g.removed && !g.ignored).count();
        long countFirst = graphUserList.stream().filter(g -> g.first && !g.ignored).count();
        long countRemoved = graphUserList.stream().filter(g -> g.removed && !g.ignored).count();

        final List<GraphUser> graphUsers = graphUserList.stream().filter(item -> !item.removed && !item.ignored).collect(Collectors.toList());
        int max = GraphUtils.getMax(0, graphUsers);

        // draw list size
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.rgb(200, 200, 200));
        gc.fillText(String.valueOf(graphUserList.size()), WIDTH-10, 20);
        gc.setFill(UweColors.colorYellow);
        gc.fillText(String.valueOf(countJoined), WIDTH-10, 35);
        gc.setFill(UweColors.colorBlue);
        gc.fillText(String.valueOf(countLeaved), WIDTH-10, 50);
        gc.setFill(UweColors.colorGray);
        gc.fillText(String.valueOf(countFirst), WIDTH-10, 65);
        gc.setFill(UweColors.colorRed);
        gc.fillText(String.valueOf(countRemoved), WIDTH-10, 80);
        gc.setTextAlign(TextAlignment.LEFT);


        // draw circles
        gc.setStroke(Color.rgb(60, 60, 60));
        gc.strokeOval(LEFT, TOP, 2*radius, 2*radius);
        for (int i = 1; i <= max; i++) {
            double circleRadius = radius - (radius/(double)max) * (double)i;
            gc.strokeOval(LEFT+radius-circleRadius, TOP+radius-circleRadius, 2*circleRadius, 2*circleRadius);
        }

        // calc user position
        for (GraphUser user : graphUsers) {
            double userRadius = user.first ? radius : radius - (radius/(double)max) * user.fromUsers.size();
            user.angle += Math.PI/512;
            user.x = LEFT + radius + userRadius * Math.cos(user.angle);
            user.y = TOP + radius + userRadius * Math.sin(user.angle);
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

            final double radius = user.radius + 20 * user.radiusFactor;
            if(showAll || user.fromUsers.size() > 0 || user.toUsers.size() > 0 || user.first){

                gc.setFill(user.joined ? UweColors.colorYellow : UweColors.colorBlue);
                gc.fillOval(user.x-radius, user.y-radius, 2*radius, 2*radius);

                if(showNames && !user.selected){

                    gc.setFill(user.getColor());
                    gc.fillText(user.login, user.x+10, user.y);

                }

                if(user.selected){
                    gc.setFill(Color.WHEAT);
                    gc.fillText(user.login, user.x+10, user.y);
                    gc.fillText(user.toString(), 10, HEIGHT-10);
                }
            }
        }
        //GraphUserEventList.render(gc);
    }





}
