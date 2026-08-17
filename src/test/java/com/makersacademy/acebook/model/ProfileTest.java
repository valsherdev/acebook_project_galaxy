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
    public void testCanGetId() {
        assertThat(profile.getId(), is(1L));
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

    @Test
    public void testCanGetAboutMe() {
        assertThat(profile.getAboutMe(), containsString("I am a test"));
    }

    @Test
    public void testAllArgsConstructorAndGetters() {
        assertThat(profile.getId(), is(1L));
        assertThat(profile.getFirstName(), is("Test"));
        assertThat(profile.getLastName(), is("User"));
        assertThat(profile.getCurrentLocation(), is("Southampton"));
        assertThat(profile.getHometown(), is("Newcastle"));
        assertThat(profile.getProfilePicture(), is(dummyBytes));
        assertThat(profile.getAboutMe(), is("I am a test"));
        assertThat(profile.getUser().getUsername(), is("testuser@test.com"));

    }

    // tests lombok getter and setter methods
    @Test
    public void testNoArgsConstructorAndSetters() {
        Profile emptyProfile = new Profile();
        emptyProfile.setId(2L);
        emptyProfile.setFirstName("Jane");
        emptyProfile.setLastName("Doe");
        emptyProfile.setCurrentLocation("London");
        emptyProfile.setHometown("Manchester");
        emptyProfile.setProfilePicture(dummyBytes);
        emptyProfile.setAboutMe("Hello world");
        emptyProfile.setUser(user);

        assertThat(emptyProfile.getId(), is(2L));
        assertThat(emptyProfile.getFirstName(), is("Jane"));
        assertThat(emptyProfile.getLastName(), is("Doe"));
        assertThat(emptyProfile.getCurrentLocation(), is("London"));
        assertThat(emptyProfile.getHometown(), is("Manchester"));
        assertThat(emptyProfile.getProfilePicture(), is(dummyBytes));
        assertThat(emptyProfile.getAboutMe(), is("Hello world"));
        assertThat(emptyProfile.getUser(), is(user));
    }

    // tests lomboks three special helper methods that Lombok automatically creates for the Profile
    // class behind the scenes: toString(), equals(), and hashCode()
    @Test
    public void testLombokToStringEqualsAndHashCode() {
        Profile p1 = new Profile(1L, "Test", "User", "Southampton", "Newcastle", dummyBytes, "I am a test", user);
        Profile p2 = new Profile(1L, "Test", "User", "Southampton", "Newcastle", dummyBytes, "I am a test", user);

        // toString() - tests to see that the object as a string looks how the actual object should look
        assertThat(p1.toString(), containsString("Profile"));

        // hashcode - like a digital footprint
        assertThat(p1.hashCode(), is(p2.hashCode()));

        // equals - testing whether p1 is the same and matches whatever is in the bracket
        assertThat(p1.equals(p2), is(true));
        assertThat(p1.equals(null), is(false));
        assertThat(p1.equals(new Object()), is(false));
    }

}




