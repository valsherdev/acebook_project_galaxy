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

import java.time.Duration;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class CommentFeatureTest {

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

    private Long insertPost(Long authorId, String content) {
        jdbcTemplate.update(
                "INSERT INTO posts (user_id, content, images, created_at) VALUES (?, ?, NULL, NOW())",
                authorId, content);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM posts WHERE user_id = ? AND content = ?",
                Long.class, authorId, content);
    }

    private void insertComment(Long postId, Long userId, String content, Timestamp createdAt) {
        jdbcTemplate.update(
                "INSERT INTO comments (post_id, user_id, content, created_at) VALUES (?, ?, ?, ?)",
                postId, userId, content, createdAt);
    }

    private Long currentUserId(String email) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, email);
    }

    private int commentCountFor(Long postId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE post_id = ?", Integer.class, postId);
    }


    @Test
    public void signedInUserCanCommentOnAPost() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "A new post");

        String commenterEmail = faker.name().username() + "@email.com";
        signUp(commenterEmail);

        driver.get("http://localhost:8081/posts");
        WebElement commentBox = driver.findElement(
                By.cssSelector("form[action='/posts/" + postId + "/comments'] input[name='content']"));
        commentBox.sendKeys("This is a great post!");
        commentBox.submit();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertEquals(1, commentCountFor(postId)));
    }


    @Test
    public void commentTextAppearsOnThePageAfterSubmitting() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "A new post");

        String commenterEmail = faker.name().username() + "@email.com";
        signUp(commenterEmail);

        driver.get("http://localhost:8081/posts");
        WebElement commentBox = driver.findElement(
                By.cssSelector("form[action='/posts/" + postId + "/comments'] input[name='content']"));
        commentBox.sendKeys("A unique comment");
        commentBox.submit();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), "A unique comment"));

        assertTrue(driver.findElement(By.tagName("body")).getText()
                .contains("A unique comment"));
    }


    @Test
    public void commentCountIncreasesAfterCommenting() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "A new post");

        String commenterEmail = faker.name().username() + "@email.com";
        signUp(commenterEmail);

        driver.get("http://localhost:8081/posts");
        String beforeText = driver.findElement(By.tagName("body")).getText();
        assertTrue(beforeText.contains("Comments (0)"));

        WebElement commentBox = driver.findElement(
                By.cssSelector("form[action='/posts/" + postId + "/comments'] input[name='content']"));
        commentBox.sendKeys("New comment");
        commentBox.submit();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Comments (1)"));
        assertTrue(driver.findElement(By.tagName("body")).getText().contains("Comments (1)"));
    }


    @Test
    public void feedPreviewShowsOnlyThreeMostRecentComments() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "Post with more than three comments");

        Long commenterId = insertUser(faker.name().username() + "@email.com");
        insertComment(postId, commenterId, "oldest comment", Timestamp.valueOf("2026-01-01 09:00:00"));
        insertComment(postId, commenterId, "second comment", Timestamp.valueOf("2026-01-02 09:00:00"));
        insertComment(postId, commenterId, "third comment", Timestamp.valueOf("2026-01-03 09:00:00"));
        insertComment(postId, commenterId, "newest comment", Timestamp.valueOf("2026-01-04 09:00:00"));

        String viewerEmail = faker.name().username() + "@email.com";
        signUp(viewerEmail);

        driver.get("http://localhost:8081/posts");
        String pageText = driver.findElement(By.tagName("body")).getText();

        // The oldest should be excluded from the 3-comment preview:
        assertFalse(pageText.contains("oldest comment"));
        assertTrue(pageText.contains("second comment"));
        assertTrue(pageText.contains("third comment"));
        assertTrue(pageText.contains("newest comment"));
    }

    @Test
    public void feedPreviewShowsRecentCommentsOldestToNewest() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "New post");

        Long commenterId = insertUser(faker.name().username() + "@email.com");
        insertComment(postId, commenterId, "alpha comment", Timestamp.valueOf("2026-01-01 09:00:00"));
        insertComment(postId, commenterId, "beta comment", Timestamp.valueOf("2026-01-02 09:00:00"));
        insertComment(postId, commenterId, "gamma comment", Timestamp.valueOf("2026-01-03 09:00:00"));

        String viewerEmail = faker.name().username() + "@email.com";
        signUp(viewerEmail);

        driver.get("http://localhost:8081/posts");
        String pageText = driver.findElement(By.tagName("body")).getText();

        int alphaIndex = pageText.indexOf("alpha comment");
        int betaIndex = pageText.indexOf("beta comment");
        int gammaIndex = pageText.indexOf("gamma comment");

        assertTrue(alphaIndex >= 0 && betaIndex >= 0 && gammaIndex >= 0);
        assertTrue(alphaIndex < betaIndex);
        assertTrue(betaIndex < gammaIndex);
    }


    @Test
    public void viewAllCommentsLinkAppearsOnlyWhenMoreThanThreeComments() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "Post with exactly three comments");

        Long commenterId = insertUser(faker.name().username() + "@email.com");
        insertComment(postId, commenterId, "one", Timestamp.valueOf("2026-01-01 09:00:00"));
        insertComment(postId, commenterId, "two", Timestamp.valueOf("2026-01-02 09:00:00"));
        insertComment(postId, commenterId, "three", Timestamp.valueOf("2026-01-03 09:00:00"));

        String viewerEmail = faker.name().username() + "@email.com";
        signUp(viewerEmail);

        driver.get("http://localhost:8081/posts");
        assertFalse(driver.findElement(By.tagName("body")).getText().contains("View all"));
    }

    @Test
    public void viewAllCommentsLinkNavigatesToFullPostPage() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "Post with four comments");

        Long commenterId = insertUser(faker.name().username() + "@email.com");
        insertComment(postId, commenterId, "first of four", Timestamp.valueOf("2026-01-01 09:00:00"));
        insertComment(postId, commenterId, "second of four", Timestamp.valueOf("2026-01-02 09:00:00"));
        insertComment(postId, commenterId, "third of four", Timestamp.valueOf("2026-01-03 09:00:00"));
        insertComment(postId, commenterId, "fourth of four", Timestamp.valueOf("2026-01-04 09:00:00"));

        String viewerEmail = faker.name().username() + "@email.com";
        signUp(viewerEmail);

        driver.get("http://localhost:8081/posts");
        assertTrue(driver.findElement(By.tagName("body")).getText().contains("Comments (4)"));
        driver.findElement(By.partialLinkText("View all")).click();

        wait.until(ExpectedConditions.urlContains("/posts/" + postId));
        String pageText = driver.findElement(By.tagName("body")).getText();

        assertTrue(pageText.contains("first of four"));
        assertTrue(pageText.contains("fourth of four"));
    }

    @Test
    public void commentFromAnotherUserDisplaysTheirUsername() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "A new post");

        String otherCommenterUsername = faker.name().username() + "@email.com";
        Long otherCommenterId = insertUser(otherCommenterUsername);
        insertComment(postId, otherCommenterId, "a comment from a user with no profile", Timestamp.valueOf("2026-01-01 09:00:00"));

        String viewerEmail = faker.name().username() + "@email.com";
        signUp(viewerEmail);

        driver.get("http://localhost:8081/posts");
        String pageText = driver.findElement(By.tagName("body")).getText();

        assertTrue(pageText.contains(otherCommenterUsername));
        assertTrue(pageText.contains("a comment from a user with no profile"));
    }

    @Test
    public void fullPostPageListsAllCommentsOldestToNewest() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "A new post");

        Long commenterId = insertUser(faker.name().username() + "@email.com");
        insertComment(postId, commenterId, "first comment", Timestamp.valueOf("2026-01-01 09:00:00"));
        insertComment(postId, commenterId, "second comment", Timestamp.valueOf("2026-01-02 09:00:00"));
        insertComment(postId, commenterId, "third comment", Timestamp.valueOf("2026-01-03 09:00:00"));
        insertComment(postId, commenterId, "fourth comment", Timestamp.valueOf("2026-01-04 09:00:00"));
        insertComment(postId, commenterId, "most recent comment", Timestamp.valueOf("2026-01-05 09:00:00"));

        String viewerEmail = faker.name().username() + "@email.com";
        signUp(viewerEmail);

        driver.get("http://localhost:8081/posts/" + postId);
        String pageText = driver.findElement(By.tagName("body")).getText();


        assertTrue(pageText.contains("first comment"));
        assertTrue(pageText.contains("most recent comment"));

        int firstIndex = pageText.indexOf("first comment");
        int secondIndex = pageText.indexOf("second comment");
        int thirdIndex = pageText.indexOf("third comment");
        int fourthIndex = pageText.indexOf("fourth comment");
        int fifthIndex = pageText.indexOf("most recent comment");

        assertTrue(firstIndex < secondIndex);
        assertTrue(secondIndex < thirdIndex);
        assertTrue(thirdIndex < fourthIndex);
        assertTrue(fourthIndex < fifthIndex);
    }

}
