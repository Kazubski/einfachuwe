package de.calitobundo.twitch.desktop.api;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.*;
import com.github.twitch4j.kraken.domain.EmoticonList;
import com.github.twitch4j.tmi.domain.BadgeSets;
import com.github.twitch4j.tmi.domain.Chatters;

import de.calitobundo.twitch.desktop.dto.UserItem;
import de.calitobundo.twitch.desktop.graph.ProgressEvent;
import de.calitobundo.twitch.desktop.views.UserFollowListView.FetchEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.calitobundo.twitch.desktop.api.Context.credential;
import static de.calitobundo.twitch.desktop.api.Context.twitchClient;

public class Fetch {

    public static List<Stream> fetchStreamInfo(List<String> channelNames){
        StreamList list = twitchClient.getHelix()
                .getStreams(credential.getAccessToken(), null, null, null, null, null, null, channelNames).execute();
        return list.getStreams();
//        if(list.getStreams()!=null && !list.getStreams().isEmpty()) {
//            return list.getStreams();
//        }
//        return null;
    }

    public static Optional<Stream> fetchStreamInfoOptional(String channelName){
        return twitchClient.getHelix()
                .getStreams(credential.getAccessToken(), null, null, null, null, null, null, Collections.singletonList(channelName))
                .execute()
                .getStreams()
                .stream().findFirst();


        // if(list.getStreams()!=null && !list.getStreams().isEmpty()) {
        //     return list.getStreams().get(0);
        // }
        // return null;
    }

    public static Stream fetchStreamInfo(String channelName){
        StreamList list = twitchClient.getHelix()
                .getStreams(credential.getAccessToken(), null, null, null, null, null, null, Collections.singletonList(channelName)).execute();
        if(list.getStreams()!=null && !list.getStreams().isEmpty()) {
            return list.getStreams().get(0);
        }
        return null;
    }

    public static User fetchUserById(String id){
        final UserList result = twitchClient.getHelix().getUsers(credential.getAccessToken(), Collections.singletonList(id), null).execute();
        if (result.getUsers() != null && !result.getUsers().isEmpty()){
            return result.getUsers().get(0);
        }
        return null;
    }

    public static User fetchUserByName(String name){
        final UserList result = twitchClient.getHelix().getUsers(credential.getAccessToken(), null, Collections.singletonList(name)).execute();
        if (result.getUsers() != null && !result.getUsers().isEmpty()){
            return result.getUsers().get(0);
        }
        return null;
    }

    public static Follow fetchFollowByIds(String fromId, String toId){
        final FollowList result = twitchClient.getHelix().getFollowers(credential.getAccessToken(), fromId, toId, null, null).execute();
        if (result.getFollows() != null && !result.getFollows().isEmpty()){
            return result.getFollows().get(0);
        }
        return null;
    }

    public static List<StreamTag> fetchTagsByUserId(String id){
        final StreamTagList result = twitchClient.getHelix().getStreamTags(credential.getAccessToken(), id).execute();
        if (result.getStreamTags() != null && !result.getStreamTags().isEmpty()){
            return result.getStreamTags();
        }
        return Collections.emptyList();
    }

    public static List<StreamTag> fetchAllStreamTags(String cursor, List<StreamTag> list){
        StreamTagList result = twitchClient.getHelix().getAllStreamTags(credential.getAccessToken(), cursor, 100, null).execute();
        list.addAll(result.getStreamTags());
        if(result.getPagination().getCursor() != null)
            fetchAllStreamTags(result.getPagination().getCursor(), list);
        return list;
    }

    public static List<Follow> follows(String cursor, List<Follow> list, String fromUser, TwitchClient twitchClient, OAuth2Credential credential){
        FollowList follows = twitchClient.getHelix().getFollowers(credential.getAccessToken(), fromUser, null, cursor, 100).execute();
        list.addAll(follows.getFollows());
        if(follows.getPagination().getCursor() != null)
            follows(follows.getPagination().getCursor(), list, fromUser, twitchClient, credential);
        return list;
    }

    public static List<Follow> followersFromUserId(FetchEvent event, String cursor, List<Follow> list, String fromUser, TwitchClient twitchClient, OAuth2Credential credential){
        FollowList follows = twitchClient.getHelix().getFollowers(credential.getAccessToken(), fromUser, null, cursor, 100).execute();
        list.addAll(follows.getFollows());
        if(event != null)
            event.onFetch(list.size(), follows.getTotal());
        if(follows.getPagination().getCursor() != null)
            followersFromUserId(event, follows.getPagination().getCursor(), list, fromUser, twitchClient, credential);
        return list;
    }
    public static List<Follow> followersFromUserId2(ProgressEvent event, String cursor, List<Follow> list, String fromUser, TwitchClient twitchClient, OAuth2Credential credential){
        FollowList follows = twitchClient.getHelix().getFollowers(credential.getAccessToken(), fromUser, null, cursor, 100).execute();
        list.addAll(follows.getFollows());
        if(event != null)
            event.onProgress(list.size(), follows.getTotal());
        if(follows.getPagination().getCursor() != null)
            followersFromUserId2(event, follows.getPagination().getCursor(), list, fromUser, twitchClient, credential);
        return list;
    }

