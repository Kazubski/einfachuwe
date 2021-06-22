package de.calitobundo.twitch.desktop.data;

import java.util.ArrayList;
import java.util.List;

import de.calitobundo.twitch.desktop.dto.StreamInfo;

public class StreamInfoData {

    private List<StreamInfo> streamInfos = new ArrayList<>();

    public StreamInfoData() {
    }

    public StreamInfoData(List<StreamInfo> list) {
        streamInfos.addAll(list);
    }

    public List<StreamInfo> getStreamInfo() {
        return streamInfos;
    }

    public void setStreamInfos(List<StreamInfo> streamInfos) {
        this.streamInfos = streamInfos;
    }
}
