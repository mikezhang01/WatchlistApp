package com.cinestack.reviews;

import com.cinestack.common.Visibility;
import com.cinestack.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByUser_IdAndMovie_Id(UUID userId, UUID movieId);

    List<Review> findByMovie_IdAndVisibilityOrderByCreatedAtDesc(UUID movieId, Visibility visibility);

    List<Review> findByUserOrderByCreatedAtDesc(User user);

    List<Review> findByUser_IdAndVisibilityOrderByCreatedAtDesc(UUID userId, Visibility visibility);
}
