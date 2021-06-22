package de.calitobundo.twitch.desktop.graph;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import com.github.twitch4j.helix.domain.Follow;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.User;

import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.api.UweColors;
import de.calitobundo.twitch.desktop.data.PersistData;
import de.calitobundo.twitch.desktop.graph.GraphUtils.ColorFading;
import de.calitobundo.twitch.desktop.graph.GraphUtils.ColorSet;
import de.calitobundo.twitch.desktop.views.UserListView;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;


public class GraphUser {

    private final User user;
    public final String id;
    public final String login;
    public final String displayName;

    private String nameColorHex = UweColors.hexColorLightGray;
    private Color nameColor = Color.valueOf(nameColorHex);
    private ColorFading nameColorFading = new ColorFading(new ColorSet(UweColors.colorDarkGray), new ColorSet(nameColor));
    public double nameColorFadingFrac = 1;

    public Stream stream = null;

    public double x;
    public double y;
    public double radius = 1;
    public double radiusFactor = 0;
    public double angle = 2 * Math.PI * new Random().nextDouble();

    public boolean first = true;
    public boolean selected = false;

    public boolean joined = true;
    public boolean followed = false;
    public boolean banned = false;
    public boolean removed = false;
    public boolean ignored = false;

    private int joinCount = 0;
    private int leaveCount = 0;
    public double fetchProgress = 0;

    public long timeJoined = 0;
    public long timeLeaved = 0;

    public List<Follow> followers;

    public final Map<String, GraphUser> fromUsers = new HashMap<>();
    public final Map<String, GraphUser> toUsers = new HashMap<>();

    public final StringProperty timeAgoPropertie = new SimpleStringProperty("0");

    public final GraphUserLayout layout;
    