    public static List<Follow> followersToUserId(FetchEvent event, String cursor, List<Follow> list, String toUser, TwitchClient twitchClient, OAuth2Credential credential){
        FollowList follows = twitchClient.getHelix().getFollowers(credential.getAccessToken(), null, toUser, cursor, 100).execute();
        list.addAll(follows.getFollows());
        if(event != null)
            event.onFetch(list.size(), follows.getTotal());
        if(follows.getPagination().getCursor() != null)
            followersToUserId(event, follows.getPagination().getCursor(), list, toUser, twitchClient, credential);
        return list;
    }

    public static List<Clip> fetchClipsByUserId(String cursor, List<Clip> list, String broadcasterId){
        ClipList clips = twitchClient.getHelix().getClips(credential.getAccessToken(), broadcasterId, null, null, cursor, null, 100, null, null).execute();
        list.addAll(clips.getData());
        if(clips.getPagination().getCursor() != null)
            fetchClipsByUserId(clips.getPagination().getCursor(), list, broadcasterId);
        return list;
    }

    public static ChannelInformation fetchChannelInformationByUserId(String broadcasterId) {
        ChannelInformationList ci = twitchClient.getHelix().getChannelInformation(credential.getAccessToken(), Collections.singletonList(broadcasterId)).execute();
        if (ci.getChannels() != null && !ci.getChannels().isEmpty()){
            return ci.getChannels().get(0);
        }
        return null;
    }

    public static List<Stream> fetchAllStreamsByUserIds(String cursor, List<Stream> streams, List<String> userIds){

        StreamList streamList = Context.twitchClient.getHelix().getStreams(Context.credential.getAccessToken(), null, null, 100, null, null, userIds, null).execute();
        streams.addAll(streamList.getStreams());
        if(streamList.getPagination().getCursor() != null)
            fetchAllStreamsByUserIds(streamList.getPagination().getCursor(), streams, userIds);
    
        return streams;
    
    
    }

    public static List<BlockedUser> blockedUsers(ProgressEvent event, String cursor, List<BlockedUser> list, String userId, TwitchClient twitchClient, OAuth2Credential credential){
        BlockedUserList result = twitchClient.getHelix().getUserBlockList(credential.getAccessToken(), userId, cursor, 100).execute();
        list.addAll(result.getBlocks());
        if(event != null)
            event.onProgress(list.size(), 0);
        if(result.getPagination().getCursor() != null)
            blockedUsers(event, result.getPagination().getCursor(), list, userId, twitchClient, credential);
        return list;
    }


    public static List<Stream> fetchAllStreamsFromFollowByUserName(FetchEvent event, String cursor, List<Stream> streams, User fromUser){

        FollowList follows = twitchClient.getHelix().getFollowers(credential.getAccessToken(), fromUser.getId(), null, cursor, 100).execute();
        final List<String> userIds = follows.getFollows().stream().map(follow -> follow.getToId()).collect(Collectors.toList());
        streams.addAll(fetchAllStreamsByUserIds(null, new ArrayList<>(), userIds));

        if(event != null)
            event.onFetch(streams.size(), follows.getTotal());

        if(follows.getPagination().getCursor() != null)
            fetchAllStreamsFromFollowByUserName(event, follows.getPagination().getCursor(), streams, fromUser);
        
        return streams;
    }





    public static List<UserItem> getChatters(String channelName, List<String> ignored){

        final Chatters chatters = twitchClient.getMessagingInterface().getChatters(channelName).execute();
        if(chatters == null)
            System.out.println("getChatters(String channelName, List<String> ignored) null");
        final List<String> allViewers = chatters.getAllViewers();
        final List<UserItem> items = new ArrayList<>();
              
        allViewers.forEach(userName -> {
            final UserItem item = new UserItem(userName, ignored.contains(userName));
            items.add(item);
        });
        return items;
    }

    public static BadgeSets getChannelBadges(String channelName){

        User user = fetchUserByName(channelName);
        BadgeSets badgeSets = Context.twitchClient.getMessagingInterface().getChannelBadges(user.getId(), "de").execute();

       return badgeSets;

    }

    public static EmoticonList getAllChatEmoticons(){

       return Context.twitchClient.getKraken().getAllChatEmoticons().execute();

    }


    public static void getBannedUserById(String userId){
        final String broadcasterId = Context.getChannelUser().getId();
        final BannedUserList bannedUsersList = twitchClient.getHelix().getBannedUsers(credential.getAccessToken(), broadcasterId, Collections.singletonList(userId), null, null).execute();

    }
    public static List<BannedUser> getBannedUserByIds(List<String> userIds){
        final String broadcasterId = Context.getChannelUser().getId();
        final BannedUserList bannedUsersList = twitchClient.getHelix().getBannedUsers(credential.getAccessToken(), broadcasterId, userIds, null, null).execute();
        return bannedUsersList.getResults();
    }
}
