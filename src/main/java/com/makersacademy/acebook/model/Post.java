package com.makersacademy.acebook.model;

import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "post")
    @OrderBy("createdAt ASC")
    private List<Comment> comments;

    @OneToMany(mappedBy = "post")
    private List<Like> likes;

    private byte[] image;

    public String convertImageByteToString() {
        if (this.image == null) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(this.image);
    }

    public Post() {}

    public Post(String content, User user, byte[] image) {

        this.content = content;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.image = image;
    }

}
