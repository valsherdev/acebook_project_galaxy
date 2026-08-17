package com.makersacademy.acebook.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class NotificationTest {

    private User user = new User("testuser");
    private Notification notification = new Notification(user, "someone liked your post", "/posts/1");

    @Test
    public void hasCorrectRecipient() {
        assertThat(notification.getUser().getUsername(), equalTo("testuser"));
    }

    @Test
    public void hasCorrectMessage() {
        assertThat(notification.getMessage(), equalTo("someone liked your post"));
    }

    @Test
    public void hasCorrectLink() {
        assertThat(notification.getLink(), equalTo("/posts/1"));
    }

    @Test
    public void startsUnread() {
        assertThat(notification.isRead(), is(false));
    }
}
