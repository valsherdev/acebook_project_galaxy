package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Friendship;
import com.makersacademy.acebook.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipRepository extends CrudRepository<Friendship, Long> {
    boolean existsByUserAndFriend(User user, User friend);

    @Query("SELECT friendship.friend.id FROM Friendship AS friendship WHERE friendship.user.id = :userId")
    List<Long> findFriendIdsByUserId(@Param("userId") Long userId);
}
