package de.calitobundo.twitch.desktop;

public class Test {

    public static void main(String[] args) {



        final String url = "https://clips-media-assets2.twitch.tv/AT-cm%7C845011455-preview-480x272.jpg";


        final String suffix = "-preview-480x272.jpg";
        final String replace = ".mp4";

        final int index = url.indexOf("-preview");

        final String test = url.substring(0, index);
        System.out.println(test);

        //https://clips-media-assets2.twitch.tv/39575898972-offset-1622.mp4
        //https://clips-media-assets2.twitch.tv/39575898972-offset-1622-preview-480x272.jpg

        //https://clips-media-assets2.twitch.tv/AT-cm%7C845011455-preview-480x272.jpg

        //https://clips-media-assets2.twitch.tv/AT-cm%7C845011455.mp4
        //https://clips-media-assets2.twitch.tv/AT-cm%7C820839137.mp4



    }
}
