package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friendship;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.FriendshipRepository;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Optional;

@Controller
public class FriendshipController {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/friends/add")
    public String addFriend(@RequestParam Long currentUserId, @RequestParam Long friendId) {

        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        User becomeFriend = userRepository.findById(friendId).orElseThrow();

        if (!currentUser.getId().equals(becomeFriend.getId())) {
            boolean alreadyFriends = friendshipRepository.existsByUserAndFriend(currentUser, becomeFriend);

            if (!alreadyFriends) {
                Friendship friendship = new Friendship(currentUser, becomeFriend, "ACCEPTED");
                friendshipRepository.save(friendship);
            }
        }
        return "redirect:/posts";
    }

}
