package de.calitobundo.twitch.desktop.views;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.github.twitch4j.kraken.domain.SimpleEmoticon;

import de.calitobundo.twitch.desktop.api.Context;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class MegaEmoteShowView extends ImageView {
    

    public final static String url2 = "https://static-cdn.jtvnw.net/emoticons/v1/%s/%s";

    private final Timer timer = new Timer(true);
    private long delay = 2000;
    private boolean running = false;
    private Stage stage;

    public void start(long delay, Stage stage){
        this.delay = delay;
        this.stage = stage;
        this.running = true;
        next();
    }

    private void next(){

        if(!running || Context.emoticons.isEmpty())
            return;

        final Random random = new Random();
        final SimpleEmoticon emoticon = Context.emoticons.get(random.nextInt(Context.emoticons.size()));
        final String emoteId = String.valueOf(emoticon.getId());
        Platform.runLater(() -> {
            stage.setTitle(emoticon.getCode());
        });
        final String url = String.format(url2, emoteId, "3.0");
        System.out.println("url "+url);
        Image image = new Image(url, 300, 300, true, true);
        timer.schedule(new DiashowTimerTask(image), delay);
    }

    public void stop(){
        running = false;
        timer.cancel();
    }

    private class DiashowTimerTask extends TimerTask {

        private final Image image;
        public DiashowTimerTask(Image image){
            this.image = image;
        }

        @Override
        public void run() {
            Platform.runLater(() -> {
                setImage(image);
            });
            next();
        }    
    }

}
