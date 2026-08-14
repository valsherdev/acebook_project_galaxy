package com.makersacademy.acebook.feature;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AcceptFriendFeatureTest {

    WebDriver driver1;
    WebDriver driver2;
    Faker faker;

    @BeforeEach
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        driver1 = new ChromeDriver();
        driver2 = new ChromeDriver();
        driver1.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver2.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        faker = new Faker();
    }

    @AfterEach
    public void tearDown() {
        driver1.close();
        driver2.close();
    }

    @Disabled
    @Test
    public void acceptedRequestShowsBothUsersAsFriends() {
        String user1Email = faker.name().username() + "@email.com";
        String user2Email = faker.name().username() + "@email.com";

        driver1.get("http://localhost:8081/");
        driver1.findElement(By.linkText("Sign up")).click();
        driver1.findElement(By.name("email")).sendKeys(user1Email);
        driver1.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver1.findElement(By.name("action")).click();

        driver2.get("http://localhost:8081/");
        driver2.findElement(By.linkText("Sign up")).click();
        driver2.findElement(By.name("email")).sendKeys(user2Email);
        driver2.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver2.findElement(By.name("action")).click();

        driver2.findElement(By.linkText("Friends")).click();
        driver2.findElement(By.cssSelector("form[action*='/friends/add'] button[type='submit']")).click();

        driver1.get("http://localhost:8081/friends");
        driver1.findElement(By.cssSelector("form[action*='/accept'] button[type='submit']")).click();

        String user1FriendsPage = driver1.findElement(By.tagName("body")).getText();
        assertTrue(user1FriendsPage.contains(user2Email));

        driver2.get("http://localhost:8081/friends");
        String userBFriendsPage = driver2.findElement(By.tagName("body")).getText();
        assertTrue(userBFriendsPage.contains(user1Email));
    }

}
