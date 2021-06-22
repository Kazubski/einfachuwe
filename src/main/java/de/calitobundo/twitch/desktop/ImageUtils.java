package de.calitobundo.twitch.desktop;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import javafx.scene.image.Image;

public class ImageUtils {



    public static Image fetchImage(String url, double width, double height) {

        Image image = null;
        if(url != null){
            try {

            final BufferedImage bufferedImage = ImageIO.read(new URL(url));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "gif", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            
            image = new Image(is, width, height, false, false);

            } catch (MalformedURLException e1) {

            } catch (IOException e1) {

            }
        }
        return image;
    }
    
}
