package com.makersacademy.acebook.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class LikeTest {
    private User user = new User("testuser");
    private Post post = new Post("hello", user, null);
    private Like like = new Like(post, user);

    @Test
    public void likeBelongsToPost() {
        assertThat(like.getPost().getContent(), containsString("hello"));
    }

    @Test
    public void likeBelongsToUser() {
        assertThat(like.getUser().getUsername(), containsString("testuser"));
    }
}
