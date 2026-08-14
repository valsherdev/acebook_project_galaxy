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

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class ProfileFeatureTest {
    WebDriver driver;
    Faker faker;

    @BeforeEach
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        driver = new ChromeDriver();
        faker = new Faker();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testGoingToProfilePageRendersEmptyProfile() {
        String email = faker.name().username() + "@email.com";

        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();

        driver.findElement(By.linkText("Profile")).click();
        driver.get("http://localhost:8081/profile");

        driver.findElement(By.xpath("/html/body/div[2]/div/p[2]/strong"));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("First name:"));


    }

    @Test
    public void testUpdatingProfileSavesFirstNameAndRendersOnProfilePage() {
        String email = faker.name().username() + "@email.com";
        String firstName = faker.name().firstName();

        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();

        driver.findElement(By.linkText("Profile")).click();
        driver.get("http://localhost:8081/profile");

        driver.findElement(By.linkText("Edit Profile")).click();
        driver.get("http://localhost:8081/profile/edit");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "First name:"));

        driver.findElement(By.xpath("//*[@id=\"firstName\"]")).click();
        driver.findElement(By.name("firstName")).sendKeys(firstName);
        driver.findElement(By.tagName("button")).click();
        driver.get("http://localhost:8081/profile");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("First name: " + firstName));
    }

    @Test
    public void testUpdatingProfileSavesFirstAndLastNameAndRendersOnProfilePage() {
        String email = faker.name().username() + "@email.com";
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();

        driver.findElement(By.linkText("Profile")).click();
        driver.get("http://localhost:8081/profile");

        driver.findElement(By.linkText("Edit Profile")).click();
        driver.get("http://localhost:8081/profile/edit");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "First name:"));

        driver.findElement(By.xpath("//*[@id=\"firstName\"]")).click();
        driver.findElement(By.name("firstName")).sendKeys(firstName);
        driver.findElement(By.xpath("//*[@id=\"lastName\"]")).click();
        driver.findElement(By.name("lastName")).sendKeys(lastName);
        driver.findElement(By.tagName("button")).click();
        driver.get("http://localhost:8081/profile");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("First name: " + firstName));
        assertTrue(pageText.contains("Last name: " + lastName));
    }

    @Test
    public void testUpdatingProfileSavesFirstAndLastNameAndRendersOnPostPage() {
        String email = faker.name().username() + "@email.com";
        String newPost = faker.lorem().sentence();
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();


        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();

        driver.findElement(By.name("content")).sendKeys(newPost);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        driver.findElement(By.linkText("Profile")).click();
        driver.get("http://localhost:8081/profile");

        driver.findElement(By.linkText("Edit Profile")).click();
        driver.get("http://localhost:8081/profile/edit");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "First name:"));

        driver.findElement(By.xpath("//*[@id=\"firstName\"]")).click();
        driver.findElement(By.name("firstName")).sendKeys(firstName);
        driver.findElement(By.xpath("//*[@id=\"lastName\"]")).click();
        driver.findElement(By.name("lastName")).sendKeys(lastName);
        driver.findElement(By.tagName("button")).click();
        driver.get("http://localhost:8081/profile");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("First name: " + firstName));
        assertTrue(pageText.contains("Last name: " + lastName));

        driver.findElement(By.linkText("Home")).click();
        driver.get("http://localhost:8081/posts");

        String pageBodyText = driver.findElement(By.tagName("body")).getText();
        assertThat(pageBodyText, containsString(firstName + ' ' + lastName));

    }
}