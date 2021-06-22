package de.calitobundo.twitch.desktop.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import de.calitobundo.twitch.desktop.chat.ChatEmote;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class EmoteDiashowView extends ImageView {
    
    private final List<Image> images = new ArrayList<>();
    private final Timer timer = new Timer(true);
    private long delay = 2000;
    private int index = 0;
    private boolean running = false;

    public EmoteDiashowView(){

    }

    private void next(){

        if(!running)
            return;

        index++;
        if(index >= images.size())
            index = 0;

        timer.schedule(new DiashowTimerTask(), delay);
    }

    public void start(long delay){
        this.delay = delay;

        index = 0;
        ChatEmote.images.entrySet().stream()
            .map(s -> s.getKey())
            .filter(i -> i.endsWith("304"))
            .collect(Collectors.toList())
            .forEach(d -> { 
                images.add(ChatEmote.images.get(d));
            });

        if(images.isEmpty()){
            running = false;
            return;
        }else{
            running = true;
            timer.schedule(new DiashowTimerTask(), 100);
        }
    }

    public void stop(){
        running = false;
        timer.cancel();
    }

    public void setDelay(long delay){
        this.delay = delay;
    }

    private class DiashowTimerTask extends TimerTask {

        @Override
        public void run() {
            Platform.runLater(() -> {
                setImage(images.get(index));
            });
            next();
        }    
    }


}
