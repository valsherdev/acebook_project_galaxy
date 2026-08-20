package com.makersacademy.acebook.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MessageTest {

    private User sender = new User("sender");
    private User recipient = new User("recipient");
    private Message message = new Message(sender, recipient, "Hello");

    @Test
    public void hasCorrectSender() {
        assertThat(message.getSender().getUsername(), equalTo("sender"));
    }

    @Test
    public void hasCorrectRecipient() {
        assertThat(message.getRecipient().getUsername(), equalTo("recipient"));
    }

    @Test
    public void hasCorrectContent() {
        assertThat(message.getContent(), equalTo("Hello"));
    }

    @Test
    public void startsUnread() {
        assertThat(message.isRead(), is(false));
    }

    @Test
    public void canBeMarkedAsRead() {
        message.setRead(true);
        assertThat(message.isRead(), is(true));
    }

    @Test
    public void parameterisedConstructorSetsCreatedAt() {
        assertThat(message.getCreatedAt(), is(notNullValue()));
    }

    @Test
    public void noArgsConstructorStartsUnreadWithNullFields() {
        Message blank = new Message();

        assertThat(blank.isRead(), is(false));
        assertThat(blank.getSender(), is(nullValue()));
        assertThat(blank.getRecipient(), is(nullValue()));
        assertThat(blank.getContent(), is(nullValue()));
    }

    @Test
    public void contentCanBeUpdatedAfterCreation() {
        message.setContent("updated");
        assertThat(message.getContent(), equalTo("updated"));
    }

}
