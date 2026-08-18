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
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class LikeFeatureTest {
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

    private Long currentUserId(String email) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, email);
    }

    private int likeCountFor(Long postId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE post_id = ?", Integer.class, postId);
    }


    // Tests:

    @Test
    public void signedInUserCanLikeAPost() {
        String email = faker.name().username() + "@email.com";
        String newPost = faker.lorem().sentence();

        signUp(email);

        driver.findElement(By.name("content")).sendKeys(newPost);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        driver.findElement(By.cssSelector("form[action*='/likes'] button")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "1 likes"));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("1 likes"));
    }

    @Test
    public void likeCountStartsAtZero() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        insertPost(ownerId, "New post");

        String email = faker.name().username() + "@email.com";
        signUp(email);

        driver.get("http://localhost:8081/posts");
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("0 likes"));
    }

    @Test
    public void clickingLikeButtonIncrementsLikeCountInDatabase() {
        Long ownerId = insertUser(faker.name().username() + "@email.com");
        Long postId = insertPost(ownerId, "Post to be liked once");

        String likerEmail = faker.name().username() + "@email.com";
        signUp(likerEmail);

        driver.get("http://localhost:8081/posts");
        driver.findElement(By.cssSelector("form[action='/posts/" + postId + "/likes'] button")).click();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertEquals(1, likeCountFor(postId)));
    }

}
