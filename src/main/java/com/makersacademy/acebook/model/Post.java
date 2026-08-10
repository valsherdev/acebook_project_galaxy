package com.makersacademy.acebook.model;

import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "POSTS")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;

    public Post() {}

    public Post(String content, User user) {

        this.content = content;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

}
