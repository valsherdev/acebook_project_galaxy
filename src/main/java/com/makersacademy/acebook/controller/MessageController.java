package com.makersacademy.acebook.controller;


import com.makersacademy.acebook.model.Message;
import com.makersacademy.acebook.model.Profile;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.MessageRepository;
import com.makersacademy.acebook.repository.ProfileRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileRepository profileRepository;

    @GetMapping("/messages")
    public String index(Model model) {
        User currentUser = getCurrentUser();

        List<Message> allMessages = messageRepository.findAllInvolvingUser(currentUser.getId());

        LinkedHashMap<Long, Message> latestMessageByPartner = new LinkedHashMap<>();
        for (Message message: allMessages) {
            boolean iAmSender = message.getSender().getId().equals(currentUser.getId());
            User partner = iAmSender ? message.getRecipient() : message.getSender();
            latestMessageByPartner.putIfAbsent(partner.getId(), message);
        }

        Map<Long, Long> unreadCountByPartnerId = new LinkedHashMap<>();
        for (Long partnerId : latestMessageByPartner.keySet()) {
            unreadCountByPartnerId.put(partnerId, messageRepository.countByRecipientIdAndSenderIdAndReadFalse(currentUser.getId(), partnerId));
        }
        model.addAttribute("conversations", latestMessageByPartner.values());
        model.addAttribute("unreadCountByPartnerId", unreadCountByPartnerId);
        model.addAttribute("currentUserId", currentUser.getId());

        return "messages/index";
    }

    @GetMapping("/messages/{userId}")
    public String show(@PathVariable Long userId, Model model) {
        User currentUser = getCurrentUser();
        User otherUser = userRepository.findById(userId).orElseThrow();

        List<Message> conversation = messageRepository.findConversation(currentUser.getId(), userId);
        messageRepository.markThreadAsRead(currentUser.getId(), userId);

        Profile currentUserProfile = profileRepository.findByUser(currentUser).orElse(null);
        model.addAttribute("currentUserProfile", currentUserProfile);

        Profile otherUserProfile = profileRepository.findByUser(otherUser).orElse(null);
        model.addAttribute("otherUserProfile", otherUserProfile);

        model.addAttribute("otherUser", otherUser);
        model.addAttribute("messages", conversation);
        model.addAttribute("currentUserId", currentUser.getId());

        return "messages/show";
    }

    @PostMapping("/messages/{userId}")
    public RedirectView send(@PathVariable Long userId, @RequestParam String content) {
        if (content == null || content.isBlank()) {
            return new RedirectView("/messages/" + userId);
        }

        User currentUser = getCurrentUser();
        User recipient = userRepository.findById(userId).orElseThrow();

        messageRepository.save(new Message(currentUser, recipient, content));

        return new RedirectView("/messages/" + userId);
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
