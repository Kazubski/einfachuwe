package de.calitobundo.twitch.desktop.views;

import com.github.twitch4j.helix.domain.Clip;
import com.github.twitch4j.helix.domain.User;

import org.apache.commons.io.FileUtils;

import static de.calitobundo.twitch.desktop.api.Fetch.*;
import static de.calitobundo.twitch.desktop.api.Context.*;

import de.calitobundo.twitch.desktop.ImageUtils;
import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.event.EventHandler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public class UserClipsInfoView extends VBox {

    private final ObservableList<Clip> clipObservableList = FXCollections.observableArrayList();
    private final ListView<Clip> clipListView = new ListView<>(clipObservableList);
    private final TextArea clipInfoTextArea = new TextArea();

    private final Button loadClipsButton = new Button("LoadClips");

    private final Button sortByTimeButton = new Button("SortTime");
    private final Button sortByViewsButton = new Button("SortViews");

    private final Button downloadClipsButton = new Button("downloadClip");
    private final Label clipsCountLabel = new Label("clips:");
    private final Label clipsCountLabel2 = new Label("0");

    private final EventHandler handler;

    public UserClipsInfoView(EventHandler handler) {
        this.handler = handler;

        clipListView.setCellFactory(listView -> new ClipListCell());

        final HBox buttons = new HBox(loadClipsButton, sortByTimeButton, sortByViewsButton, downloadClipsButton, clipsCountLabel, clipsCountLabel2);

        setPadding(new Insets(10,0,0,0));
        getChildren().addAll(buttons, clipListView, clipInfoTextArea);
        VBox.setVgrow(clipListView, Priority.ALWAYS);
        VBox.setVgrow(clipInfoTextArea, Priority.ALWAYS);
        setSpacing(10);

        clipInfoTextArea.setPrefHeight(280);

        clipsCountLabel2.textProperty().bind(Bindings.size(clipObservableList).asString());

        clipListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Clip>() {
            @Override
            public void changed(ObservableValue<? extends Clip> observable, Clip oldValue, Clip newValue) {

                clipInfoTextArea.clear();

                if(newValue == null)
                    return;

                final String id = newValue.getId();
                final String broadcasterId = newValue.getBroadcasterId();
                final String creatorId = newValue.getCreatorId();
                final String videoId = newValue.getVideoId();
                final String gameId = newValue.getGameId();
                final String url = newValue.getUrl();
                final String embedUrl = newValue.getEmbedUrl();
                final String thumbnailUrl = newValue.getThumbnailUrl();
                final String title = newValue.getTitle();
                final String viewCount = String.valueOf(newValue.getViewCount());
                final String createdAt = String.valueOf(newValue.getCreatedAtInstant().toEpochMilli());
                final String language = newValue.getLanguage();

                clipInfoTextArea.appendText("id: "+id.concat("\n"));
                clipInfoTextArea.appendText("broadcasterId: "+broadcasterId.concat("\n"));
                clipInfoTextArea.appendText("creatorId: "+creatorId.concat("\n"));
                clipInfoTextArea.appendText("videoId: "+videoId.concat("\n"));
                clipInfoTextArea.appendText("gameId: "+gameId.concat("\n"));
                clipInfoTextArea.appendText("url: "+url.concat("\n"));
                clipInfoTextArea.appendText("embedUrl: "+embedUrl.concat("\n"));
                clipInfoTextArea.appendText("thumbnailUrl: "+thumbnailUrl.concat("\n"));
                clipInfoTextArea.appendText("title: "+title.concat("\n"));
                clipInfoTextArea.appendText("viewCount: "+viewCount.concat("\n"));
                clipInfoTextArea.appendText("createdAt: "+createdAt.concat("\n"));
                clipInfoTextArea.appendText("language: "+language.concat("\n"));

            }
        });


        downloadClipsButton.setOnAction(e -> {

//             int i = 0;
//             for(Clip clip : clipObservableList){
//                 i++;

//                 final String number = String.format("%04d", i);
//                 final String url = clip.getThumbnailUrl();
//                 final String downloadUrl = url.substring(0, url.indexOf("-preview")).concat(".mp4");
//                 try {

//                     final URL URL = new URL(downloadUrl);
//                     final File file = new File("/home/uwe/Downloads/clips3/"+number+"-"+clip.getTitle()+".mp4");
//                     FileUtils.copyURLToFile(URL, file);

//                 } catch (MalformedURLException malformedURLException) {
//                     malformedURLException.printStackTrace();
//                 } catch (IOException e2) {
//                     e2.printStackTrace();
//                 }

// //                if(i == 2)
// //                    break;
//             }


        });

        sortByTimeButton.setOnAction(e -> {

          
            Comparator<Clip> timeComparator = Comparator.comparingLong(new ToLongFunction<Clip>(){

                @Override
                public long applyAsLong(Clip value) {
                    return value.getCreatedAtInstant().toEpochMilli();
                }
                
            }).reversed();


            FXCollections.sort(clipObservableList, timeComparator);
        });


        sortByViewsButton.setOnAction(e -> {

            Comparator<Clip> viewCountComparator = Comparator.comparingInt(new ToIntFunction<Clip>(){

                @Override
                public int applyAsInt(Clip value) {
                    return value.getViewCount();
                }
                
            }).reversed();

            FXCollections.sort(clipObservableList, viewCountComparator);
        });

        loadClipsButton.setOnAction(e -> {
            loadClips2(channelName);
        });
    }

    private String channelName = "einfachuwe42";
    private List<Clip> clipsList = new ArrayList<>();

    public void loadClips(User user) {
        this.channelName = user.getLogin();
        clipInfoTextArea.clear();
        clipObservableList.clear();
        new Thread(() -> {

            clipsList = fetchClipsByUserId(null, new ArrayList<>(), user.getId());
            Platform.runLater(() -> {
                clipInfoTextArea.appendText("clips: "+clipsList.size()+"\n");
                handler.onClipsSize(clipsList.size());
            });

        }).start();
    }

    public void loadClips2(String channelName) {

        new Thread(() -> {

            // final User broadcaster = fetchUserByName(channelName);
            // if(broadcaster == null)
            //     return;

            // final List<Clip> clips = fetchClipsByUserId(null, new ArrayList<>(), broadcaster.getId());
            // Platform.runLater(() -> {
            //     clipInfoTextArea.appendText("clips: "+clips.size()+"\n");
            // });

            // clipsList.forEach(clip -> {
            //     if(!ClipListCell.images.containsKey(clip.getId())){
            //         Platform.runLater(() -> {
            //             clipInfoTextArea.appendText("lade: "+clip.getThumbnailUrl()+"\n");
            //         });
            //         final Image fetchedImage = ImageUtils.fetchImage(clip.getThumbnailUrl(), 103, 58);
            //         ClipListCell.images.put(clip.getId(), fetchedImage);
            //     }
            // });

            Platform.runLater(() -> {

                // List<Clip> clips3 = clips.stream()
                // .sorted(Comparator.comparingLong(c -> c.getCreatedAtInstant().toEpochMilli()).reversed())
                // .collect(Collectors.toList());

                List<Clip> clips2 = clipsList.stream()
                    .sorted(Comparator.comparingLong(new ToLongFunction<Clip>(){

                        @Override
                        public long applyAsLong(Clip value) {
                            return value.getCreatedAtInstant().toEpochMilli();
                        }
                        
                    }).reversed())
                    .collect(Collectors.toList());

                clipObservableList.setAll(clips2);

            });
        }).start();

        //twitchClient.getHelix().getVideos(credential.getAccessToken(),null,null,null,null,null,null,null,null,null,null).execute();

    }

    private static class ClipListCell extends ListCell<Clip> {

        private static final Map<String, Image> images = new HashMap<>();
        private static final Map<String, String> creatorNames = new HashMap<>();

        private final ImageView imageView = new ImageView();
        private final Label title = new Label();
        private final Label viewCount = new Label();
        private final Label creator = new Label();
        private final Label date = new Label();
        private final HBox box1 = new HBox(title);
        private final HBox box2 = new HBox(viewCount, creator, date);
        private final VBox box3 = new VBox(box1, box2);
        private final HBox root = new HBox(imageView, box3);

        public ClipListCell() {
            box1.setSpacing(10);
            box2.setSpacing(10);
            box3.setSpacing(10);
            title.setFont(font);
            viewCount.setStyle(Context.cssRedColor);
        }

        @Override
        protected void updateItem(Clip clip, boolean empty) {
            super.updateItem(clip, empty);
            if(empty){
                setText(null);
                setGraphic(null);
            }else{
                setText(null);

                //String creatorName = creatorNames.get(clip.getCreatorId());
                //creator.setText(clip.getCreatorId());

                //if(creatorName == null){
                    // new Thread(() -> {
                    //     final User user = fetchUserById(clip.getCreatorId());
                    //     if(user != null){
                    //         Platform.runLater(() -> {
                    //             creatorNames.put(clip.getCreatorId(), user.getDisplayName());
                    //             creator.setText(user.getDisplayName());
                    //         });
                    //     }
                    // }).start();
                //}else{
                   // creator.setText(creatorName);
                //}

                imageView.setImage(images.get(clip.getId()));

                // if(chachedImage == null) {
                //     imageView.setImage(null);
                //     new Thread(() -> {

                //         //final Image fetchedImage = ImageUtils.fetchImage(clip.getThumbnailUrl(), 103, 58);

                //         //final Image fetchedImage = new Image(clip.getThumbnailUrl(), 103, 58, false, false);
                //         //images.put(clip.getId(), fetchedImage);
                //         Platform.runLater(() -> {
                //             imageView.setImage(chachedImage);
                //         });
                //     }).start();
                // }else{
                //     imageView.setImage(chachedImage);
                // }
                title.setText(clip.getTitle());
                viewCount.setText(String.valueOf(clip.getViewCount()));
                date.setText(dateTimeFormatter.format(Date.from(clip.getCreatedAtInstant())));
                setGraphic(root);
            }
        }
    }

}
