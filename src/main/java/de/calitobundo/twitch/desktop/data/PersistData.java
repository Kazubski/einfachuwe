package de.calitobundo.twitch.desktop.data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PersistData {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T loadJsonObject(Class<T> clazz){

        T data = null;
        try {
            File file = getFileFromClass(clazz);
            if(!file.exists()){
                saveJsonObject(data, clazz);
            }
            data = mapper.readValue(file, clazz);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    public static void saveJsonObject(Object object, Class<?> clazz){
        
        try {

            String string = mapper.writeValueAsString(object);
            File file = getFileFromClass(clazz);
            Files.writeString(file.toPath(), string, StandardCharsets.UTF_8);
            System.out.println("Datei "+file.getPath()+" geschrieben!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static File getFileFromClass(Class<?> clazz){
        final String className = clazz.getTypeName();
        final String name = className.substring(className.lastIndexOf(".")+1);
        File file = new File(name.concat(".json"));
        System.out.println("filename "+file.getPath());
        return file;
    }

    /**
     * 
     * 
     * 
     * 
     */


    public static void init(){
        ignoredUserData = PersistData.loadJsonObject(IgnoredUserData.class);
        if(ignoredUserData == null){
            saveJsonObject(ignoredUserData, IgnoredUserData.class);
        }
        removedUserData = PersistData.loadJsonObject(RemovedUserData.class);
        if(removedUserData == null){
            removedUserData = new RemovedUserData();
            removedUserData.getList().addAll(Arrays.asList(new String[]{ "einfachuwe42", "eyeresponse"}));
            saveJsonObject(removedUserData, RemovedUserData.class);
        }
    }


    public static IgnoredUserData ignoredUserData = new IgnoredUserData();
    public static RemovedUserData removedUserData = new RemovedUserData();

    public static void addToIgnoredUserAndSave(String name) {
        
        List<String> ignoredUsers = ignoredUserData.getList();

        if(ignoredUsers.contains(name)){
            System.out.println(name+" ist bereits in der ignoreliste!");
        }else{
            ignoredUsers.add(name);
            //IgnoredUserData ignoredUserData = new IgnoredUserData(ignoredUsers);
            PersistData.saveJsonObject(ignoredUserData, IgnoredUserData.class);
            System.out.println(name+" in die ignoreliste hinzugefügt!");
        }
    }

    public static void removeFromIgnoredUserAndSave(String name) {

        List<String> ignoredUsers = ignoredUserData.getList();

        if(ignoredUsers.remove(name)) {
            System.out.println(name + " aus der ignoreliste entfernt!");
            //IgnoredUserData ignoredUserData = new IgnoredUserData(ignoredUsers);
            PersistData.saveJsonObject(ignoredUserData, IgnoredUserData.class);
        }else {
            System.out.println(name + " nicht in der ignoreliste gefunden!");
        }
    }


    public static boolean ignored(String string){
        return ignoredUserData.contains(string);
    }

    public static boolean removed(String string){
        return removedUserData.contains(string);
    }

    // public static List<String> getIgnoredUsers() {
    //     return ignoredUserData.getList();
    // }



    static{

        final List<String> ignoredUsers = new ArrayList<>();
        ignoredUsers.add("electricallongboard");
        ignoredUsers.add("maybeilookoutofhiswindow");
        ignoredUsers.add("lurxx");
        ignoredUsers.add("saddestkitty");
        ignoredUsers.add("letsdothis_music");
        ignoredUsers.add("letsdothis_hostraffle");
        ignoredUsers.add("sad_grl");
        ignoredUsers.add("thiccur");
        ignoredUsers.add("mslenity");
        ignoredUsers.add("thecommandergroot");
        ignoredUsers.add("commanderroot");
        ignoredUsers.add("eyeresponse");
        ignoredUsers.add("einfachuwe42");
        ignoredUsers.add("aten");
        ignoredUsers.add("josefsknigge");
        ignoredUsers.add("dinu");
        ignoredUsers.add("isnicable");
        ignoredUsers.add("skumshop");
        ignoredUsers.add("communityshowcase");
        ignoredUsers.add("bloodlustr");
        ignoredUsers.add("feet");
        ignoredUsers.add("thelurxxer");
        ignoredUsers.add("casinothanks");
        ignoredUsers.add("gowithhim");
        ignoredUsers.add("jointeffortt");
        ignoredUsers.add("droopdoggg");
        ignoredUsers.add("bingcortana");
        ignoredUsers.add("extramoar");
        ignoredUsers.add("havethis2");
        ignoredUsers.add("abbottcostello");
        ignoredUsers.add("ftopayr");
        ignoredUsers.add("icewizerds");
        ignoredUsers.add("v_and_k");
        ignoredUsers.add("virgoproz");
        ignoredUsers.add("jeffecorga");
        ignoredUsers.add("relishdrove");
        ignoredUsers.add("ra1denz");
        ignoredUsers.add("bristlerich");
        ignoredUsers.add("anotherttvviewer");
        ignoredUsers.add("rafflevantri");
        ignoredUsers.add("lecturerreflux");
        ignoredUsers.add("coubzilla");
        ignoredUsers.add("adenillect");
        ignoredUsers.add("tawmtawmz");
        ignoredUsers.add("srmx30519");
        ignoredUsers.add("xixixip");
        ignoredUsers.add("artjomv2");
        ignoredUsers.add("andyblackknight");
        ignoredUsers.add("a1bear");
        ignoredUsers.add("violets_tv");
        ignoredUsers.add("own3d");
        ignoredUsers.add("twicklebot");
        ignoredUsers.add("moobot");
        ignoredUsers.add("streamelements");
        ignoredUserData.setList(ignoredUsers);

    }









}
