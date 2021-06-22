package de.calitobundo.twitch.desktop.dto;

import com.github.twitch4j.helix.domain.Follow;
import java.util.Objects;

public class FollowItem {

    public final String login;
    public final String userName;
    public final Follow follow;

    public boolean chatter = false;

    public boolean ignored = false;
    public boolean banned = false;
    public boolean friend = false;

    public FollowItem(String userName, Follow follow) {
        this.userName = userName;
        this.login = userName;
        this.follow = follow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowItem userItem = (FollowItem) o;
        return Objects.equals(userName, userItem.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }
}
