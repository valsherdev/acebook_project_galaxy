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

@Controller
public class ProfileController {

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/profile")
    public ModelAndView getProfile() {
        Profile profile = getOrCreateProfileOfCurrentUser();
        ModelAndView modelAndView = new ModelAndView("profile");
        modelAndView.addObject("profile", profile);
        return modelAndView;
    }

    @GetMapping("/profile/edit")
    public ModelAndView getEditProfile() {
        Profile profile = getOrCreateProfileOfCurrentUser();
        ModelAndView modelAndView = new ModelAndView("profile-edit");
        modelAndView.addObject("profile", profile);
        return modelAndView;
    }


    @PostMapping("/profile/edit")
    public String updateProfileDetails(@ModelAttribute("profile") Profile profileForm) {
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

        return "redirect:/profile";

    }

    private Profile getOrCreateProfileOfCurrentUser() {
        User currentUser = getCurrentUser();
        return profileRepository.findByUser(getCurrentUser()).orElseGet(() -> {
            Profile newProfile = new Profile();
            newProfile.setUser(getCurrentUser());
            return newProfile;
        });
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