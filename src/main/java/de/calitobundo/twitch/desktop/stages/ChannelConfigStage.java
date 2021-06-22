package de.calitobundo.twitch.desktop.stages;

import com.github.twitch4j.helix.domain.ChannelSearchResult;

import de.calitobundo.twitch.desktop.ImageUtils;
import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.api.UweColors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChannelConfigStage extends Stage {

    private final TextField channelNameTextField = new TextField();
    private final Button joinChannelButton = new Button("Join Channel");
    private final Button showDetailsButton = new Button("Show Details");

    private final ListView<ChannelSearchResult> resultListView = new ListView<>();
    private static boolean details = false;

    public ChannelConfigStage(Stage owner) {

        setTitle("Channel Config");
        initOwner(owner);
        initModality(Modality.NONE);

        HBox buttons = new HBox();
        buttons.getChildren().addAll(joinChannelButton, showDetailsButton);

        GridPane root = new GridPane();
        root.setPadding(new Insets(10));
        root.setHgap(5);
        root.setVgap(5);

        root.add(channelNameTextField, 0 , 0);
        root.add(buttons, 0, 1);
        root.add(resultListView, 0, 2);

        GridPane.setHgrow(channelNameTextField, Priority.ALWAYS);
        GridPane.setHgrow(joinChannelButton, Priority.ALWAYS);
        GridPane.setHgrow(resultListView, Priority.ALWAYS);
        GridPane.setVgrow(resultListView, Priority.ALWAYS);

        channelNameTextField.setPromptText("einfachuwe42");
        
        showDetailsButton.setOnAction(e -> {
            details = !details;
            showDetailsButton.setText(details ? "Hide Details" : "Show Details");
            resultListView.refresh();
        });

        Scene scene = new Scene(root, 460, 600);

        scene.getStylesheets().add(UweColors.darkStyle);
        setScene(scene);
        centerOnScreen();
        setMinWidth(200);
        setMinHeight(400);
        show();

        resultListView.setCellFactory(listview -> new ChannelConfigCellItem());

        channelNameTextField.setOnKeyTyped(e -> {
            final String query = channelNameTextField.getText();
            if(query.isEmpty() || query.length() < 3)
                return;
            new Thread(() -> {
                List<ChannelSearchResult> result = Context.twitchClient().getHelix()
                        .searchChannels(Context.credential.getAccessToken(), query, 20, null, true)
                        .execute().getResults();
                Platform.runLater(() -> {
                    resultListView.setItems(FXCollections.observableArrayList(result));
                });
            }).start();
        });

        joinChannelButton.setOnAction(e -> {
            ChannelSearchResult channelSearchResult = resultListView.getSelectionModel().getSelectedItem();
            if(channelSearchResult != null){
                final String channelName = channelSearchResult.getDisplayName().toLowerCase();
                Context.switchChannel(channelName);
            }
        });



    }

    //private static final Font font = Font.loadFont("file:resources/Langar-Regular.ttf", 12);
    private static class ChannelConfigCellItem extends ListCell<ChannelSearchResult> {

        private static final Map<String, Image> thumbnails = new HashMap<>();
        private final GridPane layout = new GridPane();
        private final HBox layout2 = new HBox();
        private final ImageView thumbnailImageView = new ImageView();
        private final Label nameLabel = new Label();
        private final Label languageLabel = new Label();
        private final Label titleLabel = new Label();
        private final Label startedAtLabel = new Label();


        public ChannelConfigCellItem() {
            layout.add(nameLabel,0 ,0,2 ,1 );
            layout.add(titleLabel,0 ,1, 2, 1);
            layout.add(languageLabel,0 ,2);
            layout.add(startedAtLabel,1 ,2);
            layout.setHgap(5);
            layout.getStyleClass().add("list-item");

            layout2.getChildren().addAll(thumbnailImageView, layout);
            layout2.setSpacing(10);

            nameLabel.setStyle(Context.cssBlueColor);
            languageLabel.setStyle(Context.cssRedColor);
        }

        @Override
        protected void updateItem(ChannelSearchResult result, boolean empty) {
            super.updateItem(result, empty);
            if(empty) {
                setText(null);
                setGraphic(null);
            }else{
                if(details){

                    final String id = result.getDisplayName();
                   
                    if(thumbnails.containsKey(id)){
                        final Image image = thumbnails.get(id);
                        thumbnailImageView.setImage(image);

                    }else{

                        thumbnailImageView.setImage(null);

                        new Thread(() -> {

                            final Image image = ImageUtils.fetchImage(result.getThumbnailUrl(), 96, 96);
                            thumbnails.put(id, image);

                            Platform.runLater(() -> {
                                thumbnailImageView.setImage(image);
                            });

                        }).start();

                    }

                    //String tagString = result.getTagsIds() == null ? "no tags" : String.join(" ", result.getTagsIds());
                    nameLabel.setText(result.getDisplayName());
                    languageLabel.setText(result.getBroadcasterLanguage().toUpperCase());

                    String resultString = result.getTitle().replaceAll("[^\\x00-\\x7F]", "");
                    titleLabel.setText(resultString);

                    if(result.getStartedAt() == null){
                        startedAtLabel.setText("0");

                    }else{
                        startedAtLabel.setText(DateFormat.getInstance().format(Date.from(result.getStartedAt())));
                    }


                    setText(null);
                    setGraphic(layout2);
                }else{
                    setText(result.getDisplayName());
                    setGraphic(null);
                    
                }



            }
        }
    }
}
