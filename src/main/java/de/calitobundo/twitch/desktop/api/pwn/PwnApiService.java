package de.calitobundo.twitch.desktop.api.pwn;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface PwnApiService {

   //https://pwn.sh/tools/streamapi.py?url=https://twitch.tv/einfachuwe42
    @GET("tools/streamapi.py")
    Call<StreamResult> getPlaylist(
            @Query("url") String url
    );



}


