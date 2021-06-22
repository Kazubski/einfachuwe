package de.calitobundo.twitch.desktop.event;

import static de.calitobundo.twitch.desktop.api.Context.*;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.ChannelJoinEvent;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.calitobundo.twitch.desktop.config.Configuration;
import de.calitobundo.twitch.desktop.data.PersistData;
import de.calitobundo.twitch.desktop.dto.UserItem;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;


public class EventService {

    private static final Logger logger = Logger.getLogger(EventService.class);

    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

    private String currentChannel = Configuration.getValue(Configuration.CHANNEL_NAME);

    private final EventHandler handler;

    public EventService(EventHandler handler) {
        this.handler = handler;

        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(new ChannelJoinEventHandler());
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(new ChannelLeaveEventHandler());
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(new ChannelMessageEventHandler());

    }

    public void start() {
        twitchClient.getChat().joinChannel(currentChannel);
        logger.info("EventService started.");
    }

    public void switchChannel(String channelName) {
        twitchClient.getChat().leaveChannel(currentChannel);
        currentChannel = channelName;
        twitchClient.getChat().joinChannel(currentChannel);
        handler.joinChannel(channelName);
        logger.info("EventService switch channel to " + currentChannel);
    }

    public void close() {
        twitchClient.getChat().leaveChannel(currentChannel);
        logger.info("EventService closed.");
    }

    public void sendMessage(String message) {
        twitchClient.getChat().sendMessage(currentChannel, message);
        logger.info("sendMessage: " +currentChannel +" "+ message);
    }

    public EventHandler getHandler() {
        return handler;
    }


    /**
     *
     *
     *
     */

    public class ChannelMessageEventHandler {
        @EventSubscriber
        public void onMessage(ChannelMessageEvent event) {
            handler.addChatMessage(event);
            logger.info("message " + event.getUser().getName() + " " + event.getMessage());
        }
    }

    public class ChannelJoinEventHandler {
        @EventSubscriber
        public void onMessage(ChannelJoinEvent event) {

            final boolean ignored = PersistData.ignored(event.getUser().getName());
            handler.joined(event);

            AUDIO_KLACK.play();

            final String formattedTime = timeFormatter.format(Date.from(event.getFiredAt().toInstant()));
            handler.addLogMessage(formattedTime + " " + event.getUser().getName() + " hat den Kanal betreten!");
            logger.info("joined " + event.getUser().getName());
        }
    }


    public class ChannelLeaveEventHandler {
        @EventSubscriber
        public void onMessage(ChannelLeaveEvent event) {

            final boolean ignored = PersistData.ignored(event.getUser().getName());
            handler.leaved(event);

            AUDIO_KLACK.play();

            final String formattedTime = timeFormatter.format(Date.from(event.getFiredAt().toInstant()));
            handler.addLogMessage(formattedTime + " " + event.getUser().getName() + " hat den Kanal verlassen!");
            logger.info("leaved " + event.getUser().getName());
        }
    }



}
