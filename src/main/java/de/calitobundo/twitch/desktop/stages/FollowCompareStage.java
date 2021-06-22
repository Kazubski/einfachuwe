package de.calitobundo.twitch.desktop.stages;

import com.github.twitch4j.helix.domain.Follow;
import com.github.twitch4j.helix.domain.FollowList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;

import de.calitobundo.twitch.desktop.api.UweColors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

import static de.calitobundo.twitch.desktop.api.Context.credential;
import static de.calitobundo.twitch.desktop.api.Context.twitchClient;

public class FollowCompareStage extends Stage {

    private final TextField username1 = new TextField();
    private final TextField username2 = new TextField();
    private final Button button = new Button("vergleichen");
    private final ListView<String> listView = new ListView<String>();

    public FollowCompareStage(Stage owner) {

        setTitle("Follower Vergleich");
        initOwner(owner);
        initModality(Modality.NONE);

        VBox layout = new VBox();
        layout.setSpacing(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(username1, username2, button, listView);

        VBox.setVgrow(listView, Priority.ALWAYS);

        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> {

            if(username1.getText().isBlank() || username2.getText().isBlank())
                return;

            User user1 = getUser(username1.getText());
            User user2 = getUser(username2.getText());

            if(user1 == null || user2 == null)
                return;

            List<Follow> userFollows1 = getFollows(null, new ArrayList<>(), user1.getId());
            List<Follow> userFollows2 = getFollows(null, new ArrayList<>(), user2.getId());

            //System.out.println("user1 hat "+userFollows1.size()+" follows");
            //System.out.println("user2 hat "+userFollows2.size()+" follows");

            Set<String> equalFollows = new HashSet<>();
            for(Follow follow1 : userFollows1){
                for(Follow follow2 : userFollows2){
                    if(follow1.getToName().equals(follow2.getToName())){
                        equalFollows.add(follow1.getToName());
                    }
                }
            }

            Map<String, String> elist = userFollows2.stream()
                    .collect(Collectors.toMap(Follow::getToName, Follow::getToId));

            userFollows1.forEach(follow ->  {
                if(elist.containsKey(follow.getToName()))
                    equalFollows.add(follow.getToName());
            });

            ObservableList<String> listtt = FXCollections.observableArrayList(equalFollows);
            listView.setItems(listtt);

        });

        //layout.setStyle("-fx-base: rgba(60, 60, 60, 255);");

        Scene scene = new Scene(layout, 260, 600);
        scene.getStylesheets().add(UweColors.darkStyle);

        setScene(scene);
        centerOnScreen();
        setMinWidth(200);
        setMinHeight(400);
        setMaxWidth(300);
        setMaxHeight(800);
        //setResizable(false);
        show();

    }


    public List<Follow> getFollows(String cursor, List<Follow> list, String fromUser){

        FollowList follows = twitchClient().getHelix().getFollowers(credential.getAccessToken(), fromUser, null, cursor, 100).execute();
        list.addAll(follows.getFollows());

        if(follows.getPagination().getCursor() != null)
            getFollows(follows.getPagination().getCursor(), list, fromUser);

        return list;
    }

    public User getUser(String userName){

        UserList userResult1 = twitchClient().getHelix().getUsers(credential.getAccessToken(), null, Arrays.asList(userName)).execute();
        if(userResult1.getUsers().isEmpty())
            return null;
        else
            return userResult1.getUsers().get(0);
    }

}
