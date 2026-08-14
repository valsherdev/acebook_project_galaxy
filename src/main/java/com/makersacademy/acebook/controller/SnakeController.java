package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Snake;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.SnakeRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/games/snake")
public class SnakeController {

    @Autowired
    SnakeRepository scoreRepository;

    @Autowired
    UserRepository userRepository;

    private User getCurrentUser() {
        DefaultOidcUser principal =
                (DefaultOidcUser) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        String username =
                (String) principal
                        .getAttributes()
                        .get("email");

        return userRepository
                .findUserByUsername(username)
                .orElseThrow();
    }

    @PostMapping("/scores")
    public ResponseEntity<Void> saveScore(
            @RequestBody ScoreRequest request
    ) {
        if (
                request == null ||
                        request.score() < 0 ||
                        request.score() > 100000
        ) {
            return ResponseEntity.badRequest().build();
        }

        User currentUser = getCurrentUser();

        scoreRepository.save(
                new Snake(
                        currentUser.getUsername(),
                        request.score()
                )
        );

        return ResponseEntity.ok().build();
    }


    @GetMapping("/scores")
    public List<ScoreResponse> getScores(
            @RequestParam(defaultValue = "10")
            int limit
    ) {

        int safeLimit =
                Math.max(
                        1,
                        Math.min(limit, 100)
                );


        return scoreRepository
                .findAllByOrderByScoreDescCreatedAtAsc(
                        PageRequest.of(0, safeLimit)
                )
                .stream()
                .map(score ->
                        new ScoreResponse(
                                score.getUsername(),
                                score.getScore()
                        )
                )
                .toList();
    }


    public record ScoreRequest(int score) {}

    public record ScoreResponse(
            String username,
            int score
    ) {}
}