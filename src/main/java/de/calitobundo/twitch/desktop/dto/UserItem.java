package de.calitobundo.twitch.desktop.dto;

import com.github.twitch4j.helix.domain.Stream;

import de.calitobundo.twitch.desktop.graph.GraphUser;

import java.util.Objects;

public class UserItem {

    private GraphUser graphUser = null;
    public final String userName;
    public boolean ignored;
    public Stream stream = null;


    public UserItem(String userName, boolean ignored) {
        this.userName = userName;
        this.ignored = ignored;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserItem userItem = (UserItem) o;
        return Objects.equals(userName, userItem.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }

    public GraphUser getGraphUser(){
        return graphUser;
    }

    public void setGraphUser(GraphUser graphUser){
        this.graphUser = graphUser;
    }
}
