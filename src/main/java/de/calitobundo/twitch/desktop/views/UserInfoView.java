package de.calitobundo.twitch.desktop.views;

import static de.calitobundo.twitch.desktop.api.Context.credential;
import static de.calitobundo.twitch.desktop.api.Context.twitchClient;
import static de.calitobundo.twitch.desktop.api.Fetch.fetchFollowByIds;
import static de.calitobundo.twitch.desktop.api.Fetch.fetchUserByName;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import com.github.twitch4j.helix.domain.BannedUser;
import com.github.twitch4j.helix.domain.BannedUserList;
import com.github.twitch4j.helix.domain.Follow;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.kraken.domain.KrakenUser;
import com.github.twitch4j.kraken.domain.KrakenUserList;

import de.calitobundo.twitch.desktop.ImageUtils;
import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.event.EventHandler;
import de.calitobundo.twitch.desktop.graph.GraphUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class UserInfoView extends VBox {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


    private final Button followButton = new Button("Follow");
    private final Button timeoutButton = new Button("Timeout");
    private final Button banButton = new Button("Ban");
    private final Button twitchButton = new Button("Open in Browser");

    private final Circle circle = new Circle(48); //profileImageView
    private final ImageView offlineTumbnailImageView = new ImageView();

    //info
    private final Label followFromSizeLabel = new Label("followFrom");
    private final Label followToSizeLabel = new Label("followTo");
    private final Label clipsSizeLabel = new Label("clipsSize");

    private final Label followFromSizeLabel2 = new Label();
    private final Label followToSizeLabel2 = new Label();
    private final Label clipsSizeLabel2 = new Label();

    //user
    private final Label loginLabel = new Label("login");
    private final Label displayNameLabel = new Label("displayName");
    private final Label viewCountLabel = new Label("viewCount");
    private final Label broadcasterTypeLabel = new Label("broadcaster");
    private final Label createdAtLabel = new Label("createdAt");
    private final Label descriptionLabel = new Label("description");
    private final Label followedAtLabel = new Label("followedAt");
    private final Label bannedAtLabel = new Label("bannedFor");

    private final TextField loginTextField = new TextField("login");
    private final TextField displayNameTextField = new TextField("displayName");
    private final TextField viewCountTextField = new TextField("viewCount");
    private final TextField broadcasterTypeTextField = new TextField("broadcaster");
    private final TextField createdAtTextField = new TextField("createdAt");
    private final TextArea descriptionTextArea = new TextArea("description");
    private final TextField followedAtTextField = new TextField("followedAt");
    private final Button followDeleteButton = new Button("Delete");

    private final TextField bannedAtTextField = new TextField("bannedFor");
    private final Button bannedButton = new Button("Ban");

    private final EventHandler handler;

    public UserInfoView(EventHandler handler) {
        this.handler = handler;

        followFromSizeLabel2.setStyle(Context.cssRedColor);
        followToSizeLabel2.setStyle(Context.cssRedColor);
        clipsSizeLabel2.setStyle(Context.cssRedColor);

        followDeleteButton.setPrefWidth(100);

        final HBox followedLayout = new HBox(followedAtTextField, followDeleteButton);
        followedLayout.setSpacing(5);
        HBox.setHgrow(followedAtTextField, Priority.ALWAYS);

        bannedButton.setPrefWidth(100);

        final HBox bannedLayout = new HBox(bannedAtTextField, bannedButton);
        bannedLayout.setSpacing(5);
        HBox.setHgrow(bannedAtTextField, Priority.ALWAYS);

        final GridPane infoLayout = new GridPane();
        infoLayout.setVgap(5);
        loginLabel.setMinWidth(100);
        infoLayout.add(loginLabel, 0, 0);
        infoLayout.add(loginTextField, 1, 0);
        infoLayout.add(displayNameLabel, 0, 1);
        infoLayout.add(displayNameTextField, 1, 1);
        infoLayout.add(viewCountLabel, 0, 2);
        infoLayout.add(viewCountTextField, 1, 2);
        infoLayout.add(broadcasterTypeLabel, 0, 3);
        infoLayout.add(broadcasterTypeTextField, 1, 3);
        infoLayout.add(createdAtLabel, 0, 4);
        infoLayout.add(createdAtTextField, 1, 4);
        infoLayout.add(descriptionLabel, 0, 5);
        infoLayout.add(descriptionTextArea, 1, 5);
        infoLayout.add(followedAtLabel, 0, 6);
        infoLayout.add(followedLayout, 1, 6);
        infoLayout.add(bannedAtLabel, 0, 7);
        infoLayout.add(bannedLayout, 1, 7);

        final HBox sizeLayout = new HBox(followFromSizeLabel, followFromSizeLabel2, followToSizeLabel, followToSizeLabel2, clipsSizeLabel, clipsSizeLabel2);
        sizeLayout.setSpacing(10);

        final VBox actionLayout = new VBox();
        sizeLayout.setSpacing(5);
        actionLayout.getChildren().addAll(followButton, timeoutButton, banButton, twitchButton);
        followButton.setMaxWidth(Double.MAX_VALUE);
        timeoutButton.setMaxWidth(Double.MAX_VALUE);
        banButton.setMaxWidth(Double.MAX_VALUE);
        twitchButton.setMaxWidth(Double.MAX_VALUE);

        //user
        loginTextField.setEditable(false);
        displayNameTextField.setEditable(false);
        viewCountTextField.setEditable(false);
        broadcasterTypeTextField.setEditable(false);
        createdAtTextField.setEditable(false);

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setWrapText(true);
        descriptionTextArea.setMinHeight(120);
        descriptionTextArea.setPrefHeight(120);

        followedAtTextField.setEditable(false);

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        final HBox topLayout = new HBox();
        topLayout.getChildren().addAll(circle, spacer, offlineTumbnailImageView, spacer2, actionLayout);

        getChildren().addAll(topLayout, sizeLayout, infoLayout);
        setSpacing(10);
        setPadding(new Insets(10, 0, 0, 0));

        followButton.setOnAction(e -> {
            final User fromUser = fetchUserByName("einfachuwe42");
            final String userName = loginTextField.getText();
            final User toUser = fetchUserByName(userName);
            if (fromUser == null || toUser == null)
                return;
            if (fromUser.getId().equals(toUser.getId())) {
                System.out.println("fromId == toId");
                return;
            }
            followButton.setDisable(true);
            if (followButton.getText().equals("Follow")) {
                new Thread(() -> {
                    twitchClient.getHelix().createFollow(credential.getAccessToken(), fromUser.getId(), toUser.getId(), false).execute();
                    Platform.runLater(() -> {
                        followButton.setDisable(false);
                        followButton.setText("Unfollow");
                    });
                }).start();
            } else {
                new Thread(() -> {
                    twitchClient.getHelix().deleteFollow(credential.getAccessToken(), fromUser.getId(), toUser.getId()).execute();
                    Platform.runLater(() -> {
                        followButton.setDisable(false);
                        followButton.setText("Follow");
                    });
                }).start();
            }
        });

        timeoutButton.setOnAction(e -> {
            final String userName = loginTextField.getText();
            if (timeoutButton.getText().equals("Timeout")) {
                twitchClient.getChat().sendMessage("einfachuwe42", "/timeout " + userName + " 5");
                timeoutButton.setText("Untimeout");
            } else {
                twitchClient.getChat().sendMessage("einfachuwe42", "/untimeout " + userName);
                timeoutButton.setText("Timeout");
            }
            fetchUserB(userName);
        });

        banButton.setOnAction(e -> {
            final String userName = loginTextField.getText();
            if (banButton.getText().equals("Ban")) {
                twitchClient.getChat().sendMessage("einfachuwe42", "/ban " + userName);
                banButton.setText("Unban");
            } else {
                twitchClient.getChat().sendMessage("einfachuwe42", "/unban " + userName);
                banButton.setText("Ban");
            }
            fetchUserB(userName);
        });

        twitchButton.setOnAction(e -> {
            final String url = "https://www.twitch.tv/"+loginTextField.getText();
            System.out.println("openbrowser "+url);
            try {
                handler.openWebsite(new URI(url));
            } catch (URISyntaxException ignored){
            }
        });

        // videoPlayerButton.setOnAction(e -> {
        //     final String userName = loginTextField.getText();
        //     videoPlayer.start(userName);
        // });

        followDeleteButton.setOnAction(e -> {
            final String fromId = user.getId();
            final String toId = Context.getChannelUser().getId();
            Context.twitchClient.getHelix().deleteFollow(Context.credential.getAccessToken(), fromId, toId).execute();
            System.out.println("followDeleteButton fromId: "+fromId+" toId:"+toId);
            fetchUserFollows(user);

            // blockedUsersTextArea.clear();
            // new Thread(() -> {
            //     List<BlockedUser> blockedUsers = Fetch.blockedUsers((size, total) -> {
            //         System.out.println("size: "+size);
            //     }, null, new ArrayList<>(), toId, twitchClient, credential);
            //     Platform.runLater(() -> {
            //         blockedUsers.forEach(user -> {
            //             blockedUsersTextArea.appendText(user.getUserLogin()+"\n");
            //         });
            //     });
            // }).start();
        });

        bannedButton.setOnAction(e -> {
            final String userName = loginTextField.getText();
            if (banButton.getText().equals("Ban")) {
                twitchClient.getChat().sendMessage("einfachuwe42", "/ban " + userName);
                banButton.setText("Unban");
                bannedButton.setText("Unban");
            } else {
                twitchClient.getChat().sendMessage("einfachuwe42", "/unban " + userName);
                banButton.setText("Ban");
                bannedButton.setText("Ban");
            }
            fetchUserB(userName);
        });

    }

    private User user = null;
    
    public void loadUser(User user) {
        this.user = user;
        fetchUserDetails(user);
    }

    private void fetchUserB(String userName) {
        new Thread(() -> {
            final User user = fetchUserByName(userName);
            if(user == null)
                return;
            fetchUserBanned(user);
        }).start();
    }


    private void fetchUserDetails(User user) {

        //viewCountSizeLabel2.setText("");
        followFromSizeLabel2.setText("");
        followToSizeLabel2.setText("");
        clipsSizeLabel2.setText("");
        new Thread(() -> {

            Platform.runLater(() -> {
                //user.getEmail();
                //user.getOfflineImageUrl();
                //user.getType();
                //idTextField.setText(user.getId());

                loginTextField.setText(user.getLogin());
                viewCountTextField.setText(String.valueOf(user.getViewCount()));
                //viewCountSizeLabel2.setText(String.valueOf(user.getViewCount()));
                displayNameTextField.setText(user.getDisplayName());
                broadcasterTypeTextField.setText(user.getBroadcasterType());

                String resultString = user.getDescription().replaceAll(Context.regexLabel, "");
                descriptionTextArea.setText(resultString);

                Image image = ImageUtils.fetchImage(user.getProfileImageUrl(), 96, 96);

                circle.setFill(Color.BLACK);

                if(image != null)
                    circle.setFill(new ImagePattern(image));

                Image offlineImage = ImageUtils.fetchImage(user.getOfflineImageUrl(), 170, 96);
                offlineTumbnailImageView.setImage(offlineImage);

             });

            fetchUserFollows(user);
            fetchUserBanned(user);


            // users account created_at from kraken
            new Thread(() -> {
                final KrakenUserList userList = twitchClient().getKraken().getUsersByLogin(Collections.singletonList(user.getLogin())).execute();
                if (userList.getUsers() != null && !userList.getUsers().isEmpty()){
                    final KrakenUser user2 = userList.getUsers().get(0);
                    Platform.runLater(() -> {
                        final Instant createdInstant = user2.getCreatedAtInstant();
                        final Instant nowInstant = Instant.now();
                        final Duration between = Duration.between(createdInstant, nowInstant);
                        final long hours = between.toHours();
                        if(hours < 24){
                            createdAtTextField.setStyle("-fx-text-fill: rgba(252, 3, 3, 255);");
                        }else{
                            createdAtTextField.setStyle("-fx-text-fill: rgba(255, 255, 255, 255);");
                        }
                        final Date myDate = Date.from(user2.getCreatedAtInstant());
                        final String formattedDate = formatter.format(myDate);
                        createdAtTextField.setText(formattedDate);
                    });
                }
            }).start();

           // handler.loadClips(user.getLogin());

        }).start();
    }


    public void deleteFollow(String fromId, String toId){

        Context.twitchClient.getHelix().deleteFollow(Context.credential.getAccessToken(), fromId, toId);

        //Context.twitchClient.getHelix().blockUser(Context.credential.getAccessToken(), targetUserId, sourceContext, reason)
        // Context.twitchClient.getHelix().getUserBlockList(Context.credential.getAccessToken(), userId, after, limit);
        // Context.twitchClient.getHelix().getBannedUsers(authToken, broadcasterId, userId, after, before);
        // Context.twitchClient.getChat().ban(channel, user, reason)
        // Context.twitchClient.getChat().unban(channel, user, reason)
        // Context.twitchClient.getChat().setSubscribersOnly(channel, user, reason)
        // Context.twitchClient.getChat().setFollowersOnly(channel, time);

    }


    private void fetchUserFollows(User user) {

        new Thread(() -> {

            // is user following einfachuwe42
            //final User user1 = fetchUserByName("einfachuwe42");
            final User user1 = Context.getChannelUser();
            //if(user == null || user1 == null)
            //    return;
            final Follow follow = fetchFollowByIds(user.getId(), user1.getId());
            if(follow == null) {
                Platform.runLater(() -> {
                    followedAtTextField.clear();
                });
            }else{
                Platform.runLater(() -> {
                    final Date myDate = Date.from(follow.getFollowedAtInstant());
                    final String formattedDate = formatter.format(myDate);
                    followedAtTextField.setText(formattedDate);
                });
            }

            // is einfachuwe42 following user
            final Follow follow1 = fetchFollowByIds(user1.getId(), user.getId());
            if(follow1 == null) {
                Platform.runLater(() -> {
                    followButton.setText("Follow");
                });
            }else{
                Platform.runLater(() -> {
                    followButton.setText("Unfollow");
                });
            }

        }).start();
    }
    

    private void fetchUserBanned(User user){

        new Thread(() -> {
            // is user banned
            final User user1 = fetchUserByName("einfachuwe42");
            if(user == null || user1 == null)
                return;
            final String bId = user1.getId();
            final BannedUserList bannedUsersList = twitchClient.getHelix().getBannedUsers(credential.getAccessToken(), bId, Collections.singletonList(user.getId()), null, null).execute();
            if(bannedUsersList.getResults() != null && !bannedUsersList.getResults().isEmpty()){

                final BannedUser bannedUser = bannedUsersList.getResults().get(0);
                final Instant expiresAt = bannedUser.getExpiresAt();

                if(expiresAt == null){
                    // ban
                    Platform.runLater(() -> {
                        banButton.setText("Unban");
                        bannedButton.setText("Unban");
                        bannedAtTextField.setText("true");
                    });
                }else{
                    // timeout
                    final Instant now = Instant.now();
                    Instant delta = expiresAt.minusMillis(now.toEpochMilli());
                    Platform.runLater(() -> {
                        timeoutButton.setText("Untimeout");
                        banButton.setText("Ban");
                        bannedButton.setText("Ban");
                        bannedAtTextField.setText(GraphUtils.getTimeAgo(delta.toEpochMilli()));
                        //bannedAtTextField.setText(formatter.format(Date.from(delta)));
                    });
                }
            }else{
                Platform.runLater(() -> {
                    banButton.setText("Ban");
                    bannedButton.setText("Ban");
                    timeoutButton.setText("Timeout");
                    bannedAtTextField.setText("false");
                });
            }
        }).start();
    }


    public void onFollowFromSize(int size) {
        followFromSizeLabel2.setText(String.valueOf(size));
    }

    public void onFollowToSize(int size) {
        followToSizeLabel2.setText(String.valueOf(size));

    }

    public void onClipsSize(int size) {
        clipsSizeLabel2.setText(String.valueOf(size));

    }

    public void stop(){
       // videoPlayer.close();;
    }

}
