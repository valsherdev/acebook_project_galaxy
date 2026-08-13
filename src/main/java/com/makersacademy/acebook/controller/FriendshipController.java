package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friendship;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.FriendshipRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FriendshipController {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/friends")
    public String index(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("currentUserId", currentUser.getId());

        List<Long> acceptedFriendIds = friendshipRepository.findAcceptedFriendIdsByUserId(currentUser.getId());
        List<User> friends = new ArrayList<>();
        for (Long id : acceptedFriendIds) {
            friends.add(userRepository.findById(id).orElseThrow());
        }

        List<Friendship> incomingRequests = friendshipRepository.findIncomingPendingRequests(currentUser.getId());

        List<User> allUsers = (List<User>) userRepository.findAll();
        List<User> suggestions = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getId().equals(currentUser.getId())) continue;
            if (acceptedFriendIds.contains(user.getId())) continue;
            boolean pending = friendshipRepository.findBetweenUsers(currentUser.getId(), user.getId()).isPresent();
            if (pending) continue;
            suggestions.add(user);
        }

        model.addAttribute("friends", friends);
        model.addAttribute("incomingRequests", incomingRequests);
        model.addAttribute("suggestions", suggestions);
        return "friends/index";
    }

    @PostMapping("/friends/add")
    public String addFriend(@RequestParam Long currentUserId, @RequestParam Long friendId) {

        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        User friendUser = userRepository.findById(friendId).orElseThrow();

        if (!currentUser.getId().equals(friendUser.getId())) {
            boolean alreadyFriends = friendshipRepository.existsByUserAndFriend(currentUser, friendUser);

            if (!alreadyFriends) {
                Friendship friendship = new Friendship(currentUser, friendUser, "ACCEPTED");
                friendshipRepository.save(friendship);
            }
        }
        return "redirect:/posts";
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
