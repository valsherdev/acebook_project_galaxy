package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friendship;
import com.makersacademy.acebook.model.Profile;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.repository.FriendshipRepository;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.ProfileRepository;
import com.makersacademy.acebook.repository.UserRepository;
import com.makersacademy.acebook.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import net.coobird.thumbnailator.Thumbnails;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import org.springframework.web.servlet.view.RedirectView;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

@Controller
public class ProfileController {

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;
    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private ImageService imageService;

    @GetMapping("/profile")
    public ModelAndView getProfile() {
        Profile profile = getOrCreateProfileOfCurrentUser();

        List<Post> posts =
                postRepository.findAllByUserIdOrderByCreatedAtDesc(profile.getUser().getId());

        ModelAndView modelAndView = new ModelAndView("profiles/profile");
        User currentUser = getCurrentUser();
        modelAndView.addObject("profile", profile);
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("currentUser", currentUser);
        modelAndView.addObject("currentUserId", currentUser.getId());

        addFriendshipAttributesToModel(modelAndView, currentUser);

        return modelAndView;
    }

    @GetMapping("/profile/edit")
    public ModelAndView getEditProfile() {
        Profile profile = getOrCreateProfileOfCurrentUser();
        ModelAndView modelAndView = new ModelAndView("profiles/profile-edit");
        modelAndView.addObject("profile", profile);
        return modelAndView;
    }


    @PostMapping("/profile/edit")
    public String updateProfileDetails(@ModelAttribute("profile") Profile profileForm,
                                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        User currentUser = getCurrentUser();
        Profile profile = profileRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    Profile newProfile = new Profile();
                    newProfile.setUser(currentUser);
                    return newProfile;
                });

        if (imageFile != null && !imageFile.isEmpty()) {
            byte[] compressedImage =
                    imageService.compressImage(imageFile.getBytes());

            profile.setProfilePicture(compressedImage);
        }

        profile.setFirstName(profileForm.getFirstName());
        profile.setLastName(profileForm.getLastName());
        profile.setCurrentLocation(profileForm.getCurrentLocation());
        profile.setHometown(profileForm.getHometown());
        profile.setAboutMe(profileForm.getAboutMe());
        profileRepository.save(profile);

        return "redirect:/profile";

    }

    @GetMapping("/profile/{userId}")
    public ModelAndView getUserProfile(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        Profile profile = profileRepository.findByUser(user)
                .orElseGet(() -> {
                    Profile newProfile = new Profile();
                    newProfile.setUser(user);
                    return profileRepository.save(newProfile);
                });

        List<Post> posts =
                postRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        ModelAndView modelAndView = new ModelAndView("profiles/profile");
        User currentUser = getCurrentUser();
        modelAndView.addObject("profile", profile);
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("currentUser", currentUser);
        modelAndView.addObject("currentUserId", currentUser.getId());

        addFriendshipAttributesToModel(modelAndView, currentUser);

        return modelAndView;
    }

    @GetMapping("/profile/{userId}/image")
    @ResponseBody
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        Profile profile = profileRepository.findByUser(user).orElseThrow();

        byte[] image = profile.getProfilePicture();

        if (image == null || image.length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }

    @GetMapping("/profiles/search")
    public ModelAndView searchProfiles(@RequestParam(name = "query", required = false) String query) {
        ModelAndView modelAndView = new ModelAndView("profiles/search");
        List<Profile> matchingProfiles;
        List<User> matchingUsernames;

        if (query != null && !query.trim().isEmpty()) {
            matchingProfiles = profileRepository.findByFirstNameContainingIgnoreCase(query.trim());
        } else {
            matchingProfiles = Collections.emptyList();
        }

        if (query != null && !query.trim().isEmpty()) {
            matchingUsernames = userRepository.findByUsernameContainingIgnoreCase(query.trim());
        } else {
            matchingUsernames = Collections.emptyList();
        }


        modelAndView.addObject("profiles", matchingProfiles);
        modelAndView.addObject("users", matchingUsernames);
        modelAndView.addObject("searchQuery", query);


        return modelAndView;
    }



    private void addFriendshipAttributesToModel(ModelAndView modelAndView, User currentUser) {
        List<Friendship> outgoingPending = friendshipRepository.findOutgoingPendingRequests(currentUser.getId());
        List<Long> pendingIds = new ArrayList<>();
        for (Friendship friendship : outgoingPending) {
            pendingIds.add(friendship.getFriend().getId());
        }
        modelAndView.addObject("currentUserPendingIds", pendingIds);

        List<Long> friendIds = friendshipRepository.findAcceptedFriendIdsByUserId(currentUser.getId());
        modelAndView.addObject("currentUserFriendIds", friendIds);
    }

    private Profile getOrCreateProfileOfCurrentUser() {
        User currentUser = getCurrentUser();
        return profileRepository.findByUser(getCurrentUser()).orElseGet(() -> {
            Profile newProfile = new Profile();
            newProfile.setUser(getCurrentUser());
            return profileRepository.save(newProfile);
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