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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class MessageFeatureTest {

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
        if (driver != null) {
            driver.quit();
        }
        jdbcTemplate.update("DELETE FROM messages");
        jdbcTemplate.update("DELETE FROM notifications");
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM comments");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("DELETE FROM users");
    }


    // helpers:

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

    private void insertMessage(Long senderId, Long recipientId, String content, Timestamp createdAt) {
        jdbcTemplate.update(
                "INSERT INTO messages (sender_id, recipient_id, content, read, created_at) VALUES (?, ?, ?, false, ?)",
                senderId, recipientId, content, createdAt);
    }

    private int messageCountBetween(Long userId1, Long userId2) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?)",
                Integer.class, userId1, userId2, userId2, userId1);
    }

    private int unreadCountFor(Long recipientId, Long senderId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages WHERE recipient_id = ? AND sender_id = ? AND read = false",
                Integer.class, recipientId, senderId);
    }


    // tests:
    @Test
    public void signedInUserCanSendAMessageToOtherUser() {
        Long otherId = insertUser(faker.name().username() + "@email.com");

        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        driver.get("http://localhost:8081/messages/" + otherId);
        driver.findElement(By.cssSelector("form[action='/messages/" + otherId + "'] input[name='content']")).sendKeys("Hello");
        driver.findElement(By.cssSelector("form[action='/messages/" + otherId + "'] button[type='submit']")).click();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertEquals(1, messageCountBetween(myId, otherId)));
    }

    @Test
    public void sentMessageAppearsWithSenderName() {
        Long otherId = insertUser(faker.name().username() + "@email.com");

        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);

        driver.get("http://localhost:8081/messages/" + otherId);
        driver.findElement(By.cssSelector("form[action='/messages/" + otherId + "'] input[name='content']")).sendKeys("Hello");
        driver.findElement(By.cssSelector("form[action='/messages/" + otherId + "'] button[type='submit']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), "Hello"));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Hello"));
        assertTrue(pageText.contains(myEmail));
    }

    @Test
    public void conversationShowsMessagesInChronologicalOrder() {
        Long otherId = insertUser(faker.name().username() + "@email.com");

        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        insertMessage(otherId, myId, "first message", Timestamp.valueOf("2026-01-01 09:00:00"));

        driver.get("http://localhost:8081/messages/" + otherId);
        driver.findElement(By.cssSelector("form[action='/messages/" + otherId + "'] input[name='content']")).sendKeys("second message");
        driver.findElement(By.cssSelector("form[action='/messages/" + otherId + "'] button[type='submit']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "second message"));

        String pageText = driver.findElement(By.tagName("body")).getText();
        int firstIndex = pageText.indexOf("first message");
        int replyIndex = pageText.indexOf("second message");

        assertTrue(firstIndex >= 0 && replyIndex >= 0);
        assertTrue(firstIndex < replyIndex);
    }

    @Test
    public void openingAThreadMarksMessagesAsRead() {
        Long otherId = insertUser(faker.name().username() + "@email.com");

        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        insertMessage(otherId, myId, "an unread message", Timestamp.valueOf("2026-01-01 09:00:00"));
        assertEquals(1, unreadCountFor(myId, otherId));

        driver.get("http://localhost:8081/messages/" + otherId);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertEquals(0, unreadCountFor(myId, otherId)));
    }

    @Test
    public void openingOneThreadDoesNotMarkMessagesFromAnotherSenderAsRead() {
        Long firstSenderId = insertUser(faker.name().username() + "@email.com");
        Long secondSenderId = insertUser(faker.name().username() + "@email.com");

        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);
        Long myId = currentUserId(myEmail);

        insertMessage(firstSenderId, myId, "from first sender", Timestamp.valueOf("2026-01-01 09:00:00"));
        insertMessage(secondSenderId, myId, "from second sender", Timestamp.valueOf("2026-01-01 09:00:00"));

        driver.get("http://localhost:8081/messages/" + firstSenderId);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertEquals(0, unreadCountFor(myId, firstSenderId)));

        assertEquals(1, unreadCountFor(myId, secondSenderId));
    }
}
