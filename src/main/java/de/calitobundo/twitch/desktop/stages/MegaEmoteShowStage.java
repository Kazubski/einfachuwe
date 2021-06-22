package de.calitobundo.twitch.desktop.stages;

import java.util.ArrayList;
import java.util.List;

import de.calitobundo.twitch.desktop.api.UweColors;
import de.calitobundo.twitch.desktop.views.MegaEmoteShowView;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MegaEmoteShowStage extends Stage {

    private static final List<MegaEmoteShowStage> stages = new ArrayList<>();
    private final MegaEmoteShowView diashowView;


    public MegaEmoteShowStage(int x, int y, long delay, Stage owner){
        this(owner, delay);
        setTitle("MegaEmoteShowStage");
        initOwner(owner);
        initModality(Modality.NONE);
        setX(1920+x);
        setY(y);
        //setWidth(300);
        //setHeight(300);
        //setResizable(false);
        show();
    }

    private MegaEmoteShowStage(Stage owner, long delay){

        diashowView = new MegaEmoteShowView();
        final StackPane imagePane = new StackPane(diashowView);
        imagePane.setPrefSize(300, 300);

        final HBox layout = new HBox(imagePane);
        final Scene scene = new Scene(layout, 300, 300);
        scene.getStylesheets().add(UweColors.darkStyle);
        setScene(scene);

        setOnCloseRequest(e -> {
            diashowView.stop();
        });

        
        diashowView.start(delay, this);

    }

    public static void start(){


    }

    public static void closeAllStages(){
        stages.forEach(MegaEmoteShowStage::close);
        stages.clear();
    }
   

    public static void open(Stage stage) {

        //closeAllStages();
        stages.add(new MegaEmoteShowStage(100, 100, 1000, stage));
   

    }

}
