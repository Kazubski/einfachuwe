package de.calitobundo.twitch.desktop.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.twitch4j.helix.domain.Follow;
import com.github.twitch4j.helix.domain.User;

import de.calitobundo.twitch.desktop.api.Context;
import de.calitobundo.twitch.desktop.api.Fetch;
import de.calitobundo.twitch.desktop.views.ChannelInformationView.ChannelStatsItem;
import javafx.scene.paint.Color;

public class GraphUtils {
    
    private static final Comparator<GraphUser> SIZE_COMPARATOR = Comparator.comparingInt(GraphUser::size).reversed();

    private GraphUtils(){}

    public static GraphUser fetchGraphUser(String name) {
        final User user = Fetch.fetchUserByName(name);
        return user != null ? fetchGraphUser(user) : null;
    }

    public static GraphUser fetchGraphUser(User user) {
        final List<Follow> followers = Fetch.followersFromUserId(null, null, new ArrayList<>(), user.getId(), Context.twitchClient, Context.credential);
        final GraphUser graphUser = new GraphUser(user, followers);
        return graphUser;
    }

    public static List<Follow> fetchFollowers(User user, ProgressEvent event) {
        final List<Follow> followers = Fetch.followersFromUserId2(event, null, new ArrayList<>(), user.getId(), Context.twitchClient, Context.credential);
        return followers;
    }

    public static long getMax(int value, Stream<Long> stream){

        Optional<Long> optional = stream.max(new Comparator<Long>(){
            @Override
            public int compare(Long o1, Long o2) {
                return (int)(o1 - o2);
            }
        });
        if(optional.isPresent())
            return optional.get();
        else
            return value;
    }

    public static int getMax(int maxLevel, List<GraphUser> graphUsers){

        int max = 0;
        int maxCount = 0;
        if(maxLevel > 0){
            max = maxLevel;
        }else{
            for (GraphUser user : graphUsers) {
                if(user.fromUsers.size() > max){
                    max = user.fromUsers.size();
                    maxCount = 1;
                }else if(user.fromUsers.size() == max){
                    maxCount++;
                }
            }
            if(maxCount > 1){
                max++;
            }
        }
        return max;
    }

    public static void orderUsers(List<GraphUser> graphUsers){
        final List<GraphUser> users = graphUsers.stream()
            .filter(user -> user.fromUsers.size() == 0 && !user.removed && !user.ignored)
            .sorted(Comparator.comparingDouble(GraphUser::angle))
            .collect(Collectors.toList());
        final double angle2 = 2*Math.PI/users.size();
        final double startAngle = users.get(0).angle() % (2*Math.PI);
        for (int i = 0; i < users.size(); i++) {
            users.get(i).angle = startAngle + i * angle2;
        }
    }


    public static void calculateFollowLines(List<GraphUser> allGraphUsers){

        
        allGraphUsers.forEach(GraphUser::clearFollowLines);

        List<GraphUser> graphUsers = allGraphUsers.stream().filter(g -> !g.removed && !g.ignored).collect(Collectors.toList());
        
        for (GraphUser toUser : graphUsers) {
            //toUser.fromUsers.clear();
            toUser.first = false;
            for (GraphUser fromUser : graphUsers) {
                if(!toUser.equals(fromUser)){
                    for (Follow follow : fromUser.followers) {
                         if(follow.getToLogin().equals(toUser.login)){
                            toUser.fromUsers.put(fromUser.login, fromUser);
                            fromUser.toUsers.put(toUser.login, toUser);
                        }
                    }
                }
            }
        }
        graphUsers.forEach(user -> {
            user.layout.update();
        });
        graphUsers.sort(SIZE_COMPARATOR);
    }




    public static GraphUser findUser(List<GraphUser> list, double mouseX, double mouseY, boolean showAll){

        list.stream().filter(user -> user.selected).forEach(user -> {
            user.selected = false;
        });
        for (GraphUser user : list) {
            if(showAll || user.size() > 0 || user.toUsers.size() > 0 || user.first){
                final double dx = mouseX - user.x;
                final double dy = mouseY - user.y;
                final double distance = Math.sqrt(dx*dx+dy*dy);
                final double minDistance = 3 * user.radius;
                if(distance < minDistance){
                    user.selected = true;
                    return user;
                }
            }
        }
        return null;
    }

    public static final String cssTextFill = "-fx-text-fill: rgba(252, 122, 122, 255);";

    // public static ColorFading getColorFading(){


