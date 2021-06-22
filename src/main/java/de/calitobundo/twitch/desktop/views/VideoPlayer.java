package de.calitobundo.twitch.desktop.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.calitobundo.twitch.desktop.api.pwn.PwnApiRestClient;
import de.calitobundo.twitch.desktop.api.pwn.StreamResult;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
// import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
// import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory;
// import uk.co.caprica.vlcj.player.base.MediaPlayer;
// import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
// import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VideoPlayer extends ImageView{


    //private final MediaPlayerFactory mediaPlayerFactory;

    //private final EmbeddedMediaPlayer embeddedMediaPlayer;
    private static final List<String> resolutions = new ArrayList<>();

   // private ImageView videoImageView;


    static {
        //resolutions.add("160p"); 648p60 432p
        resolutions.add("360p");
        resolutions.add("432p");
        resolutions.add("480p");
        resolutions.add("648p60");
        resolutions.add("720p");
        resolutions.add("720p60");
        resolutions.add("786p60");
        resolutions.add("900p60");
        resolutions.add("1080p");
        resolutions.add("1080p60");
    }

    public VideoPlayer(){
        
        List<String> VLC_GLOBAL_OPTIONS = new ArrayList<>(Arrays.asList(
               // "--network-caching=0",
               // "--file-caching=0",
               // "--dts-pts=2000",
                "--verbose=1"
        ));


        // mediaPlayerFactory = new MediaPlayerFactory(VLC_GLOBAL_OPTIONS);
        // embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        // embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

        //     @Override
        //     public void playing(MediaPlayer mediaPlayer) {
        //         System.out.println("playing "+mediaPlayer.media().info().mrl());
        //     }

        //     @Override
        //     public void stopped(MediaPlayer mediaPlayer) {
        //         System.out.println("stopped");
        //     }

        // });


        //embeddedMediaPlayer.controls().setPosition(0.4f);
            
    }

    public void init(){
        //this.videoImageView = new ImageView();
        setPreserveRatio(true);
       // embeddedMediaPlayer.videoSurface().set(ImageViewVideoSurfaceFactory.videoSurfaceForImageView(this));
    }

    public void start(String channelName){


        System.out.println("--------------------------------------------------------");
       // embeddedMediaPlayer.controls().stop();

        new Thread(() -> {

            PwnApiRestClient client = new PwnApiRestClient();
            StreamResult result = client.getPlaylist("https://www.twitch.tv/"+channelName);

            if(result.urls != null) {
                System.out.println(result.urls.keySet());
            } else {
                System.out.println("Keine result von der PwnApi!");
                return;
            }

            String streamUrl = null;
            String selectedResolution = "na";

            for (String resolution : resolutions) {
                if(result.urls.containsKey(resolution)){
                    streamUrl = result.urls.get(resolution);
                    selectedResolution = resolution;
                    break;
                }
            }

            if(streamUrl == null && result.urls.containsKey("160p")){
                streamUrl = result.urls.get("160p");
                selectedResolution = "160p";
            }

            if(streamUrl == null){
                System.out.println("Kein Stream in "+resolutions.toString()+" gefunden!");
                return;
            }else{
                System.out.println("Stream in "+selectedResolution+" gewählt!");
            }
            final String url = streamUrl;

            Platform.runLater(()-> {
                //updateDetails(item);
                //embeddedMediaPlayer.controls().stop();
                //embeddedMediaPlayer.audio().setVolume(35);
                //embeddedMediaPlayer.media().start(url);

            });
        }).start();

    }

    public void close(){
       // embeddedMediaPlayer.controls().stop();
        //embeddedMediaPlayer.release();
        //mediaPlayerFactory.release();
    }

}
