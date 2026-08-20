package com.makersacademy.acebook.feature;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class FriendshipFeatureTest {

    WebDriver driver;
    Faker faker;
    WebDriverWait wait;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait =  new WebDriverWait(driver, Duration.ofSeconds(15));
        faker = new Faker();
    }

    @AfterEach
    public void tearDown() {
        driver.close();
        jdbcTemplate.update("DELETE FROM notifications");
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM comments");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("DELETE FROM users");
    }

    private void signUp(String email) {
        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));

    }

    private Long insertUser(String username) {
        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", username);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    private Long currentUserId(String email) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, email);
    }



    @Test
    public void userSeesAddFriendButton() {
        String email = faker.name().username() + "@email.com";
        signUp(email);

        String otherUsername = faker.name().username() + "@email.com";
        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", otherUsername);

        driver.get("http://localhost:8081/friends");

        assertFalse(driver.findElements(By.xpath("//button[contains(text(), 'Add Friend')]")).isEmpty());

    }

    @Test
    public void userSeesIncomingFriendRequestAndCanAccept() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        String friendUsername = faker.name().username() + "@email.com";
        Long friendId = insertUser(friendUsername);

        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'PENDING')",
                friendId, myId);

        driver.get("http://localhost:8081/friends");
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains(friendUsername));

        driver.findElement(By.cssSelector("form[action*='/accept'] button[type='submit']")).click();

        String afterAccept = driver.findElement(By.tagName("body")).getText();
        assertTrue(afterAccept.contains(friendUsername));

    }

    @Test
    public void userSeesOutgoingRequestAsPendingAfterSending() {

        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);

        String otherUsername = faker.name().username() + "@email.com";
        insertUser(otherUsername);

        var count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM users WHERE username IN (?, ?)", Integer.class, myEmail, otherUsername);
        assertEquals(2, count);

        driver.get("http://localhost:8081/friends");
        String beforeSend = driver.findElement(By.tagName("body")).getText();
        assertTrue(beforeSend.contains(otherUsername));

        driver.findElement(By.cssSelector("form[action='/friends/add'] button[type='submit']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Pending"));

        String afterSend = driver.findElement(By.tagName("body")).getText();

        assertTrue(afterSend.contains("Pending"));
        assertTrue(afterSend.contains(otherUsername));

    }

    @Test
    public void decliningFriendRequestRemovesItFromTheDatabase() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        String requesterUsername = faker.name().username() + "@email.com";
        Long requesterId = insertUser(requesterUsername);

        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'PENDING')",
                requesterId, myId);

        driver.get("http://localhost:8081/friends");
        driver.findElement(By.cssSelector("form[action*='/decline'] button[type='submit']")).click();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Integer remaining = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                    Integer.class, requesterId, myId);
            assertEquals(0, remaining);
        });
    }

    @Test
    public void acceptedFriendNoLongerAppearsUnderFriendRequests() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        String friendUsername = faker.name().username() + "@email.com";
        Long friendId = insertUser(friendUsername);

        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'PENDING')",
                friendId, myId);

        driver.get("http://localhost:8081/friends");
        driver.findElement(By.cssSelector("form[action*='/accept'] button[type='submit']")).click();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String status = jdbcTemplate.queryForObject(
                    "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                    String.class, friendId, myId);
            assertEquals("ACCEPTED", status);
        });

        driver.navigate().refresh();
        assertTrue(driver.findElements(By.cssSelector("form[action*='/accept']")).isEmpty());
        assertTrue(driver.findElements(By.cssSelector("form[action*='/decline']")).isEmpty());
    }

    @Test
    public void acceptedFriendNoLongerShowsAddFriendButton() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        String friendUsername = faker.name().username() + "@email.com";
        Long friendId = insertUser(friendUsername);

        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'PENDING')",
                friendId, myId);

        driver.get("http://localhost:8081/friends");
        driver.findElement(By.cssSelector("form[action*='/accept'] button[type='submit']")).click();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String status = jdbcTemplate.queryForObject(
                    "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                    String.class, friendId, myId);
            assertEquals("ACCEPTED", status);
        });

        driver.navigate().refresh();
        assertTrue(driver.findElements(By.xpath("//button[contains(text(), 'Add Friend')]")).isEmpty());
    }

    @Test
    public void sendingFriendRequestRemovesThemFromSuggestions() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);

        String otherUsername = faker.name().username() + "@email.com";
        insertUser(otherUsername);

        driver.get("http://localhost:8081/friends");
        driver.findElement(By.cssSelector("form[action='/friends/add'] button[type='submit']")).click();

        await().atMost(15, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            driver.navigate().refresh();

            assertTrue(driver.findElements(By.xpath("//button[contains(text(), 'Add Friend')]")).isEmpty());
        });
    }

    @Test
    public void multipleIncomingRequestsCanBeAcceptedIndependently() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        String firstRequesterUsername = faker.name().username() + "@email.com";
        Long firstRequesterId = insertUser(firstRequesterUsername);
        String secondRequesterUsername = faker.name().username() + "@email.com";
        Long secondRequesterId = insertUser(secondRequesterUsername);

        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'PENDING')",
                firstRequesterId, myId);
        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'PENDING')",
                secondRequesterId, myId);

        driver.get("http://localhost:8081/friends");

        driver.findElement(By.xpath(
                "//a[contains(@class,'friend-username') and normalize-space(.)='" + firstRequesterUsername + "']" +
                        "/following-sibling::form[contains(@action,'/accept')]//button"
        )).click();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String firstStatus = jdbcTemplate.queryForObject(
                    "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                    String.class, firstRequesterId, myId);
            assertEquals("ACCEPTED", firstStatus);
        });

        String secondStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                String.class, secondRequesterId, myId);
        assertEquals("PENDING", secondStatus);
    }

    @Test
    public void userDoesNotSeeThemselvesAsASuggestion() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);

        driver.get("http://localhost:8081/friends");
        assertTrue(driver.findElements(By.xpath("//button[contains(text(), 'Add Friend')]")).isEmpty());
    }

    @Test
    public void userCanRemoveAcceptedFriend() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        String friendUsername = faker.name().username() + "@email.com";
        Long friendId = insertUser(friendUsername);

        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'ACCEPTED')",
                myId, friendId);

        driver.get("http://localhost:8081/friends");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains(friendUsername));

        driver.findElement(By.cssSelector("button.delete-friend-btn")).click();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Integer remaining = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (friend_id = ? AND user_id = ?)",
                    Integer.class, myId, friendId, friendId, myId);
            assertEquals(0, remaining);
        });
    }
}
