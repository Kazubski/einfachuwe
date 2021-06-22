package de.calitobundo.twitch.desktop.stages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.calitobundo.twitch.desktop.api.UweColors;
import de.calitobundo.twitch.desktop.views.EmoteDiashowView;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EmoteDiashowStage extends Stage {
    
    private static final List<EmoteDiashowStage> stages = new ArrayList<>();
    private final EmoteDiashowView diashowView;

    public EmoteDiashowStage(int x, int y, long delay, Stage owner){
        this(owner, delay);
        setTitle("EmoteDiashowStage");
        initOwner(owner);
        initModality(Modality.NONE);
        setX(x);
        setY(y);
        //setWidth(300);
        //setHeight(300);
        //setResizable(false);
        show();
    }


    private EmoteDiashowStage(Stage owner, long delay){

        diashowView = new EmoteDiashowView();
        final StackPane imagePane = new StackPane(diashowView);
        imagePane.setPrefSize(300, 300);

        final HBox layout = new HBox(imagePane);
        final Scene scene = new Scene(layout, 300, 300);
        scene.getStylesheets().add(UweColors.darkStyle);
        setScene(scene);

        scene.setOnMouseClicked(e -> {
            final double width = 300;
            final double posX = e.getX();
            double result = 2000 * (posX / width); 
            if(result < 10){
                result = 10;
            }
            if(result > 2000){
                result = 2000;
            }    

            diashowView.setDelay((long)result);
        });

        setOnCloseRequest(e -> {
            diashowView.stop();
        });

        
        diashowView.start(delay);


    }

    public static void closeAllStages(){
        stages.forEach(EmoteDiashowStage::close);
        stages.clear();
    }
   

    public static void open(int sizeX, int sizeY, Stage stage) {

        closeAllStages();

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                final Random random = new Random();
                final long delay = 500 + (long)(1000 * random.nextDouble());
                final int left = 1920 + x * 302; 
                final int top =  45 + y * 329;
                stages.add(new EmoteDiashowStage(left, top, delay, stage));
            }
        }

    }


}
