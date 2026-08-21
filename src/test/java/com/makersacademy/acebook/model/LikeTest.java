package com.makersacademy.acebook.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

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

    @Test
    public void likeReferencesTheExactPostObject() {
        assertThat(like.getPost(), equalTo(post));
    }

    @Test
    public void likeReferencesTheExactUserObject() {
        assertThat(like.getUser(), equalTo(user));
    }

    @Test
    public void twoLikesOnSamePostByDifferentUsersAreIndependent() {
        User secondUser = new User("anotheruser");
        Like secondLike = new Like(post, secondUser);

        assertThat(like.getUser().getUsername(), not(equalTo(secondLike.getUser().getUsername())));
        assertThat(like.getPost(), equalTo(secondLike.getPost()));
    }

    @Test
    public void sameUserCanLikeDifferentPosts() {
        Post secondPost = new Post("another post", user, null);
        Like likeOnSecondPost = new Like(secondPost, user);

        assertThat(like.getUser(), equalTo(likeOnSecondPost.getUser()));
        assertThat(like.getPost(), not(equalTo(likeOnSecondPost.getPost())));
    }


}
