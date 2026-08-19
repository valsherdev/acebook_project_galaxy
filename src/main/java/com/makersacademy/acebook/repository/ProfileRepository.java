package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Profile;
import com.makersacademy.acebook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);

    Optional<Profile> findByUser(User user);

    List<Profile> findByFirstNameIgnoreCase(String firstName);

    List<Profile> findByFirstNameContainingIgnoreCase(String firstName);
}
