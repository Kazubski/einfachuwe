module uwedesktop {

    requires javafx.controls;
    //requires twitch4j;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.module.paramnames;
    requires credentialmanager;
    requires twitch4j.helix;
    requires events4j.handler.simple;
    requires twitch4j.kraken;
    requires twitch4j.common;
    requires twitch4j.chat;
    requires log4j;

    exports de.calitobundo.twitch.desktop to javafx.graphics;


//    requires twitch4j;
     //requires apollo.api;
    //opens apollo.api;s
   // opens log4;
    //exports de.calitobundo.twitch.desktop;
}