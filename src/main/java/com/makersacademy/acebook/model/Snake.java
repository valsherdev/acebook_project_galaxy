package com.makersacademy.acebook.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "snake_scores")
public class Snake{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private int score;

    private LocalDateTime createdAt;


    protected Snake() {
    }


    public Snake(
            String username,
            int score
    ) {

        this.username = username;

        this.score = score;

        this.createdAt =
                LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }


    public String getUsername() {
        return username;
    }


    public int getScore() {
        return score;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
