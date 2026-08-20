package com.makersacademy.acebook.feature;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SearchProfilesFeatureTest {

    WebDriver driver;
    Faker faker;
    WebDriverWait wait;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

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
        jdbcTemplate.update("DELETE FROM profiles");
        jdbcTemplate.update("DELETE FROM users");
    }


    @Test
    public void userSearchesProfilesAndFindsFilledInProfile() {
        String myEmail = faker.name().username() + "@email.com";

        driver.get("http://localhost:" + port + "/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(myEmail);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));

        String otherUsername = faker.name().username() + "@email.com";

        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", otherUsername);

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                Long.class,
                otherUsername
        );

        jdbcTemplate.update("INSERT INTO profiles (first_name, last_name, current_location, hometown, about_me, user_id, profile_picture) " +
                                                        "VALUES ('Joe', 'Bloggs', 'London', 'London', 'About me', ?, null)", userId);


        driver.findElement(By.name("query")).sendKeys("Joe");
        driver.findElement(By.cssSelector("form.search-box button")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Search results for profiles:')]")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Joe Bloggs"));
    }

    @Test
    public void userSearchesProfilesAndFindsEmailAddressNonFilledInFirstNameAndLastNameInProfile() {
        String myEmail = faker.name().username() + "@email.com";

        driver.get("http://localhost:" + port + "/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(myEmail);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));

        String otherUsername = "jane.johnson@email.com";

        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", otherUsername);

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                Long.class,
                otherUsername
        );

        jdbcTemplate.update("INSERT INTO profiles (first_name, last_name, current_location, hometown, about_me, user_id, profile_picture) " +
                "VALUES (null, null, 'London', 'London', 'About me', ?, null)", userId);


        driver.findElement(By.name("query")).sendKeys("Jane");
        driver.findElement(By.cssSelector("form.search-box button")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Search results for email address:')]")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("jane.johnson@email.com"));
    }

    @Test
    public void userSearchesProfilesAndFindsEmailAddressNonFilledInFirstNameAndLastNameInProfileAndOtherUsersWithSearchQuery() {
        String myEmail = faker.name().username() + "@email.com";

        driver.get("http://localhost:" + port + "/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(myEmail);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));

        String otherUsername = "jane.johnson@email.com";
        String anotherUsername = "janesmith@email.com";

        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", otherUsername);
        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", anotherUsername);

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                Long.class,
                otherUsername
        );

        Long anotherUserId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                Long.class,
                anotherUsername
        );

        jdbcTemplate.update("INSERT INTO profiles (first_name, last_name, current_location, hometown, about_me, user_id, profile_picture) " +
                "VALUES ('Jane', 'Johnson', 'London', 'London', 'About me', ?, null)", userId);

        jdbcTemplate.update("INSERT INTO profiles (first_name, last_name, current_location, hometown, about_me, user_id, profile_picture) " +
                "VALUES (null, null, 'London', 'London', 'About me', ?, null)", anotherUserId);


        driver.findElement(By.name("query")).sendKeys("Jane");
        driver.findElement(By.cssSelector("form.search-box button")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Search results for email address:')]")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Jane Johnson"));
        assertTrue(pageText.contains("janesmith@email.com"));
    }

    @Test
    public void userSearchesEmptyStringAndReturnsNothingFoundMessage() {
        String myEmail = faker.name().username() + "@email.com";

        driver.get("http://localhost:" + port + "/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(myEmail);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));

        driver.findElement(By.name("query")).sendKeys("feature");
        driver.findElement(By.cssSelector("form.search-box button")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(),'No profiles or email addresses found matching')]")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("No profiles or email addresses found matching"));
        assertTrue(pageText.contains("feature"));

    }

    @Test
    public void userSearchesProfilesAndFindsEmailAddressNonFilledInFirstNameAndLastNameInProfileAndOtherUsersWithSearchQueryWithLeadingWhitespace() {
        String myEmail = faker.name().username() + "@email.com";

        driver.get("http://localhost:" + port + "/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(myEmail);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));

        String otherUsername = "jane.johnson@email.com";
        String anotherUsername = "janesmith@email.com";

        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", otherUsername);
        jdbcTemplate.update("INSERT INTO users (username, enabled) VALUES (?, true)", anotherUsername);

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                Long.class,
                otherUsername
        );

        Long anotherUserId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                Long.class,
                anotherUsername
        );

        jdbcTemplate.update("INSERT INTO profiles (first_name, last_name, current_location, hometown, about_me, user_id, profile_picture) " +
                "VALUES ('Jane', 'Johnson', 'London', 'London', 'About me', ?, null)", userId);

        jdbcTemplate.update("INSERT INTO profiles (first_name, last_name, current_location, hometown, about_me, user_id, profile_picture) " +
                "VALUES (null, null, 'London', 'London', 'About me', ?, null)", anotherUserId);


        driver.findElement(By.name("query")).sendKeys(" Jane");
        driver.findElement(By.cssSelector("form.search-box button")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Search results for email address:')]")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Jane Johnson"));
        assertTrue(pageText.contains("janesmith@email.com"));
    }

    @Test
    public void userClicksSearchWithoutTypingBringsUpRequiredMessage() {
        String myEmail = faker.name().username() + "@email.com";

        driver.get("http://localhost:" + port + "/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(myEmail);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));

        WebElement searchInput = driver.findElement(By.xpath("//html/body/div[1]/nav/form/input"));

        driver.findElement(By.cssSelector("form.search-box button")).click();

        JavascriptExecutor js = (JavascriptExecutor) driver;

        Boolean isInvalid = (Boolean) js.executeScript("return !arguments[0].checkValidity();", searchInput);
        assertTrue(isInvalid, "Expected search input to fail form validation");

        String validationMessage = (String) js.executeScript("return arguments[0].validationMessage;", searchInput);
        assertEquals("Please fill in this field.", validationMessage);

    }
}
