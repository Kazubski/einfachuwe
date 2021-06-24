package de.calitobundo.twitch.desktop.views;

import static de.calitobundo.twitch.desktop.api.Fetch.getChannelBadges;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.tmi.domain.Badge;
import com.github.twitch4j.tmi.domain.BadgeSets;

import de.calitobundo.twitch.desktop.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class ChannelView extends VBox {

    // channel
    private final Button toggleUserListView = new Button("Hide Userlist");

    // test
    private final TextField fontSizeTextField = new TextField();
    private final Button fontSizeButton = new Button("Refresh");
    private final Button testButton = new Button("Test");

    // chat
    private final ChatListView chatListView;
    private final TextField chatTextField = new TextField();
    private final Button chatButton = new Button("Send");
    private final Button clearChatButton = new Button("Clear");
    private final Button byeButton = new Button("Moin");

    //
    // private final TextArea logTextArea = new TextArea();
    // private final Button clearButton = new Button("Clear");
    // private final Button button = new Button("Hide");

    // tab
    //private final TabPane consoleTabPane = new TabPane();
    //private final Tab consoleTab = new Tab("Console");


    //private boolean hideConsole = true;

    public ChannelView(EventHandler handler) {

        setMinWidth(400);
        setPrefWidth(600);

        //consoleTab.setContent(logTextArea);
        //consoleTab.setClosable(false);
        //consoleTabPane.getTabs().add(consoleTab);

        chatListView = new ChatListView(handler);

        final HBox sizeLayout = new HBox();
        sizeLayout.setSpacing(10);
        sizeLayout.getChildren().addAll(fontSizeTextField, fontSizeButton, testButton);
        HBox.setHgrow(fontSizeTextField, Priority.ALWAYS);

        final HBox chatInputs = new HBox();
        chatInputs.setSpacing(10);
        chatInputs.getChildren().addAll(chatTextField, chatButton, clearChatButton, byeButton);
        HBox.setHgrow(chatTextField, Priority.ALWAYS);

        // final HBox logInputs = new HBox();
        // logInputs.setSpacing(10);
        // logInputs.getChildren().addAll(clearButton, button);
        // HBox.setHgrow(clearButton, Priority.ALWAYS);

        // logTextArea.setPrefHeight(240);
        // logTextArea.setEditable(false);

        getChildren().addAll(sizeLayout, chatListView, chatInputs);
        setVgrow(chatListView, Priority.ALWAYS);
        setSpacing(10);

        // clearButton.setOnAction(e -> {
        //     logTextArea.clear();
        // });

        chatButton.setOnAction(e -> {
            final String message = chatTextField.getText();
            handler.getService().sendMessage(message);
            chatTextField.clear();
        });

        chatTextField.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER){
                final String message = chatTextField.getText();
                handler.getService().sendMessage(message);
                chatTextField.clear();
            }
        });

        // button.setOnAction(e -> {
        //     hideConsole = !hideConsole;
        //     if(hideConsole){
        //         button.setText("Hide");
        //         getChildren().add(consoleTabPane);
        //     }else{
        //         button.setText("Show");
        //         getChildren().remove(consoleTabPane);
        //     }
        // });

        byeButton.setOnAction(e -> {
            final String message = "!hu Moin";
            handler.getService().sendMessage(message);
        });

        clearChatButton.setOnAction(e -> {
            chatListView.clear();
        });

        fontSizeButton.setOnAction(e -> {
            int size = Integer.parseInt(fontSizeTextField.getText());
            if(size < 6)
                size = 6;
            if(size > 100)
                size = 100;
            fontSizeTextField.setText(String.valueOf(size));
            chatListView.setSize(size);
        });

        // testButton.setOnAction(e -> {
        //     BadgeSets badges = getChannelBadges("einfachuwe42");
        //     Badge bs = badges.getSubscriberBadgeSet().get().getVersions().get("0");
        //     badges.getBadgesByName().forEach((k,v) -> {
        //         System.out.println(k);
        //     });
        //     System.out.println(bs);
        // });

        toggleUserListView.setOnAction(e -> {
            handler.showUserListView(true);
        });

    }

    public void addLogMessage(String message){
        //logTextArea.appendText(message+"\n");
    }

    public void addChatMessage(ChannelMessageEvent event){
        chatListView.addChatMessage(event);
    }

    public void clear(){
        chatListView.clear();
        //logTextArea.clear();
    }

    public void addChattersTab(String channelName) {

        // final Tab chattersTab = new Tab(channelName);
        // final Chatters chatters = twitchClient.getMessagingInterface().getChatters(channelName).execute();
        // final TextArea textArea = new TextArea();
        // chatters.getAllViewers().forEach(chatter -> {
        //     textArea.appendText(chatter+"\n");
        // });
        // chattersTab.setContent(textArea);
        // chattersTab.setClosable(true);
        // consoleTabPane.getTabs().add(chattersTab);

    }
}
