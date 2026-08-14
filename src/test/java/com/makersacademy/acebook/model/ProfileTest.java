package com.makersacademy.acebook.model;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.*;

public class ProfileTest {

    private byte[] dummyBytes = "test-image-bytes".getBytes();
    private final User user = new User("testuser@test.com", true);
    private Profile profile = new Profile(1L, "Test", "User", "Southampton", "Newcastle", dummyBytes, "I am a test", user);

    @Test
    public void profileLinkedToCorrectUser() {
        assertThat(profile.getUser().getUsername(), containsString("testuser@test.com"));
    }

    @Test
    public void testCanGetFirstName() {
        assertThat(profile.getFirstName(), containsString("Test"));
    }

    @Test
    public void testCanGetLastName() {
        assertThat(profile.getLastName(), containsString("User"));
    }

    @Test
    public void testCanGetCurrentLocation() {
        assertThat(profile.getCurrentLocation(), containsString("Southampton"));
    }

    @Test
    public void testCanGetHometown() {
        assertThat(profile.getHometown(), containsString("Newcastle"));
    }

    @Test
    public void testConvertsProfilePictureToStringCorrectly() {
        String expectedBase64 = java.util.Base64.getEncoder().encodeToString(dummyBytes);
        assertThat(profile.convertImageByteToString(), is(expectedBase64));
    }

    @Test
    public void testConvertsNullProfilePictureToNullString() {
        Profile emptyProfile = new Profile();
        assertThat(emptyProfile.convertImageByteToString(), nullValue());
    }
}




