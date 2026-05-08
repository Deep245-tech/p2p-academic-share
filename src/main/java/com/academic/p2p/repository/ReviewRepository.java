package com.academic.p2p.repository;

import com.academic.p2p.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByResourceId(Long resourceId);
    List<Review> findByReviewerId(Long reviewerId);
    Optional<Review> findByResourceIdAndReviewerId(Long resourceId, Long reviewerId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.resource.id = :resourceId")
    Double calculateAverageRating(@Param("resourceId") Long resourceId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.resource.uploader.id = :userId AND r.isHelpful = true")
    long countHelpfulReviewsReceived(@Param("userId") Long userId);
}