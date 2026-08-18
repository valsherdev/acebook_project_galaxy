package com.makersacademy.acebook.feature;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class NotificationFeatureTest {

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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

    // helper methods:

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

    private Long insertPost(Long authorId, String content) {
        jdbcTemplate.update(
                "INSERT INTO posts (user_id, content, image, created_at) VALUES (?, ?, NULL, NOW())",
                authorId, content);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM posts WHERE user_id = ? AND content = ?",
                Long.class, authorId, content);
    }

    private Long currentUserId(String email) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, email);
    }

    // tests:

    @Test
    public void navBarShowsUnreadCountWhenNotificationExists() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);

        Long myId = currentUserId(myEmail);

        jdbcTemplate.update(
                "INSERT INTO notifications (user_id, message, link, read) VALUES (?, ?, ?, false)",
                myId, "someone sent you a friend request", "/friends");

        driver.get("http://localhost:8081/posts");
        String navBarText = driver.findElement(By.cssSelector(".navbar")).getText();
        assertTrue(navBarText.contains("Notifications (1)"));
    }

    @Test
    public void visitingNotificationsPageMarksAsRead() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);

        Long myId = currentUserId(myEmail);

        jdbcTemplate.update(
                "INSERT INTO notifications (user_id, message, link, read) VALUES (?, ?, ?, false)",
                myId, "someone commented on your post", "/posts/1");

        driver.get("http://localhost:8081/notifications");
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("someone commented on your post"));

        driver.get("http://localhost:8081/posts");
        String navBarAfter = driver.findElement(By.cssSelector(".navbar")).getText();
        assertFalse(navBarAfter.contains("Notifications (1)"));
    }

    @Test
    public void commentingOnAnotherUsersPostCreatesNotificationForOwner() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "Owner's original post");

        String commenterEmail = faker.name().username() + "@email.com";
        signUp(commenterEmail);

        driver.get("http://localhost:8081/posts");
        WebElement commentBox = driver.findElement(
                By.cssSelector("form[action='/posts/" + postId + "/comments'] input[name='content']"));
        commentBox.sendKeys("Nice post!");
        commentBox.submit();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            int notificationCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notifications WHERE user_id = ?", Integer.class, ownerId);
            assertEquals(1, notificationCount);
        });

        String message = jdbcTemplate.queryForObject(
                "SELECT message FROM notifications WHERE user_id = ?", String.class, ownerId);
        String link = jdbcTemplate.queryForObject(
                "SELECT link FROM notifications WHERE user_id = ?", String.class, ownerId);
        assertTrue(message.contains("commented on your post"));
        assertEquals("/posts/" + postId, link);
    }

    @Test
    public void commentingOnOwnPostDoesNotCreateNotification() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        driver.get("http://localhost:8081/posts");
        WebElement postInput = driver.findElement(By.cssSelector(".post-form input[type=text]"));
        postInput.sendKeys("My own post");
        postInput.submit();
        wait.until(ExpectedConditions.urlContains("/posts"));

        Long postId = jdbcTemplate.queryForObject(
                "SELECT id FROM posts WHERE user_id = ? AND content = ?", Long.class, myId, "My own post");

        driver.get("http://localhost:8081/posts");
        WebElement commentBox = driver.findElement(
                By.cssSelector("form[action='/posts/" + postId + "/comments'] input[name='content']"));
        commentBox.sendKeys("commenting on myself");
        commentBox.submit();
        wait.until(ExpectedConditions.urlContains("/posts"));

        int notificationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notifications WHERE user_id = ?", Integer.class, myId);
        assertEquals(0, notificationCount);
    }

    @Test
    public void likingAnotherUsersPostCreatesNotificationForOwner() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "Post to be liked");

        String likerEmail = faker.name().username() + "@email.com";
        signUp(likerEmail);

        driver.get("http://localhost:8081/posts");
        driver.findElement(By.cssSelector("form[action='/posts/" + postId + "/likes'] button")).click();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            int notificationCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notifications WHERE user_id = ?", Integer.class, ownerId);
            assertEquals(1, notificationCount);
        });

        String message = jdbcTemplate.queryForObject(
                "SELECT message FROM notifications WHERE user_id = ?", String.class, ownerId);
        String link = jdbcTemplate.queryForObject(
                "SELECT link FROM notifications WHERE user_id = ?", String.class, ownerId);
        assertTrue(message.contains("liked your post"));
        assertEquals("/posts/" + postId, link);
    }

    @Test
    public void likingOwnPostDoesNotCreateNotification() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        driver.get("http://localhost:8081/posts");
        WebElement postInput = driver.findElement(By.cssSelector(".post-form input[type=text]"));
        postInput.sendKeys("Like my own post");
        postInput.submit();

        await().atMost(5, TimeUnit.SECONDS).ignoreExceptions().untilAsserted(() -> {
            Integer postCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM posts WHERE user_id = ? AND content = ?", Integer.class, myId, "Like my own post");
            assertEquals(1, postCount);
        });
        Long postId = jdbcTemplate.queryForObject(
                "SELECT id FROM posts WHERE user_id = ? AND content = ?", Long.class, myId, "Like my own post");

        driver.get("http://localhost:8081/posts");
        driver.findElement(By.cssSelector("form[action='/posts/" + postId + "/likes'] button")).click();

        await().pollDelay(1, TimeUnit.SECONDS).atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
            int notificationCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notifications WHERE user_id = ?", Integer.class, myId);
            assertEquals(0, notificationCount);
        });
    }

    @Test
    public void unlikingAPostDoesNotCreateANotification() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "Post liked then unliked");

        String likerEmail = faker.name().username() + "@email.com";
        signUp(likerEmail);

        driver.get("http://localhost:8081/posts");
        By likeButton = By.cssSelector("form[action='/posts/" + postId + "/likes'] button");

        driver.findElement(likeButton).click();
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            int notificationCountAfterLike = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notifications WHERE user_id = ?", Integer.class, ownerId);
            assertEquals(1, notificationCountAfterLike);
        });

        driver.findElement(likeButton).click();
        await().pollDelay(1, TimeUnit.SECONDS).atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
            int notificationCountAfterUnlike = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notifications WHERE user_id = ?", Integer.class, ownerId);
            assertEquals(1, notificationCountAfterUnlike);
        });
    }

    @Test
    public void sendingFriendRequestCreatesNotificationForRecipient() {
        Long recipientId = insertUser(faker.name().username() + "@email.com");

        String requesterEmail = faker.name().username() + "@email.com";
        signUp(requesterEmail);

        driver.get("http://localhost:8081/friends");
        driver.findElement(By.cssSelector("form[action='/friends/add'] button[type='submit']")).click();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            int notificationCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notifications WHERE user_id = ?", Integer.class, recipientId);
            assertEquals(1, notificationCount);
        });
        String message = jdbcTemplate.queryForObject(
                "SELECT message FROM notifications WHERE user_id = ?", String.class, recipientId);
        assertTrue(message.contains("sent you a friend request"));
    }

    @Test
    public void notificationsAreListedMostRecentFirst() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        jdbcTemplate.update(
                "INSERT INTO notifications (user_id, message, link, read, created_at) VALUES (?, ?, ?, false, ?)",
                myId, "oldest notification", "/posts/1", Timestamp.valueOf("2026-01-01 09:00:00"));
        jdbcTemplate.update(
                "INSERT INTO notifications (user_id, message, link, read, created_at) VALUES (?, ?, ?, false, ?)",
                myId, "middle notification", "/posts/2", Timestamp.valueOf("2026-01-02 09:00:00"));
        jdbcTemplate.update(
                "INSERT INTO notifications (user_id, message, link, read, created_at) VALUES (?, ?, ?, false, ?)",
                myId, "newest notification", "/posts/3", Timestamp.valueOf("2026-01-03 09:00:00"));

        driver.get("http://localhost:8081/notifications");
        String pageText = driver.findElement(By.tagName("body")).getText();

        int newestIndex = pageText.indexOf("newest notification");
        int middleIndex = pageText.indexOf("middle notification");
        int oldestIndex = pageText.indexOf("oldest notification");

        assertTrue(newestIndex >= 0 && middleIndex >= 0 && oldestIndex >= 0);
        assertTrue(newestIndex < middleIndex);
        assertTrue(middleIndex < oldestIndex);
    }


}
