package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Profile;
import com.makersacademy.acebook.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProfileRepository extends CrudRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);

    Optional<Profile> findByUser(User user);

    Optional<Profile> getAllByFirstName(User userId);
}
