package com.makersacademy.acebook.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

    @Test
    public void canBeMarkedAsRead() {
        notification.setRead(true);
        assertThat(notification.isRead(), is(true));
    }

    @Test
    public void constructorSetsCreatedAt() {
        assertThat(notification.getCreatedAt(), is(notNullValue()));
    }

    @Test
    public void noArgsConstructorStartsUnreadWithNullFields() {
        Notification blank = new Notification();

        assertThat(blank.isRead(), is(false));
        assertThat(blank.getUser(), is(nullValue()));
        assertThat(blank.getMessage(), is(nullValue()));
        assertThat(blank.getLink(), is(nullValue()));
    }

    @Test
    public void noArgsConstructorStillSetsCreatedAt() {
        Notification blank = new Notification();
        assertThat(blank.getCreatedAt(), is(notNullValue()));
    }

    @Test
    public void messageCanBeUpdatedAfterCreation() {
        notification.setMessage("new message");
        assertThat(notification.getMessage(), equalTo("new message"));
    }

    @Test
    public void recipientCanBeReassigned() {
        User otherUser = new User("otheruser");
        notification.setUser(otherUser);
        assertThat(notification.getUser().getUsername(), equalTo("otheruser"));
    }

    @Test
    public void twoNotificationsForDifferentUsersAreNotEqual() {
        User anotherUser = new User("anotheruser");
        Notification other = new Notification(anotherUser, "someone liked your post", "/posts/1");

        assertThat(notification, is(not(equalTo(other))));
    }

    @Test
    public void toStringDoesNotThrowAndIncludesMessage() {
        assertThat(notification.toString(), containsString("someone liked your post"));
    }
}
