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
    WebDriverWait wait;

    @BeforeEach
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        driver = new ChromeDriver();
        faker = new Faker();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // creates an account, and if it doesn't log in automatically, it will log in manually right after
    // - this was causing some of the tests to fail because it wasn't logging the test in after creating the account so
    // it couldn't find the next steps specified on the tests
    private void signUpAndLogInUser(String email, String password) {
        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.name("action")).click();

        // manual log in, if not done automatically
        if (driver.getCurrentUrl().contains("/login")) {
            driver.findElement(By.name("email")).sendKeys(email);
            driver.findElement(By.name("password")).sendKeys(password);
            driver.findElement(By.tagName("button")).click();
        }
    }

    // User can see an empty profile page when they go onto their profile before filling anything out
    @Test
    public void testGoingToProfilePageRendersEmptyProfile() {
        String email = faker.name().username() + "@email.com";

        signUpAndLogInUser(email, "P@55qw0rd");

        driver.get("http://localhost:8081/posts");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("content")));

        driver.get("http://localhost:8081/profile");
        wait.until(ExpectedConditions.urlToBe("http://localhost:8081/profile"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "First name:"));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("First name:"));
    }

    // First name saved successfully to profile
    @Test
    public void testUpdatingProfileSavesFirstNameAndRendersOnProfilePage() {
        String email = faker.name().username() + "@email.com";
        String firstName = faker.name().firstName();

        signUpAndLogInUser(email, "P@55qw0rd");

        driver.get("http://localhost:8081/profile");
        driver.get("http://localhost:8081/profile/edit");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));

        driver.findElement(By.name("firstName")).sendKeys(firstName);
        driver.findElement(By.tagName("button")).click();

        driver.get("http://localhost:8081/profile");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), firstName));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("First name: " + firstName));
    }

    // First and last name saved successfully to profile
    @Test
    public void testUpdatingProfileSavesFirstAndLastNameAndRendersOnProfilePage() {
        String email = faker.name().username() + "@email.com";
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        signUpAndLogInUser(email, "P@55qw0rd");

        driver.get("http://localhost:8081/profile");
        driver.get("http://localhost:8081/profile/edit");

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "First name:"));

        driver.findElement(By.name("firstName")).sendKeys(firstName);
        driver.findElement(By.name("lastName")).sendKeys(lastName);
        driver.findElement(By.tagName("button")).click();

        driver.get("http://localhost:8081/profile");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), firstName));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("First name: " + firstName));
        assertTrue(pageText.contains("Last name: " + lastName));
    }

    // First and last name saved successfully to profile, full name shows on users individual post
    @Test
    public void testUpdatingProfileSavesFirstAndLastNameAndRendersOnPostPage() {
        String email = faker.name().username() + "@email.com";
        String newPost = faker.lorem().sentence();
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        signUpAndLogInUser(email, "P@55qw0rd");

        driver.get("http://localhost:8081/posts");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("content")));

        driver.findElement(By.name("content")).sendKeys(newPost);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        driver.get("http://localhost:8081/profile");
        driver.get("http://localhost:8081/profile/edit");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));

        driver.findElement(By.name("firstName")).sendKeys(firstName);
        driver.findElement(By.name("lastName")).sendKeys(lastName);
        driver.findElement(By.tagName("button")).click();

        driver.get("http://localhost:8081/profile");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("First name: " + firstName));
        assertTrue(pageText.contains("Last name: " + lastName));

        driver.get("http://localhost:8081/posts");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), firstName));

        String pageBodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageBodyText.contains(firstName));
        assertTrue(pageBodyText.contains(lastName));
    }

    // Only last name saved on profile, posts show users email on post (as full name is incomplete)
    @Test
    public void testUpdatingOnlyLastNameRendersOnPostPageAsEmailAndNotCompleteName() {
        String email = faker.name().username() + "@email.com";
        String newPost = faker.lorem().sentence();
        String lastName = faker.name().lastName();

        signUpAndLogInUser(email, "P@55qw0rd");

        driver.get("http://localhost:8081/profile");
        driver.get("http://localhost:8081/profile/edit");

        System.out.println("FAILED ON URL: " + driver.getCurrentUrl());
        System.out.println("PAGE SOURCE: " + driver.getPageSource());

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("lastName")));

        driver.findElement(By.name("lastName")).sendKeys(lastName);
        driver.findElement(By.tagName("button")).click();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/profile/edit")));

        driver.get("http://localhost:8081/posts");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("content"))).sendKeys(newPost);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), email));
        assertThat(driver.findElement(By.tagName("body")).getText(), containsString(email));

    }

    // Only first name saved on profile, posts show users email on post (as full name is incomplete)
    @Test
    public void testUpdatingOnlyFirstNameRendersOnPostPageAsEmailAndNotCompleteName() {
        String email = faker.name().username() + "@email.com";
        String newPost = faker.lorem().sentence();
        String firstName = faker.name().firstName();

        signUpAndLogInUser(email, "P@55qw0rd");

        driver.get("http://localhost:8081/posts");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("content")));

        driver.findElement(By.name("content")).sendKeys(newPost);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        driver.get("http://localhost:8081/profile");
        driver.get("http://localhost:8081/profile/edit");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));

        driver.findElement(By.name("firstName")).sendKeys(firstName);
        driver.findElement(By.tagName("button")).click();

        driver.get("http://localhost:8081/profile");

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("First name: " + firstName));

        driver.get("http://localhost:8081/posts");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), email));

        String pageBodyText = driver.findElement(By.tagName("body")).getText();
        assertThat(pageBodyText, containsString(email));

    }

    // When a user uploads a profile picture, it saves and shows on their profile page
    @Test
    public void testUploadingAnImageRendersCorrectlyOnProfilePage() {
        String email = faker.name().username() + "@email.com";

        signUpAndLogInUser(email, "P@55qw0rd");

        driver.get("http://localhost:8081/profile");
        driver.get("http://localhost:8081/profile/edit");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("imageFile")));

        String imagePath = java.nio.file.Paths.get("src/test/java/com/makersacademy/acebook/resources/test-image.jpeg").toAbsolutePath().toString();
        driver.findElement(By.name("imageFile")).sendKeys(imagePath);

        driver.findElement(By.tagName("button")).click();
        driver.get("http://localhost:8081/profile");

        WebElement profileImage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("img.profile-pic-placeholder")));

        String imgSrc = profileImage.getAttribute("src");
        assertTrue(imgSrc.contains("data:image/") || imgSrc.contains("http"));
        }

    // When a user makes a post and also uploads a profile picture, their profile picture will show on their
    // individual posts
    @Test
    public void testAddingAPostAndUploadingAProfilePictureRendersCorrectlyOnPostPage() {
        String email = faker.name().username() + "@email.com";
        String newPost = faker.lorem().sentence();

        signUpAndLogInUser(email, "P@55qw0rd");

        driver.get("http://localhost:8081/posts");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("content")));

        driver.findElement(By.name("content")).sendKeys(newPost);
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        driver.get("http://localhost:8081/profile");
        driver.get("http://localhost:8081/profile/edit");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("imageFile")));

        String imagePath = java.nio.file.Paths.get("src/test/java/com/makersacademy/acebook/resources/test-image.jpeg").toAbsolutePath().toString();
        driver.findElement(By.name("imageFile")).sendKeys(imagePath);
        driver.findElement(By.tagName("button")).click();
        driver.get("http://localhost:8081/profile");

        WebElement profileImage = driver.findElement(By.cssSelector("img.profile-pic-placeholder"));
        assertThat(profileImage.getAttribute("src"), containsString("data:image/"));

        driver.get("http://localhost:8081/posts");

        String pageBodyText = driver.findElement(By.tagName("body")).getText();
        assertThat(pageBodyText, containsString(email));

        WebElement postProfileImage = driver.findElement(By.cssSelector("li.post-box img.profile-pic-placeholder"));

        String postImgSrc = postProfileImage.getAttribute("src");
        assertTrue(postImgSrc.contains("data:image/") || postImgSrc.contains("http"));
    }

}