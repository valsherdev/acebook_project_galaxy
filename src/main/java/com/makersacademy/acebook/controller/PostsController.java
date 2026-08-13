package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Comment;
import com.makersacademy.acebook.model.Like;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.CommentRepository;
import com.makersacademy.acebook.repository.FriendshipRepository;
import com.makersacademy.acebook.repository.LikeRepository;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

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

    @GetMapping("/posts")
    public String index(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("currentUserId", currentUser.getId());

        List<Long> friendIds = friendshipRepository.findFriendIdsByUserId(currentUser.getId());
        model.addAttribute("currentUserFriendIds", friendIds);

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
}
