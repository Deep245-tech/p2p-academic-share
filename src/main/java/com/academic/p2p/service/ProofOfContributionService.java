package com.academic.p2p.service;

import com.academic.p2p.model.*;
import com.academic.p2p.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProofOfContributionService {
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private TokenTransactionRepository tokenTransactionRepository;
    
    @Autowired
    private UserService userService;
    
    @Value("${poc.upload-reward}")
    private int uploadReward;
    
    @Value("${poc.verification-reward}")
    private int verificationReward;
    
    @Value("${poc.quality-bonus}")
    private int qualityBonus;
    
    @Value("${poc.initial-tokens}")
    private int initialTokens;
    
    @Transactional
    public void rewardUpload(User user, Resource resource) {
        // Base upload reward
        userService.addTokens(user.getId(), uploadReward, 
            TokenTransaction.TransactionReason.UPLOAD_REWARD, 
            resource.getId(), "Reward for uploading: " + resource.getTitle());
        
        // Update user stats
        user.setTotalUploads(user.getTotalUploads() + 1);
        userRepository.save(user);
    }
    
    @Transactional
    public void rewardVerification(User verifier, Resource resource) {
        userService.addTokens(verifier.getId(), verificationReward,
            TokenTransaction.TransactionReason.VERIFICATION_REWARD,
            resource.getId(), "Reward for verifying: " + resource.getTitle());
        
        resource.setVerifiedBy(verifier);
        resource.setVerifiedAt(LocalDateTime.now());
        resource.setVerificationStatus(Resource.VerificationStatus.VERIFIED);
        resourceRepository.save(resource);
        
        // Update uploader stats
        User uploader = resource.getUploader();
        uploader.setVerifiedUploads(uploader.getVerifiedUploads() + 1);
        userRepository.save(uploader);
    }
    
    @Transactional
    public void rewardQualityContent(Resource resource) {
        if (resource.getQualityScore() >= 4.0) {
            User uploader = resource.getUploader();
            userService.addTokens(uploader.getId(), qualityBonus,
                TokenTransaction.TransactionReason.QUALITY_BONUS,
                resource.getId(), "Quality bonus for: " + resource.getTitle());
        }
    }
    
    @Transactional
    public void calculateResourceQuality(Resource resource) {
        Double avgRating = reviewRepository.calculateAverageRating(resource.getId());
        long helpfulReviews = reviewRepository.findByResourceId(resource.getId())
            .stream().filter(Review::getIsHelpful).count();
        
        // Quality score algorithm: 50% rating + 30% download count + 20% helpful reviews
        double downloadScore = Math.min(resource.getDownloadCount() / 100.0, 5.0);
        double reviewScore = Math.min(helpfulReviews * 0.5, 5.0);
        double ratingScore = avgRating != null ? avgRating : 3.0;
        
        double qualityScore = (ratingScore * 0.5) + (downloadScore * 0.3) + (reviewScore * 0.2);
        resource.setQualityScore(Math.min(5.0, qualityScore));
        resourceRepository.save(resource);
        
        // Reward if quality is high
        if (qualityScore >= 4.0 && resource.getVerificationStatus() == Resource.VerificationStatus.VERIFIED) {
            rewardQualityContent(resource);
        }
    }
    
    @Transactional
    public void updateUserReputation(User user) {
        double uploadScore = Math.min(user.getVerifiedUploads() * 0.5, 2.5);
        double helpfulReviews = reviewRepository.countHelpfulReviewsReceived(user.getId()) * 0.1;
        double reputationScore = 1.0 + uploadScore + Math.min(helpfulReviews, 2.5);
        
        user.updateReputation(reputationScore);
        userRepository.save(user);
    }
    
    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void scheduledQualityCalculation() {
        List<Resource> verifiedResources = resourceRepository.findByVerificationStatus(
            Resource.VerificationStatus.VERIFIED);
        
        for (Resource resource : verifiedResources) {
            calculateResourceQuality(resource);
        }
        
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            updateUserReputation(user);
        }
    }
    
    public double calculateContributionScore(User user) {
        double score = 0.0;
        
        // Upload contribution
        score += user.getVerifiedUploads() * 10;
        
        // Review contribution
        score += user.getTotalReviews() * 2;
        
        // Token balance (engagement indicator)
        score += user.getTokenBalance() * 0.1;
        
        // Reputation bonus
        score *= user.getReputationScore();
        
        return score;
    }
}