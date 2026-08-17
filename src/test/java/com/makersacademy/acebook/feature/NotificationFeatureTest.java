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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private void signUp(String email) {
        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));
    }

    @Test
    public void navBarShowsUnreadCountWhenNotificationExists() {
        String myEmail = faker.name().username() + "@email.com";
        signUp(myEmail);

        Long myId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, myEmail);

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

        Long myId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, myEmail);

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

}
