package de.calitobundo.twitch.desktop.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.github.twitch4j.helix.domain.User;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ChannelHistoryView extends VBox {


    private final TextArea textArea = new TextArea();
    private final TextField searchTextField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Button clearButton = new Button("Clear");


    public ChannelHistoryView() {

        final HBox inputLayout = new HBox(searchTextField, searchButton, clearButton);
        inputLayout.setSpacing(10);
        HBox.setHgrow(searchTextField, Priority.ALWAYS);

        textArea.setPrefHeight(240);
        textArea.setEditable(false);

        getChildren().addAll(inputLayout, textArea);
        setVgrow(textArea, Priority.ALWAYS);
        setSpacing(10);

        clearButton.setOnAction(e -> {
            textArea.clear();
        });

        searchButton.setOnAction(e -> {
            final String name = searchTextField.getText();
            if(name.isBlank() || name.length() < 3)
                return;
            textArea.clear();
            List<ChatHistoryItem> list = getChatHistory(name).stream().filter(item -> item.type.equals("message")).collect(Collectors.toList());
            if(list.isEmpty()){
                textArea.appendText("no history for "+name+"\n");
            }else{
                list.forEach(item -> {
                    final String text = item.date+" "+item.name+" "+item.message;
                    textArea.appendText(text+"\n");
                });
            }
        });
    
    }

    public void setUser(User user){
        searchTextField.setText(user.getLogin());
        textArea.clear();
    }


    // public static void main(String[] args) {
        

    //     List<ChatHistoryItem> list = getChatHistory("kaiser_jumbi");
    //     list.forEach(System.out::println);
    //     System.out.println("ChatHistoryItems count: "+list.size());
    //     final long joinCount = list.stream().filter(item -> item.type.equals("joined")).count();
    //     final long leaveCount = list.stream().filter(item -> item.type.equals("leaved")).count();
    //     final long messageCount = list.stream().filter(item -> item.type.equals("message")).count();
    //     System.out.println("ChatHistoryItems joinCount: "+joinCount);
    //     System.out.println("ChatHistoryItems leaveCount: "+leaveCount);
    //     System.out.println("ChatHistoryItems messageCount: "+messageCount);

    // }

    //2021-05-24 19:18:27 INFO  [de.calitobundo.twitch.desktop.event.EventService] message einfachuwe42 !h einfac102Herz
    public static List<ChatHistoryItem> getChatHistory(String name){

        final List<ChatHistoryItem> items = new ArrayList<>();

        try {

            final File file = new File("uwe-twitch-desktop.log");
            final Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                final String line = scanner.nextLine();
                if(line.contains(name)){ 

                    //date
                    final String date = line.substring(0, 19);

                    //event
                    final String eventIndexString = "EventService] ";
                    final int startIndex = line.indexOf(eventIndexString);
                    final int startIndex2 = startIndex + eventIndexString.length();
                    final int index3 = line.indexOf(" ", startIndex2+1);
                    final String type = line.substring(startIndex2, index3);
                    
                    //name
                    final int startIndex3 = index3+1;
                    final int index4 = line.indexOf(" ", startIndex3);
                    String name2;
                    if(index4 != -1){
                        name2 = line.substring(startIndex3, index4);
                    }else{
                        name2 = line.substring(startIndex3);
                    }
                    String message = "";
                    if(type.equals("message")){
                        message = line.substring(index4+1);
                    }
                    ChatHistoryItem item = new ChatHistoryItem(date, name2, type, message);
                    items.add(item);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return items;
    }


    public static class ChatHistoryItem {

        final public String date;
        final public String name;
        final public String type;
        final public String message;

        public ChatHistoryItem(String date, String name, String type, String message){
            this.date = date;
            this.name = name;
            this.type = type;
            this.message = message;
        }

        public enum ChatEventType {
            JOIN, LEAVE, MESSAGE
        }

        @Override
        public String toString() {
            return date+" "+type+" "+name+" "+(message.length() > 20 ? message.substring(0, 20) : message);
        }
    }




}
