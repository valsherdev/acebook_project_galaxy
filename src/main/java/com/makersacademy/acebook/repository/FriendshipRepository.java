package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Friendship;
import com.makersacademy.acebook.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends CrudRepository<Friendship, Long> {
    boolean existsByUserAndFriend(User user, User friend);

    @Query("SELECT friendship.friend.id FROM Friendship AS friendship WHERE friendship.user.id = :userId AND friendship.status = 'ACCEPTED'")
    List<Long> findFriendIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN friendship.user.id = :userId THEN friendship.friend.id ELSE friendship.user.id END " +
            "FROM Friendship AS friendship " +
            "WHERE (friendship.user.id = :userId OR friendship.friend.id = :userId) AND friendship.status = 'ACCEPTED'")
    List<Long> findAcceptedFriendIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT friendship FROM Friendship AS friendship WHERE friendship.user.id = :userId AND friendship.status = 'PENDING'")
    List<Friendship> findOutgoingPendingRequests(@Param("userId") Long userId);

    @Query("SELECT friendship FROM Friendship AS friendship WHERE friendship.friend.id = :userId AND friendship.status = 'PENDING'")
    List<Friendship> findIncomingPendingRequests(@Param("userId") Long userId);

    @Query("SELECT friendship FROM Friendship AS friendship WHERE (friendship.user.id = :id1 AND friendship.friend.id = :id2) OR (friendship.user.id = :id2 AND friendship.friend.id = :id1)")
    Optional<Friendship> findBetweenUsers(@Param("id1") Long id1, @Param("id2") Long id2);

    @Query("SELECT friendship FROM Friendship friendship WHERE " +
            "(friendship.user.id = :u1 AND friendship.friend.id = :u2 OR " +
            " friendship.user.id = :u2 AND friendship.friend.id = :u1) " +
            "AND friendship.status = 'ACCEPTED'")
    Optional<Friendship> findAcceptedFriendship(@Param("u1") Long userId, @Param("u2") Long friendId);
}
