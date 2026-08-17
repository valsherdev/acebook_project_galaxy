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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class FriendshipFeatureTest {

    WebDriver driver;
    Faker faker;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        faker = new Faker();
    }

    @AfterEach
    public void tearDown() {
        driver.close();
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM comments");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("DELETE FROM users");
    }



    @Test
    public void userSeesAddFriendButton() {
        String email = faker.name().username() + "@email.com";

        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));

        String otherUsername = faker.name().username() + "@email.com";
        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", otherUsername);

        driver.get("http://localhost:8081/friends");

        assertFalse(driver.findElements(By.xpath("//button[contains(text(), 'Add Friend')]")).isEmpty());

    }

    @Test
    public void userSeesIncomingFriendRequestAndCanAccept() {
        String myEmail = faker.name().username() + "@email.com";

        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(myEmail);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));

        Long myId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, myEmail);

        String friendUsername = faker.name().username() + "@email.com";
        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", friendUsername);
        Long friendId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, friendUsername);

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

        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(myEmail);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));

        String otherUsername = faker.name().username() + "@email.com";
        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", otherUsername);

        var count = jdbcTemplate.queryForObject("SELECT count(*) FROM users", Integer.class);
        assertEquals(2, count);

        driver.get("http://localhost:8081/friends");
        String beforeSend = driver.findElement(By.tagName("body")).getText();
        assertTrue(beforeSend.contains(otherUsername));

        driver.findElement(By.cssSelector("form[action='/friends/add'] button[type='submit']")).click();

        String afterSend = driver.findElement(By.tagName("body")).getText();

        assertTrue(afterSend.contains("Pending"));
        assertTrue(afterSend.contains(otherUsername));
    }

}
