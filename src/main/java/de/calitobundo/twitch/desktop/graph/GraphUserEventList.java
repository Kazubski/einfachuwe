package de.calitobundo.twitch.desktop.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.calitobundo.twitch.desktop.api.UweColors;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GraphUserEventList {

    public static final List<DrawUser> list = new ArrayList<>();

    public static void add(GraphUser user){
        list.add(0, new DrawUser(user));
    }

    public static void remove(){
        final List<DrawUser> removeList = list.stream().filter(d -> d.remove).collect(Collectors.toList());
        list.removeAll(removeList);
    }

    public static void render(GraphicsContext gc){
        remove();
        double y = 0;
        for (DrawUser drawUser : list) {
            y += 23;
            drawUser.render(gc, y);
        }
    }

    private static class DrawUser {

        private final GraphUser graphUser;
        public boolean remove = false;
        public double time = 0;
        public final String text;
        public final Color color;
        public DrawUser(GraphUser user){
            this.graphUser = user;
            this.text = user.login;//+" ("+user.joinCount+")";
            this.color = user.joined ? UweColors.colorYellow : UweColors.colorBlue;
        }

        public void render(GraphicsContext gc, double y){
                time += 1;
                gc.setFill(UweColors.colorRed);
                gc.fillRect(10, y+4, 100*graphUser.fetchProgress, 3);
                gc.setFill(color);
                gc.fillText(text, 10, y);
                if(time > 50){
                    remove = true;
                    graphUser.fetchProgress = 0;
                }
        }
    }
}
