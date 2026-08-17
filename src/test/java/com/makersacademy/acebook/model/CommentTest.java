package com.makersacademy.acebook.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class CommentTest {
    private User user = new User("testuser");
    private Post post = new Post("hello", user, null);
    private Comment comment = new Comment("nice post!", post, user);

    @Test
    public void commentHasContent() {
        assertThat(comment.getContent(), containsString("nice post!"));
    }

    @Test
    public void commentHasAuthor() {
        assertThat(comment.getUser().getUsername(), containsString("testuser"));
    }

    @Test
    public void commentBelongsToPost() {
        assertThat(comment.getPost().getContent(), containsString("hello"));
    }
}
