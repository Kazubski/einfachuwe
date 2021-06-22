package de.calitobundo.twitch.desktop.api;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.twitch4j.helix.domain.Follow;
import com.github.twitch4j.helix.domain.FollowList;

import de.calitobundo.twitch.desktop.dto.FollowItem;
import de.calitobundo.twitch.desktop.graph.GraphUser;


public class FollowUtils {
    

    public static Set<String> getFollowCompareResult(List<Follow> userFollows1, List<Follow> userFollows2){


        // if(name1.getText().isBlank() || name2.getText().isBlank())
        //         return;

            // User user1 = Fetch.fetchUserByName(name1);
            // User user2 = Fetch.fetchUserByName(name2);

            // List<Follow> userFollows1 = getFollows(null, new ArrayList<>(), user1.getId());
            // List<Follow> userFollows2 = getFollows(null, new ArrayList<>(), user2.getId());

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

            return equalFollows;


    }

    
    public static List<Follow> getFollows(String cursor, List<Follow> list, String fromUser){

        FollowList follows = Context.twitchClient().getHelix().getFollowers(Context.credential.getAccessToken(), fromUser, null, cursor, 100).execute();
        list.addAll(follows.getFollows());

        //if(follows.getPagination().getCursor() != null)
        //    getFollows(follows.getPagination().getCursor(), list, fromUser);

        return list;
    }


    public static List<FollowItem> mapToFollowItemsFrom(List<Follow> followerList){
        return followerList.stream().map(new Function<Follow, FollowItem>(){
            @Override
            public FollowItem apply(Follow follow) {
                return new FollowItem(follow.getToLogin(), follow);
            }
        }).collect(Collectors.toList());
    }
        
    
    public static List<FollowItem> mapToFollowItemsTo(List<Follow> followerList){
        return followerList.stream().map(new Function<Follow, FollowItem>(){
            @Override
            public FollowItem apply(Follow follow) {
                return new FollowItem(follow.getFromLogin(), follow);
            }
        }).collect(Collectors.toList());
        
    }

    public static FollowItem getFollowItemByUserLogin(GraphUser graphUser, List<FollowItem> followItems){
        for (FollowItem followItem : followItems) {
            if(followItem.login.equalsIgnoreCase(graphUser.login)){
                return followItem;
            }           
        }
        return null;
    }

    
}
