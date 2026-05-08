package com.academic.p2p.repository;

import com.academic.p2p.model.Resource;
import com.academic.p2p.model.Resource.ResourceType;
import com.academic.p2p.model.Resource.ResourceCategory;
import com.academic.p2p.model.Resource.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByUploaderId(Long uploaderId);
    List<Resource> findByVerificationStatus(VerificationStatus status);
    List<Resource> findByResourceType(ResourceType type);
    List<Resource> findByCategory(ResourceCategory category);
 
    
@Query("SELECT DISTINCT r FROM Resource r LEFT JOIN r.tags t WHERE " +
           "(:keyword IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:type IS NULL OR r.resourceType = :type) " +
           "AND (:category IS NULL OR r.category = :category) " +
           "AND (:verified = false OR r.verificationStatus = 'VERIFIED')")
    Page<Resource> filterResources(@org.springframework.data.repository.query.Param("keyword") String keyword,
                                   @org.springframework.data.repository.query.Param("type") Resource.ResourceType type,
                                   @org.springframework.data.repository.query.Param("category") Resource.ResourceCategory category,
                                   @org.springframework.data.repository.query.Param("verified") boolean verified,
                                   Pageable pageable);
    
    @Query("SELECT r FROM Resource r WHERE r.verificationStatus = 'VERIFIED' " +
           "ORDER BY r.qualityScore DESC")
    List<Resource> findTopQualityResources(Pageable pageable);
    
    @Query("SELECT r FROM Resource r WHERE r.verificationStatus = 'VERIFIED' " +
           "ORDER BY r.downloadCount DESC")
    List<Resource> findMostDownloadedResources(Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM Resource r WHERE r.verificationStatus = 'VERIFIED'")
    long countVerifiedResources();
}