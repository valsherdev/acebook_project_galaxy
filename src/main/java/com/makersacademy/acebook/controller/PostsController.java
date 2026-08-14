package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.*;
import com.makersacademy.acebook.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class PostsController {

    @Autowired
    PostRepository repository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    FriendshipRepository friendshipRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @GetMapping("/posts")
    public String index(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("currentUserId", currentUser.getId());

        List<Long> friendIds = friendshipRepository.findAcceptedFriendIdsByUserId(currentUser.getId());
        model.addAttribute("currentUserFriendIds", friendIds);

        List<Friendship> outgoingPending = friendshipRepository.findOutgoingPendingRequests(currentUser.getId());
        List<Long> pendingIds = new ArrayList<>();
        for (Friendship friendship : outgoingPending) pendingIds.add(friendship.getFriend().getId());
        model.addAttribute("currentUserPendingIds", pendingIds);

        Iterable<Post> posts = repository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("posts", posts);
        model.addAttribute("post", new Post());
        return "posts/index";
    }

    @PostMapping("/posts")
    public RedirectView create(@ModelAttribute Post post) {
        if (post.getContent() == null || post.getContent().isBlank()) {
            return new RedirectView("/posts");
        }
        User currentUser = getCurrentUser();
        post.setUser(currentUser);
        repository.save(post);
        return new RedirectView("/posts");
    }

    @PostMapping("/posts/{postId}/comments")
    public RedirectView createComment(@PathVariable Long postId, @RequestParam String content) {
        Post post = repository.findById(postId).orElseThrow();
        User currentUser = getCurrentUser();
        commentRepository.save(new Comment(content, post, currentUser));

        if (post.getUser() != null && !post.getUser().getId().equals(currentUser.getId())) {
            notificationRepository.save(new Notification(
                    post.getUser(),
                    currentUser.getUsername() + " commented on your post",
                    "/posts/" + post.getId()
            ));
        }
        return new RedirectView("/posts");
    }

    @GetMapping("/posts/{id}")
    public String show(@PathVariable Long id, Model model) {
        Post post = repository.findById(id).orElseThrow();
        model.addAttribute("post", post);
        return "posts/show";
    }

    @PostMapping("/posts/{postId}/delete")
    public RedirectView delete(@PathVariable Long postId) {
        Post post = repository.findById(postId).orElseThrow();
        User currentUser = getCurrentUser();
        if (post.getUser().getId().equals(currentUser.getId())) {
            repository.delete(post);
        }
        return new RedirectView("/posts");
    }

    @PostMapping("/posts/{postId}/likes")
    public RedirectView toggleLike(@PathVariable Long postId) {
        Post post = repository.findById(postId).orElseThrow();
        User currentUser = getCurrentUser();
        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, currentUser.getId());
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            likeRepository.save(new Like(post, currentUser));
        }
        return new RedirectView("/posts");
    }

    private User getCurrentUser() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String username = (String) principal.getAttributes().get("email");
        return userRepository.findUserByUsername(username).orElseThrow();
    }

    @GetMapping("/games/snake")
    public String snake() {
        return "forward:/games/snake/snake.html";
    }

}
