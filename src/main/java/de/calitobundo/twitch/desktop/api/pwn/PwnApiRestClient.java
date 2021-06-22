package de.calitobundo.twitch.desktop.api.pwn;

import static de.calitobundo.twitch.desktop.api.pwn.PwnApiServiceGenerator.*;

public class PwnApiRestClient {

    private final PwnApiService pwnApiService;


    public PwnApiRestClient() {
        this.pwnApiService = createService(PwnApiService.class);
    }

    public StreamResult getPlaylist(String url) {
        return executeSync(pwnApiService.getPlaylist(url));
    }

}
