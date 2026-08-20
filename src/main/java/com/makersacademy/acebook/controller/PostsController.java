package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.*;
import com.makersacademy.acebook.repository.CommentRepository;
import com.makersacademy.acebook.repository.FriendshipRepository;
import com.makersacademy.acebook.repository.LikeRepository;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.UserRepository;
import com.makersacademy.acebook.service.ImageService;
import com.makersacademy.acebook.model.Comment;
import com.makersacademy.acebook.model.Like;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.model.Profile;
import com.makersacademy.acebook.repository.*;
import com.makersacademy.acebook.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayOutputStream;

import java.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

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

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    private ImageService imageService;

    @GetMapping("/posts")
    public String index(@RequestParam(name = "filter", defaultValue = "all") String filter, Model model) {
        User currentUser = getCurrentUser();

        model.addAttribute("currentUserId", currentUser.getId());

        List<Long> friendIds = friendshipRepository.findAcceptedFriendIdsByUserId(currentUser.getId());
        model.addAttribute("currentUserFriendIds", friendIds);

        Profile profile = profileRepository.findByUser(currentUser).orElse(null);
        model.addAttribute("profile", profile);

        List<Friendship> outgoingPending = friendshipRepository.findOutgoingPendingRequests(currentUser.getId());
        List<Long> pendingIds = new ArrayList<>();
        for (Friendship friendship : outgoingPending) pendingIds.add(friendship.getFriend().getId());
        model.addAttribute("currentUserPendingIds", pendingIds);

        Iterable<Post> allPosts = repository.findAllByOrderByCreatedAtDesc();

        List<Post> posts = new ArrayList<>();
        for (Post post : allPosts) {
            boolean isFriendsPost = post.getUser() != null && friendIds.contains(post.getUser().getId());
            if ("friends".equals(filter)) {
                if (isFriendsPost) posts.add(post);
            } else {
                posts.add(post);
            }
        }
        model.addAttribute("posts", posts);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("post", new Post());

        Map<Long, List<Comment>> recentCommentsByPost = new HashMap<>();
        for (Post post : posts) {
            List<Comment> recent = commentRepository.findTop3ByPostIdOrderByCreatedAtDesc(post.getId());
            Collections.reverse(recent);
            recentCommentsByPost.put(post.getId(), recent);
        }
        model.addAttribute("recentCommentsByPost", recentCommentsByPost);
        return "posts/index";
    }

    @PostMapping("/posts")
    public RedirectView create(
            @ModelAttribute Post post,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles
    ) throws IOException {

        boolean hasContent =
                post.getContent() != null &&
                        !post.getContent().isBlank();

        boolean hasImage = false;

        List<String> encodedImages = new ArrayList<>();

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {

                if (file != null && !file.isEmpty()) {

                    hasImage = true;

                    byte[] compressedImage =
                            imageService.compressImage(file.getBytes());

                    String encodedImage =
                            java.util.Base64.getEncoder()
                                    .encodeToString(compressedImage);

                    encodedImages.add(encodedImage);
                }
            }
        }

        if (!hasContent && !hasImage) {
            return new RedirectView("/posts");
        }

        if (hasImage) {
            post.setImages(String.join(",", encodedImages));
        }

        User currentUser = getCurrentUser();
        post.setUser(currentUser);

        repository.save(post);

        return new RedirectView("/posts");
    }

    @PostMapping("/posts/{postId}/comments")
    public RedirectView createComment(@PathVariable Long postId, @RequestParam String content, HttpServletRequest request) {
        Post post = repository.findById(postId).orElseThrow();
        User currentUser = getCurrentUser();
        commentRepository.save(new Comment(content, post, currentUser));

        if (post.getUser() != null && !post.getUser().getId().equals(currentUser.getId())) {
            notificationRepository.save(new Notification(
                    post.getUser(),
                    currentUser.getName() + " commented on your post",
                    "/posts/" + post.getId()
            ));
        }
        String currentUrl = request.getHeader("Referer");
        String baseUrl = (currentUrl != null && !currentUrl.isEmpty()) ? currentUrl : "/posts";
        return new RedirectView(baseUrl + "#post-" + postId);
    }

    @GetMapping("/posts/{id}")
    public String show(@PathVariable Long id, Model model) {
        Post post = repository.findById(id).orElseThrow();
        model.addAttribute("post", post);

        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentUserId", currentUser.getId());

        List<Friendship> outgoingPending = friendshipRepository.findOutgoingPendingRequests(currentUser.getId());
        List<Long> pendingIds = new ArrayList<>();
        for (Friendship friendship : outgoingPending) {
            pendingIds.add(friendship.getFriend().getId());
        }
        model.addAttribute("currentUserPendingIds", pendingIds);

        List<Long> friendIds = friendshipRepository.findAcceptedFriendIdsByUserId(currentUser.getId());
        model.addAttribute("currentUserFriendIds", friendIds);

        return "posts/show";
    }

    @GetMapping("/posts/{postId}/images/{imageIndex}")
    @ResponseBody
    public ResponseEntity<byte[]> getPostImage(
            @PathVariable Long postId,
            @PathVariable int imageIndex) {

        Post post = repository.findById(postId).orElseThrow();

        List<String> images = post.getConvertedImages();

        if (imageIndex < 0 || imageIndex >= images.size()) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = Base64.getDecoder().decode(images.get(imageIndex));

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageBytes);
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
    public RedirectView toggleLike(@PathVariable Long postId, HttpServletRequest request) {
        Post post = repository.findById(postId).orElseThrow();
        User currentUser = getCurrentUser();
        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, currentUser.getId());
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            likeRepository.save(new Like(post, currentUser));

            if (post.getUser() != null && !post.getUser().getId().equals(currentUser.getId())) {
                notificationRepository.save(new Notification(
                        post.getUser(),
                        currentUser.getName() + " liked your post",
                        "/posts/" + post.getId()
                ));
            }
        }
        String currentUrl = request.getHeader("Referer");
        String baseUrl = (currentUrl != null && !currentUrl.isEmpty()) ? currentUrl : "/posts";
        return new RedirectView(baseUrl + "#post-" + postId);
    }

    private User getCurrentUser() {
        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String username = (String) principal.getAttributes().get("email");
        return userRepository.findUserByUsername(username).orElseThrow();
    }

    private byte[] compressImage(MultipartFile file) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .size(1200, 1200)
                .outputFormat("jpg")
                .outputQuality(0.7)
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    @GetMapping("/games/snake")
    public String snake() {
        return "forward:/games/snake/snake.html";
    }

}
