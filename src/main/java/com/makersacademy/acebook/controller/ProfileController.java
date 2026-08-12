package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Profile;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.ProfileRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ProfileController {

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/profile/")
    public ModelAndView getProfile() {
        return new ModelAndView("profile");
    }


    @PostMapping("/profile/update/")
    public RedirectView updateProfileDetails(@ModelAttribute Profile profileForm) {
        User currentUser = getCurrentUser();

        Profile profile = profileRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    Profile newProfile = new Profile();
                    newProfile.setUser(currentUser);
                    return newProfile;
                });

        profile.setFirstName(profileForm.getFirstName());
        profile.setLastName(profileForm.getLastName());
        profile.setCurrentLocation(profileForm.getCurrentLocation());
        profile.setHometown(profileForm.getHometown());
        profile.setAboutMe(profileForm.getAboutMe());
        profileRepository.save(profile);

        return new RedirectView("profile");

    }


    private User getCurrentUser() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String username = (String) principal.getAttributes().get("email");
        return userRepository.findUserByUsername(username).orElseThrow();
    }
}