package com.makersacademy.acebook.model;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FriendshipTest {

    private User user = new User("user");
    private User friend = new User("friend");
    private Friendship friendship = new Friendship(user, friend, "ACCEPTED");

    @Test
    public void hasCorrectUserWhoRequestedFriend() {
        assertThat(friendship.getUser().getUsername(), containsString("user"));
    }

    @Test
    public void hasCorrectFriendWhoWasAdded() {
        assertThat(friendship.getFriend().getUsername(), containsString("friend"));
    }

    @Test
    public void friendInstantlyAccepted() {
        assertThat(friendship.getStatus(), containsString("ACCEPTED"));
    }
}
