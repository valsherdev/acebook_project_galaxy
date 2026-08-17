package com.makersacademy.acebook.model;

import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<Comment> comments;

    @OneToMany(mappedBy = "post")
    private List<Like> likes;

    @Column(columnDefinition = "LONGTEXT")
    private String images;

    public List<String> getConvertedImages() {
        List<String> convertedImages = new ArrayList<>();
        if (this.images != null && !this.images.isBlank()) {
            for (String img : this.images.split(",")) {
                if (!img.isBlank()) {
                    convertedImages.add(img);
                }
            }
        } return convertedImages;
    }

    public Post() {}

    public Post(String content, User user, String images) {

        this.content = content;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.images = images;
    }

}