    //     ColorSet start = new ColorSet(20, 20, 20, 255);
    //     ColorSet end = new ColorSet(122, 122, 122, 255);
    //     return new ColorFad;
    // }


    public static class ColorFading {
        public final ColorSet colorStart;
        public final ColorSet colorEnd;
        public final ColorSet colorDiff;
        public ColorFading(ColorSet colorStart, ColorSet colorEnd){
            this.colorStart = colorStart;
            this.colorEnd = colorEnd;
            this.colorDiff = colorEnd.sub(colorStart);
        }
        public ColorSet getColorSet(double frac){
            return colorStart.add(colorDiff.mul(frac));
        }
        public Color getColor(double frac){
            return getColorSet(frac).toColor();
        }

    }

    public static class ColorSet {
        final String cssTextFill = "rgba(%d, %d, %d, %d)";
        public double red;
        public double green;
        public double blue;
        public double alpha;
        public ColorSet(double red, double green, double blue, double alpha){
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
        public ColorSet(Color color){
            this.red = color.getRed();
            this.green = color.getGreen();
            this.blue = color.getBlue();
            this.alpha = color.getOpacity();
        }
        public ColorSet add(ColorSet color){
            double red = this.red + color.red;
            double green = this.green + color.green;
            double blue = this.blue + color.blue;
            double alpha = this.alpha + color.alpha;
            return new ColorSet(red, green, blue, alpha);
        }
        public ColorSet sub(ColorSet color){
            double red = this.red - color.red;
            double green = this.green - color.green;
            double blue = this.blue - color.blue;
            double alpha = this.alpha - color.alpha;
            return new ColorSet(red, green, blue, alpha);
        }
        public ColorSet mul(double frac){
            double red = this.red * frac;
            double green = this.green * frac;
            double blue = this.blue * frac;
            double alpha = this.alpha * frac;
            return new ColorSet(red, green, blue, alpha);
        }
        public String getString(){
            return String.format(cssTextFill, (int)(red*255), (int)(green*255), (int)(blue*255), (int)(alpha*255));
        }

        public Color toColor(){
            return new Color(red, green, blue, alpha);
        }
    }




    private static final long KILO = 1000;
    private static final long MEGA = KILO*KILO;
    private static final long GIGA = KILO*MEGA;
    private static final long TERRA = KILO*GIGA;
    public static String getViewsShortString(long value){
        final long terras = value / TERRA;
        final long restTerras = value % TERRA;
        final long gigas = restTerras / GIGA;
        final long restGigas = restTerras % GIGA;
        final long megas = restGigas / MEGA;
        final long restMegas = restGigas % MEGA;
        final long kilos = restMegas / KILO;
        final long restKilos =  restMegas % KILO;
        if(terras > 0){
            return terras+"T";
        }else if(gigas > 0){
            return gigas+"G";
        }else if(megas > 0){
            return megas+"M";
        }else if(kilos > 0){
            return kilos+"K";
        }else{
            return restKilos+"";
        }
    }

    private static final long SECOND = 1000;
    private static final long MINUTE = 60*SECOND;
    private static final long HOUR = 60*MINUTE;
    private static final long DAY = 24*HOUR;
    private static final long YEAR = 365*DAY;
    public static String getTimeAgo(long duration){
        final long years = duration / YEAR;
        final long restYears = duration % YEAR;
        final long days = restYears / DAY;
        final long restDays = restYears % DAY;
        final long hours = restDays / HOUR;
        final long restHours = restDays % HOUR;
        final long minutes = restHours / MINUTE;
        final long restMinutes =  restHours % MINUTE;
        final long seconds = restMinutes / SECOND;
        final long restMillis = restMinutes % SECOND;
        if(years > 0){
            return years+"y"+days+"d";
        }else if(days > 0){
            return days+"d";
        }else if(hours > 0){
            return hours+"h";
        }else if(minutes > 0){
            return minutes+"m";
        }else if(seconds > 0){
            return seconds+"s";
        }else{
            return restMillis+"ms";
        }
    }


    public static List<List<String>> getSubLists(int size, List<String> list){
        final List<List<String>> lists = new ArrayList<>();

        // int indexStart = 0;
        // int indexEnd = list.size() > 100 ? 100 : list.size();


        for (int i = 0; i < list.size(); i += size) {
            lists.add(list.subList(i, Math.min(i + size, list.size())));
        }

        // while(list.size() > size){
        //     final List<String> subList = list.subList(0, size);
        //     list.removeAll(subList);
        //     lists.add(subList);
        // }
        // if(!list.isEmpty()){
        //     lists.add(list);
        // }
        return lists;
    }


}
