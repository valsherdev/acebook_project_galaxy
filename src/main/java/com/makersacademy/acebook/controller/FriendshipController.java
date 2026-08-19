package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friendship;
import com.makersacademy.acebook.model.Notification;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.FriendshipRepository;
import com.makersacademy.acebook.repository.NotificationRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class FriendshipController {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

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

        List<Friendship> outgoingRequests = friendshipRepository.findOutgoingPendingRequests(currentUser.getId());

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
        model.addAttribute("outgoingRequests", outgoingRequests);
        model.addAttribute("suggestions", suggestions);
        return "friends/index";
    }

    // VS: CHANGE: checks findBetweenUsers (either direction) instead of existsByUserAndFriend (one direction only)
    @PostMapping("/friends/add")
    public RedirectView addFriend(@RequestParam Long currentUserId, @RequestParam Long friendId) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        User friendUser = userRepository.findById(friendId).orElseThrow();

        if (!currentUser.getId().equals(friendUser.getId())) {
            boolean alreadyExists = friendshipRepository.findBetweenUsers(currentUser.getId(), friendUser.getId()).isPresent();
            if (!alreadyExists) {
                Friendship friendship = new Friendship(currentUser, friendUser, "PENDING");
                friendshipRepository.save(friendship);

                notificationRepository.save(new Notification(
                        friendUser,
                        currentUser.getName() + " sent you a friend request",
                        "/friends"
                ));
            }
        }
        return new RedirectView("/friends");
    }

    @PostMapping("/friends/{friendshipId}/accept")
    public RedirectView accept(@PathVariable Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId).orElseThrow();
        friendship.setStatus("ACCEPTED");
        friendshipRepository.save(friendship);
        return new RedirectView("/friends");
    }

    @PostMapping("/friends/{friendshipId}/decline")
    public RedirectView decline(@PathVariable Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId).orElseThrow();
        friendshipRepository.delete(friendship);
        return new RedirectView("/friends");
    }

    @PostMapping("/friends/{friendId}/delete")
    public RedirectView delete(@PathVariable Long friendId) {

        User currentUser = getCurrentUser();

        Friendship friendship = friendshipRepository.findAcceptedFriendship(currentUser.getId(), friendId)
                .orElseThrow(() -> new RuntimeException("Friendship not found!"));

        friendshipRepository.delete(friendship);

        return new RedirectView("/friends");
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
