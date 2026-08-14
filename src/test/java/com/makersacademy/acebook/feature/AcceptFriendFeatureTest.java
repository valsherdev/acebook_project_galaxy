package com.makersacademy.acebook.feature;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

        // USER 2 signs up
        driver2.get("http://localhost:8081/");
        driver2.findElement(By.linkText("Sign up")).click();
        driver2.findElement(By.name("email")).sendKeys(user2Email);
        driver2.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver2.findElement(By.name("action")).click();

        // USER 2 goes to Friends
        driver2.findElement(By.linkText("Friends")).click();

        // USER 2 specifically sends a friend request to USER 1
        WebElement user1Suggestion = driver2.findElement(
                By.xpath("//div[span[text()='" + user1Email + "']]")
        );

        user1Suggestion.findElement(
                By.cssSelector("form[action='/friends/add'] button[type='submit']")
        ).click();

        // USER 1 goes to Friends
        driver1.get("http://localhost:8081/friends");

        // USER 1 specifically accepts USER 2's request
        WebElement user2Request = driver1.findElement(
                By.xpath("//div[span[text()='" + user2Email + "']]")
        );

        user2Request.findElement(
                By.cssSelector("form[action*='/accept'] button[type='submit']")
        ).click();

        // USER 1 can see USER 2 as a friend
        String user1FriendsPage =
                driver1.findElement(By.tagName("body")).getText();

        assertTrue(user1FriendsPage.contains(user2Email));

        // USER 2 can see USER 1 as a friend
        driver2.get("http://localhost:8081/friends");

        String user2FriendsPage =
                driver2.findElement(By.tagName("body")).getText();

        assertTrue(user2FriendsPage.contains(user1Email));
    }

}
