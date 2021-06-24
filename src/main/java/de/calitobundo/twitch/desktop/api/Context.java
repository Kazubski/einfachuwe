package de.calitobundo.twitch.desktop.api;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.kraken.domain.SimpleEmoticon;

import de.calitobundo.twitch.desktop.audio.AudioFile;
import de.calitobundo.twitch.desktop.config.Configuration;
import de.calitobundo.twitch.desktop.data.PersistData;
import de.calitobundo.twitch.desktop.event.EventHandler;
import de.calitobundo.twitch.desktop.event.EventService;
import de.calitobundo.twitch.desktop.views.ChatListView;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Context {

    private static final Logger logger = Logger.getLogger(Context.class);

    public static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static EventService service;

    public static final Image liveImage;
    public static final Image notLiveImage;
    public static final Image neverLiveImage;

    public static final Font font;

    public static final AudioFile AUDIO_KLACK;
    public static final AudioFile AUDIO_JOIN_CHANNEL;
    public static final AudioFile AUDIO_LEAVE_CHANNEL;

    public static final String cssRedColor = "-fx-text-fill: rgba(252, 122, 122, 255);";
    public static final String cssWhiteColor = "-fx-text-fill: rgba(255, 255, 255, 255);";
    public static final String cssBlueColor = "-fx-text-fill: rgba(124, 122, 255, 255);";
    public static final String cssGrayColor = "-fx-text-fill: rgba(124, 122, 122, 255);";
    public static final String cssLightGrayColor = "-fx-text-fill: rgba(201, 186, 186, 255);";
    public static final String cssYellowColor = "-fx-text-fill: rgba(252, 228, 122, 255);";
    public static final String cssGreenColor = "-fx-text-fill: rgba(122, 252, 133, 255);";

    //public static final String regexLabel2 = "^[\\u0000-\\u007F]*$";
    //public static final String regexLabel = "[^\\x00-\\x7F]"; //"[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\s]"

    public static final String regexLabel = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\s]";
    public static final String regexLabelA = "[^\\x00-\\x7F]";

    static{

        AUDIO_KLACK = new AudioFile("klack.tmp", Objects.requireNonNull(Context.class.getClassLoader().getResourceAsStream("sounds/lotto_ball2.wav")));
        AUDIO_LEAVE_CHANNEL = new AudioFile("husten_tief.tmp", Objects.requireNonNull(Context.class.getClassLoader().getResourceAsStream("sounds/husten_tief.wav")));
        AUDIO_JOIN_CHANNEL = new AudioFile("husten_hoch.tmp", Objects.requireNonNull(Context.class.getClassLoader().getResourceAsStream("sounds/husten_hoch.wav")));

        font = Font.loadFont(Context.class.getClassLoader().getResourceAsStream("fonts/OpenSansEmoji.ttf"), 16);

        liveImage = new Image(Objects.requireNonNull(Context.class.getClassLoader().getResourceAsStream("images/kartoffel_36.png")));
        notLiveImage = new Image(Objects.requireNonNull(Context.class.getClassLoader().getResourceAsStream("images/kartoffel_36_dark.png")));
        neverLiveImage = new Image(Objects.requireNonNull(Context.class.getClassLoader().getResourceAsStream("images/kartoffel_36_darker.png")));

    }

    public static List<SimpleEmoticon> emoticons = new ArrayList<>();
    public static OAuth2Credential credential;
    public static TwitchClient twitchClient;


    private Context(){}

    public static String home_channel, current_channel;
    private static User channelUser = null;
    private static User homeUser = null;

    public static User setChannelUser(String channelUserName){
        current_channel = channelUserName;
        return channelUser = Fetch.fetchUserByName(channelUserName);
    }
    public static User getChannelUser(){
        if(channelUser == null){
            return channelUser = Fetch.fetchUserByName(current_channel);
        }else{
            return channelUser;
        }
    }

    public static User setHomeUser(String homeUserName){
        home_channel = homeUserName;
        return homeUser = Fetch.fetchUserByName(homeUserName);
    }
    public static User getHomeUser(){
        if(homeUser == null){
            return homeUser = Fetch.fetchUserByName(home_channel);
        }else{
            return homeUser;
        }
    }


    // emotes
    public static void loadEmoticons(){
        emoticons = twitchClient.getKraken().getChatEmoticons().execute().getEmoticons();
    }

    // init
    public static void init(EventHandler handler){

        PersistData.init();

        home_channel = Configuration.getValue(Configuration.CHANNEL_NAME);
        current_channel = home_channel;

        credential = new OAuth2Credential(Configuration.IDENTITY_PROVIDER, Configuration.getValue(Configuration.O_AUTH));

        twitchClient = TwitchClientBuilder.builder()
                .withClientId(Configuration.getValue(Configuration.CLIENT_ID))
                .withClientSecret(Configuration.getValue(Configuration.CLIENT_SECRET))
                .withEnableKraken(true)
                .withEnableHelix(true)
                .withEnableChat(true)
                .withEnableTMI(true)
                .withChatAccount(credential)
                .build();

        ChatListView.badgesSet = Fetch.getChannelBadges(current_channel);
            // badgesSet.getBadgesByName().forEach((k,v) -> { 
            //     System.out.println(k+" / "+v);
            //     v.getVersions().forEach((k2,v2) -> {
            //         System.out.println(k2+" / "+v2.getTitle());
            //     });
            // });

        service = new EventService(handler);
        service.start();
        logger.info("Context init.");


        //System.out.println("emoteSize: "+emoticons.size());

        // User user = Fetch.fetchUserByName("einfachuwe42");
        // KrakenEmoticonSetList emoticonSetList = twitchClient.getKraken().getUserEmotes(credential.getAccessToken(), user.getId()).execute();

        // emoticonSetList.getEmoticonSets().entrySet().forEach(e -> {

        //     e.getValue().forEach(s -> {
        //         System.out.println(e.getKey()+" "+s.getCode()+" "+s.getId());
        //     });

        // });

        // for (SimpleEmoticon se : emoticons) {
        //     System.out.println(se.getId()+" "+se.getCode()+" "+se.getEmoticonSet());
        // }        
    }
    

    public static void close() {

        try {

            service.close();
            if(twitchClient != null)
                twitchClient.close();

        }catch (Exception e){
            logger.info("close" + e.toString());
        }
        logger.info("Context closed.");
    }

    public static TwitchClient twitchClient(){
        return twitchClient;
    }


    // public static void saveStreamInfo(StreamInfoData streamInfoData){
    //     try {
    //         String jsonString = mapper.writeValueAsString(streamInfoData);
    //         File dest = new File("stream_info.json");
    //         Files.writeString(dest.toPath(), jsonString, StandardCharsets.UTF_8);
    //         System.out.println("Datei stream_info.json geschrieben!");
    //     } catch (Exception e) {
    //         logger.warn(e.getMessage());
    //     }
    // }

    // public static StreamInfoData loadStreamInfo () {
    //     StreamInfoData streamInfoData = null;
    //     try {
    //         File dest = new File("stream_info.json");
    //         if(!dest.exists())
    //             saveStreamInfo(new StreamInfoData());
    //         streamInfoData = mapper.readValue(dest, StreamInfoData.class);
    //     } catch (IOException e) {
    //         logger.warn(e.getMessage());
    //     }
    //     return streamInfoData;
    // }


    // public static void addToIgnoredUserAndSave(String name) {
        
    //     List<String> ignoredUsers = ignoredUserData.getList();

    //     if(ignoredUsers.contains(name)){
    //         System.out.println(name+" ist bereits in der ignoreliste!");
    //     }else{
    //         ignoredUsers.add(name);
    //         //IgnoredUserData ignoredUserData = new IgnoredUserData(ignoredUsers);
    //         PersistData.saveJsonObject(ignoredUserData, IgnoredUserData.class);
    //         System.out.println(name+" in die ignoreliste hinzugefügt!");
    //     }
    // }

    // public static void removeFromIgnoredUserAndSave(String name) {

    //     List<String> ignoredUsers = ignoredUserData.getList();

    //     if(ignoredUsers.remove(name)) {
    //         System.out.println(name + " aus der ignoreliste entfernt!");
    //         //IgnoredUserData ignoredUserData = new IgnoredUserData(ignoredUsers);
    //         PersistData.saveJsonObject(ignoredUserData, IgnoredUserData.class);
    //     }else {
    //         System.out.println(name + " nicht in der ignoreliste gefunden!");
    //     }
    // }




    // public static List<String> getIgnoredUsers() {
    //     return ignoredUserData.getList();
    // }


    public static void switchChannel(String name) {
        service.switchChannel(name);
    }

    public static EventService getService() {
        return service;
    }
}