package com.makersacademy.acebook.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

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

    public String convertImageByteToString() {
        if (this.profilePicture == null) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(this.profilePicture);
    }

}
