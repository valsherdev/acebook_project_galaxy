package com.makersacademy.acebook.model;


import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "profiles")

public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String currentLocation;
    private String hometown;

    @Column(name = "profile_picture")
    private byte[] profilePicture;

    @Column(columnDefinition = "TEXT")
    private String aboutMe;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)

    private User user;

    public Profile(String firstName, String lastName, String currentLocation, String hometown, Byte profilePicture, String aboutMe) {
    }

    public String convertImageByteToString() {
        if (this.profilePicture == null) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(this.profilePicture);
    }

}
