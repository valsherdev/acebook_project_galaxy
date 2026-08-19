package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Profile;
import com.makersacademy.acebook.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProfileRepositoryTest {


    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    public void testSearchForNameReturnsAListOfMatchingNames() {
        User testUser1 = new User();
        User testUser2 = new User();
        User testUser3 = new User();
        User testUser4 = new User();
        testUser1.setUsername("joe.bloggs@test.com");
        testUser1.setEnabled(true);
        testUser2.setUsername("tom.davis@test.com");
        testUser2.setEnabled(true);
        testUser3.setUsername("samantha.cotton@test.com");
        testUser3.setEnabled(true);
        testUser4.setUsername("joe.smith@test.com");
        testUser4.setEnabled(true);
        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testUser3 = userRepository.save(testUser3);
        testUser4 = userRepository.save(testUser4);

        byte[] dummyBytes = new byte[0];
        profileRepository.saveAll(List.of(
                new Profile(null, "Joe", "Bloggs", "London", "London", dummyBytes, "I am a test", testUser1),
                new Profile(null, "Tom", "Davis", "Southampton", "Newcastle", dummyBytes, "I am a test", testUser2),
                new Profile(null, "Samantha", "Cotton", "Southampton", "Newcastle", dummyBytes, "I am a test", testUser3),
                new Profile(null, "Joe", "Smith", "Liverpool", "Newcastle", dummyBytes, "I am a test", testUser4)
        ));

        List<Profile> results = profileRepository.findByFirstNameContainingIgnoreCase("joe");

        assertThat(results)
                .hasSize(2)
                .extracting(Profile::getFirstName)
                .containsOnly("Joe");
    }

    @Test
    public void testSearchForPartialNameReturnsAListOfMatchingNames() {
        User testUser1 = new User();
        User testUser2 = new User();
        User testUser3 = new User();
        User testUser4 = new User();
        testUser1.setUsername("joe.bloggs@test.com");
        testUser1.setEnabled(true);
        testUser2.setUsername("tom.davis@test.com");
        testUser2.setEnabled(true);
        testUser3.setUsername("samantha.cotton@test.com");
        testUser3.setEnabled(true);
        testUser4.setUsername("joe.smith@test.com");
        testUser4.setEnabled(true);
        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testUser3 = userRepository.save(testUser3);
        testUser4 = userRepository.save(testUser4);

        byte[] dummyBytes = new byte[0];
        profileRepository.saveAll(List.of(
                new Profile(null, "Joe", "Bloggs", "London", "London", dummyBytes, "I am a test", testUser1),
                new Profile(null, "Tom", "Davis", "Southampton", "Newcastle", dummyBytes, "I am a test", testUser2),
                new Profile(null, "Samantha", "Cotton", "Southampton", "Newcastle", dummyBytes, "I am a test", testUser3),
                new Profile(null, "Joe", "Smith", "Liverpool", "Newcastle", dummyBytes, "I am a test", testUser4)
        ));

        List<Profile> results = profileRepository.findByFirstNameContainingIgnoreCase("sama");

        assertThat(results)
                .hasSize(1)
                .extracting(Profile::getFirstName)
                .containsOnly("Samantha");
    }

}
