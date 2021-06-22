package de.calitobundo.twitch.desktop.dto;

import java.util.ArrayList;
import java.util.List;

import com.github.twitch4j.tmi.domain.Badge;

import de.calitobundo.twitch.desktop.api.UweColors;
import de.calitobundo.twitch.desktop.chat.ChatElement;
import de.calitobundo.twitch.desktop.chat.ChatEmotePosition;
import de.calitobundo.twitch.desktop.chat.ChatItem;
import de.calitobundo.twitch.desktop.views.ChatListView;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatItemCell {

    public static int size = 16;
    public final ChatItem item;

    public final List<Node> nodes = new ArrayList<>();
    public final TextFlow textFlow = new TextFlow();

    private final Text dateText = new Text();
    private final Text nameText = new Text();

    private final Badge subscriberBadge; 
    private final Badge bitsBadge; 

    public ChatItemCell(ChatItem item) {
        this.item = item;

        subscriberBadge = ChatListView.badgesSet.getSubscriberBadgeSet().get().getVersions().get(item.badges.get("subscriber"));
        bitsBadge = ChatListView.badgesSet.getBadgesByName().get("bits").getVersions().get(item.badges.get("bits"));

        dateText.setText(item.date);
        nameText.setText(item.name);
        textFlow.setPrefWidth(300);
        render();
    }

    public void resize(int size){
        ChatItemCell.size = size;
        render();
    }

    private void render(){

        nodes.clear();
        textFlow.getChildren().clear();

        if(item == null)
            return;

        dateText.setStyle("-fx-fill: "+UweColors.hexColorDarkGray+"; -fx-font-size: "+size+"px;");
        nameText.setStyle("-fx-fill: " + ((item.name.equals("chickenmax89") ? "#ff9cf0" : item.color) + "; -fx-font-size: "+size+"px;"));

        textFlow.getChildren().addAll(dateText, new Text(" "));

        if(subscriberBadge != null){
            final ImageView subscriberBadgeImageView = new ImageView(new Image(subscriberBadge.getImageUrl2x(), size, size, false, false));
            subscriberBadgeImageView.setStyle("-fx-translate-y: "+(int)(size/6)+"px;");
            textFlow.getChildren().addAll(subscriberBadgeImageView, new Text(" "));
        }

        if(bitsBadge != null){
            final ImageView bitsBadgeImageView = new ImageView(new Image(bitsBadge.getImageUrl2x(), size, size, false, false));
            bitsBadgeImageView.setStyle("-fx-translate-y: "+(int)(size/6)+"px;");
            textFlow.getChildren().addAll(bitsBadgeImageView, new Text(" "));
        }

        textFlow.getChildren().addAll(nameText, new Text(" "));

        for (ChatElement el : item.elements) {
            final ChatEmotePosition cep = getPos(item.positions, el.start);
            if (cep == null) {
                final Text text = new Text(el.word);
                text.setStyle("-fx-fill: "+UweColors.hexColorLightGray+"; -fx-font-size: " + size + "px;");
                
                nodes.add(text);
            } else {

                final Label root = new Label();
                final ImageView emoteImageView = new ImageView(cep.emote.getImage(size));
                final Tooltip tooltip = new Tooltip();
                final ImageView toolTipImageView = new ImageView(cep.emote.getImage(300, "3.0"));
                final Label toolTipLabel = new Label(cep.emote.id+" x:"+emoteImageView.getImage().getWidth()+" y:"+emoteImageView.getImage().getHeight());
                final VBox toolTipLayout = new VBox(toolTipImageView, toolTipLabel);

                tooltip.setGraphic(toolTipLayout);
                root.setGraphic(emoteImageView);
                root.setTooltip(tooltip);
                nodes.add(root);
            }
            nodes.add(new Text(" "));
        }
        textFlow.getChildren().addAll(nodes);
    }


    private static ChatEmotePosition getPos(List<ChatEmotePosition> positions, int start) {

        for (ChatEmotePosition position : positions) {
            if (position.start == start)
                return position;
        }
        return null;
    }

}
