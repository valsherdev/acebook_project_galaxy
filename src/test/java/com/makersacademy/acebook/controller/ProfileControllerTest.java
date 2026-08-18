package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Profile;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.ProfileRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ProfileController profileController;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    public void testSearchProfilesReturnsMatchingResultsInModel() {
        User user = new User();
        user.setUsername("joe.bloggs@test.com");
        user.setEnabled(true);
        userRepository.save(user);

        byte[] dummyBytes = new byte[0];
        Profile profile = new Profile(null, "Joe", "Bloggs", "Nottingham", "Newcastle", dummyBytes, "About me", user);
        profileRepository.save(profile);

        ModelAndView modelAndView = profileController.searchProfiles("Joe");

        assertThat(modelAndView.getViewName()).isEqualTo("profiles/search");

        List<Profile> profilesInModel = (List<Profile>) modelAndView.getModel().get("profiles");
        assertThat(profilesInModel).hasSize(1);
        assertThat(profilesInModel.getFirst().getFirstName()).isEqualTo("Joe");
    }

    @Test
    @WithMockUser(username = "joe.bloggs@test.com")
    public void testGetProfilePageOfAUser() throws Exception {
        User user = new User();
        user.setUsername("joe.bloggs@test.com");
        user.setEnabled(true);
        userRepository.save(user);

        byte[] dummyBytes = new byte[0];
        Profile profile = new Profile(null, "Joe", "Bloggs", "Nottingham", "Newcastle", dummyBytes, "About me", user);
        profileRepository.save(profile);


        mockMvc.perform(get("/profile/" + user.getId())
                        .with(oidcLogin().idToken(token -> token.claim("email", "joe.bloggs@test.com"))))
                        .andExpect(status().isOk())
                        .andExpect(view().name("profiles/profile"))
                        .andExpect(model().attributeExists("profile"))
                        .andExpect(model().attributeExists("posts"));
    }
}
