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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddFriendFeatureTest {

    WebDriver driver;
    Faker faker;

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
    }


    @Disabled
    @Test
    public void userSeesRequestSentAfterAddingFriend() {
        String userAEmail = faker.name().username() + "@email.com";
        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(userAEmail);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();

        driver.findElement(By.name("content")).sendKeys("Hello, this is my post");
        driver.findElement(By.cssSelector(".post-form input[type='submit']")).click();

        driver.findElement(By.linkText("Logout")).click();


        String userBEmail = faker.name().username() + "@email.com";
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(userBEmail);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();

        driver.findElement(By.linkText("Friends")).click();
        driver.findElement(By.cssSelector("form[action*='/friends/add'] button[type='submit']")).click();
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("/friends"));


        driver.get("http://localhost:8081/posts");
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Request sent"));
    }


}
