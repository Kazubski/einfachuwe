package de.calitobundo.twitch.desktop.api;

import java.util.Objects;

import javafx.scene.paint.Color;

public class UweColors {
    
    public static String darkStyle = Objects.requireNonNull(UweColors.class.getClassLoader().getResource("css/modena_dark.css")).toExternalForm();

    public static final String hexColorYellow = "#fce47a"; //252, 228, 122
    public static final String hexColorBlue = "#7c7aff"; //124, 122, 255
    public static final String hexColorRed = "#fc7a7a"; //252, 122, 122
    public static final String hexColorDarkRed = "#b22222"; //178, 34, 34
    public static final String hexColorGreen = "#7afc85"; //122, 252, 133
    public static final String hexColorLightGray = "#e6e6e6"; //230, 230, 230
    public static final String hexColorGray = "#a0a0a0"; //160, 160, 160
    public static final String hexColorDarkGray = "#646464"; //100, 100, 100
    public static final String hexColorDarkerGray = "#282828"; //40, 40, 40

    

    public static Color colorYellow = Color.valueOf(hexColorYellow);
    public static Color colorBlue = Color.valueOf(hexColorBlue);
    public static Color colorRed = Color.valueOf(hexColorRed);
    public static Color colorDarkRed = Color.valueOf(hexColorDarkRed);
    public static Color colorGreen = Color.valueOf(hexColorGreen);
    public static Color colorLightGray = Color.valueOf(hexColorLightGray);
    public static Color colorGray = Color.valueOf(hexColorGray);
    public static Color colorDarkGray = Color.valueOf(hexColorDarkGray);
    public static Color colorDarkerGray = Color.valueOf(hexColorDarkerGray);

    public static final String cssTextFill = "-fx-text-fill: %s;";
    public static final String cssYellowColor = String.format(cssTextFill, hexColorYellow);
    public static final String cssBlueColor = String.format(cssTextFill, hexColorBlue);
    public static final String cssRedColor = String.format(cssTextFill, hexColorRed);
    public static final String cssGreenColor = String.format(cssTextFill, hexColorGreen);
    public static final String cssTransparentColor = String.format(cssTextFill, "transparent");


    // public static final String cssWhiteColor = "-fx-text-fill: rgba(255, 255, 255, 255);";
    // public static final String cssGrayColor = "-fx-text-fill: rgba(124, 122, 122, 255);";
    // public static final String cssLightGrayColor = "-fx-text-fill: rgba(201, 186, 186, 255);";





    public void test(){



    }


}
