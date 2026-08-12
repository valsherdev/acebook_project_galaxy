package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="friendships")
@Getter
@Setter
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    private User friend;

    private String status = "ACCEPTED";

    public Friendship() {}

    public Friendship(User user, User friend, String status) {
        this.user = user;
        this.friend = friend;
        this.status = status;
    }



}