    public GraphUser(User user, List<Follow> followers){
        this.id = user.getId();
        this.login = user.getLogin();
        this.displayName = user.getDisplayName();
        this.user = user;
        this.removed = PersistData.removed(login);
        this.ignored = PersistData.ignored(login);
        setFollowers(followers);
        layout = new GraphUserLayout();
        layout.update();
        Timeline timeline2 = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
            timeAgoPropertie.set(GraphUtils.getTimeAgo(System.currentTimeMillis() - getTime()));
        }));
        timeline2.setCycleCount(-1);
        timeline2.play();
    }

    public void clearFollowLines(){
        fromUsers.clear();
        toUsers.clear();
        layout.update();
    }

    public void setFollowers(List<Follow> followers){
        this.followers = followers;
        followed = followers.stream().filter(follow -> follow.getToLogin().equals(Context.getChannelUser().getLogin())).findFirst().isPresent();
    }

    public void fetchFollowers(){
        setFollowers(GraphUtils.fetchFollowers(user, (size, total) -> {
            fetchProgress = size / total;
            System.out.println(login+" fetchFollowers "+size+" of "+total+" -> "+fetchProgress*100+"%");
        }));
    }

    public long getTime(){
        if(joined){
            return timeJoined;
        }else{
            return timeLeaved;
        }
    }

    public Color getColor(){
        return nameColor;
    }
    public void setColor(String colorHex){
        nameColorHex = colorHex == null ? UweColors.hexColorGray : colorHex;
        nameColor = Color.valueOf(nameColorHex);
        nameColorFading = new ColorFading(new ColorSet(UweColors.colorDarkGray), new ColorSet(nameColor));
    }

    public void setRemoved(boolean removed){
        this.removed = removed;
    }

    public void setIgnored(boolean ignored){
        this.ignored = ignored;
    }

    public void setBanned(boolean banned){
        this.banned = banned;
    }

    public void setStream(Stream stream){
        this.stream = stream;
    }

    public void joined(){
        joined = true;
        joinCount++;
        timeJoined = System.currentTimeMillis();
    }

    public void leaved(){
        joined = false;
        leaveCount++;
        timeLeaved = System.currentTimeMillis();
    }

    public void update(){
        layout.update();
    }

    public void startAnimation(){
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), new TestActionEventHandler(this)));
        timeline.setCycleCount(50);
        timeline.play();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphUser graphUser = (GraphUser) o;
        return Objects.equals(login, graphUser.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return login+" / "+followers.size()+" {"+toUsers.size()+"} "+fromUsers.size()+" / "+fromUsers.keySet();
    }

    public int size(){
        return fromUsers.size();
    }

    public double angle(){
        return angle;
    }

    private static class TestActionEventHandler implements EventHandler<ActionEvent> {

        private GraphUser graphUser;
        private int cycleCount = 50;
        private double time = 0;
        private double delta = Math.PI/cycleCount;

        public TestActionEventHandler(GraphUser graphUser){
            this.graphUser = graphUser;
        }

        @Override
        public void handle(ActionEvent event) {
            time += delta;
            graphUser.radiusFactor = getFactor(time);
        }

        private double getFactor(double time){
            return Math.sin(time);
        }
    }



    public class GraphUserLayout extends HBox {

        private final Circle joinedCircle = new Circle(8);
        private final Circle statusCircle = new Circle(5);
        private final StackPane statusLayout = new StackPane(joinedCircle, statusCircle);

        private final Circle followCircle = new Circle(8);
        private final Circle notCalcCircle = new Circle(5);
        private final StackPane followStatusLayout = new StackPane(followCircle, notCalcCircle);

        private final Circle bannedCircle = new Circle(8);
        private final Circle followedCircle = new Circle(5);
        private final StackPane bannStatusLayout = new StackPane(bannedCircle, followedCircle);

        private final Circle ignoreCircle = new Circle(5);
        private final StackPane ignoreStatusLayout = new StackPane(ignoreCircle);

        private final Circle liveCircle = new Circle(8);

        private final Label userNameLabel = new Label();
        private final Label timeLabel = new Label();
        private final Label viewerCountLabel = new Label();

        // layout2
        private final Label createdAtLabel = new Label();
        private final Label viewCountLabel = new Label();

        // layout3
        private final Label joinDurationLabel = new Label();
        private final Label leavDurationLabel = new Label();

        private final Region spacerGrowAlways = new Region();
        private final Region spacer2 = new Region();
        private final Region spacer3 = new Region();
        private final Region spacer4 = new Region();
        private final Region spacer5 = new Region();
        private final Region spacer55 = new Region();
        private final Region spacer555 = new Region();
        private final Region spacer6 = new Region();
        private final Region spacer7 = new Region();

        //private final Tooltip tooltip = new Tooltip();


        private final String cssString = "-fx-fill:linear-gradient( to right, ";
        private final String cssString2 = "%s 0.5,%s 0.5);";

        private final HBox layout1 = new HBox(userNameLabel, spacer5, timeLabel);
        private final HBox layout2 = new HBox(createdAtLabel, spacer55, viewCountLabel);
        private final HBox layout3 = new HBox(joinDurationLabel, spacer555, leavDurationLabel);
        private final HBox layout = new HBox(layout1);


        public GraphUserLayout(){

            //tooltip.setGraphic(new Label(user.getDescription()));

            final long duration = Instant.now().toEpochMilli() - user.getCreatedAt().toEpochMilli();
            
            createdAtLabel.setText("for "+GraphUtils.getTimeAgo(duration));
            viewCountLabel.setText("views "+GraphUtils.getViewsShortString(user.getViewCount()));

            joinDurationLabel.setStyle(UweColors.cssYellowColor);
            leavDurationLabel.setStyle(UweColors.cssBlueColor);
            
            setHgrow(spacerGrowAlways, Priority.ALWAYS);
            spacer2.setPrefWidth(5);
            spacer3.setPrefWidth(5);
            spacer4.setPrefWidth(5);
            spacer5.setPrefWidth(5);
            spacer55.setPrefWidth(5);
            spacer555.setPrefWidth(5);
            spacer6.setPrefWidth(5);
            spacer7.setPrefWidth(5);


            setPadding(new Insets(1, 4, 1, 4));
            getChildren().addAll(statusLayout, spacer4, followStatusLayout, spacer3, bannStatusLayout, spacer6, ignoreStatusLayout, spacer7, layout, spacerGrowAlways, viewerCountLabel, spacer2, liveCircle);

            timeLabel.textProperty().bind(timeAgoPropertie);
            final String displayName = user.getDisplayName();
            userNameLabel.setText(displayName.length() > 16 ? displayName.substring(0, 16).concat(".") : displayName);

            statusCircle.setOnMouseClicked(e -> {
                setRemoved(!removed);
                update();
                UserListView.calc();
            });
   
            joinedCircle.setOnMouseClicked(e -> {
                setRemoved(!removed);
                update();
                UserListView.calc();
            });

            bannedCircle.setOnMouseClicked(e -> {
                resetColor();
                update();
                fetchFollowers();
                update();
            });

            followedCircle.setOnMouseClicked(e -> {
                resetColor();
                update();
                fetchFollowers();
                update();
            });

            ignoreCircle.setOnMouseClicked(e -> {
                setIgnored(!ignored);
                update();
                if(ignored){
                    PersistData.addToIgnoredUserAndSave(login);
                }else{
                    PersistData.removeFromIgnoredUserAndSave(login);
                }
            });

            setOnMouseEntered(e -> {
                selected = true;
                final Animation animation = new Transition() {
                    {
                        setCycleDuration(Duration.millis(500));
                        setInterpolator(Interpolator.EASE_IN);
                    }
                    @Override
                    protected void interpolate(double frac) {
                        nameColorFadingFrac = frac;
                        update();
                    }
                };
                animation.play();
            });

            setOnMouseExited(e -> {
                selected = false;
            });


            liveCircle.setOnMouseEntered(e -> {
                layout.getChildren().setAll(layout2);
            });

            liveCircle.setOnMouseExited(e -> {
                layout.getChildren().setAll(layout1);
            });

            liveCircle.setOnMouseClicked(e -> {
                layout.getChildren().setAll(layout3);
                update();
                // if(layout.getChildren().contains(layout3)){
                //     layout.getChildren().setAll(layout2);
                // }else{
                // }
            });

            // liveCircle.setOnMouseReleased(e -> {

            //     layout.getChildren().clear();
            //     System.out.println(e.getSource());
            //     if(e.getSource() instanceof Circle){
            //         layout.getChildren().add(layout2);
            //     }else{
            //         layout.getChildren().add(layout1);
            //     }

            // });
        }

        private void resetColor(){
            bannedCircle.setFill(UweColors.colorDarkerGray);
            followedCircle.setFill(UweColors.colorDarkerGray);
        }

        public GraphUserLayout update(){
  

            if(layout.getChildren().contains(layout3)){
                if(joinCount > 0){
                    final long duration1 = Instant.now().toEpochMilli() - timeJoined;
                    joinDurationLabel.setText(GraphUtils.getTimeAgo(duration1));
                }else{
                    joinDurationLabel.setText("0");
                }
                if(leaveCount > 0){
                    final long duration2 = Instant.now().toEpochMilli() - timeLeaved;
                    leavDurationLabel.setText(GraphUtils.getTimeAgo(duration2));
                }else{
                    leavDurationLabel.setText("0");
                }
            }

            // joined and removed circle
            joinedCircle.setFill(joined ? UweColors.colorYellow : UweColors.colorBlue);
            statusCircle.setFill(removed ? UweColors.colorDarkRed : UweColors.colorDarkerGray);

            // follow circle
            String cssColorFromUsers = fromUsers.isEmpty() ? UweColors.hexColorDarkRed : UweColors.hexColorRed;
            String cssColorToUsers = toUsers.isEmpty() ? UweColors.hexColorDarkRed : UweColors.hexColorRed;
            followCircle.setStyle(cssString+String.format(cssString2, cssColorFromUsers, cssColorToUsers));
        
            //first time circle
            notCalcCircle.setFill(first ? UweColors.colorDarkerGray : Color.TRANSPARENT);

            //bann circle
            bannedCircle.setFill(banned ? UweColors.colorRed : UweColors.colorGreen);
            followedCircle.setFill(followed ? UweColors.colorGreen : UweColors.colorDarkerGray);

            //ignore
            ignoreCircle.setFill(ignored ? UweColors.colorDarkRed : UweColors.colorDarkerGray);
            //
            userNameLabel.setStyle("-fx-text-fill: "+nameColorFading.getColorSet(nameColorFadingFrac).getString()+";");
            //userNameLabel.setTooltip(tooltip);

            timeLabel.setStyle(joined ? UweColors.cssYellowColor : UweColors.cssBlueColor);

            if(stream == null){
                liveCircle.setFill(UweColors.colorDarkerGray);
                viewerCountLabel.setText("");
            }else{
                liveCircle.setFill(UweColors.colorRed);
                viewerCountLabel.setText(String.valueOf(stream.getViewerCount()));
            }
            return this;
        }

    }

}
