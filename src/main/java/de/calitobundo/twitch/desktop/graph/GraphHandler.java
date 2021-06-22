package de.calitobundo.twitch.desktop.graph;

import java.util.List;

public interface GraphHandler {


    List<GraphUser> getGraphUsers();
    void setRotation(boolean rotation);
    double getCenter();
    void openGraphInStage();
    void closeGraphInStage();
    void onUserRemoved(GraphUser graphUser);

}
