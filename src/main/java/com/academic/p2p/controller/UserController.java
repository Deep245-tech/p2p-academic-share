package com.academic.p2p.controller;

import com.academic.p2p.dto.ReviewDTO;
import com.academic.p2p.dto.TokenBalanceDTO;
import com.academic.p2p.model.Review;
import com.academic.p2p.model.Resource;
import com.academic.p2p.model.User;
import com.academic.p2p.service.ProofOfContributionService;
import com.academic.p2p.service.ResourceService;
import com.academic.p2p.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private com.academic.p2p.repository.ReviewRepository reviewRepository;

    @Autowired
    private com.academic.p2p.repository.ResourceRepository resourceRepository;

    @Autowired
    private ProofOfContributionService pocService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> registrationRequest) {
        try {
            User user = userService.registerUser(
                registrationRequest.get("username"),
                registrationRequest.get("email"),
                registrationRequest.get("password")
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful! Welcome tokens credited.");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("tokenBalance", user.getTokenBalance());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest, HttpSession session) {
        try {
            User user = userService.getUserByUsername(loginRequest.get("username"));
            
            // In production, use password encoder
            if (user.getPassword().equals(loginRequest.get("password"))) {
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("userId", user.getId());
                response.put("username", user.getUsername());
                response.put("tokenBalance", user.getTokenBalance());
                response.put("reputationScore", user.getReputationScore());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid password");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<TokenBalanceDTO> getTokenBalance(@PathVariable Long userId) {
        TokenBalanceDTO balance = userService.getTokenBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<?> getTransactionHistory(@PathVariable Long userId) {
        var transactions = userService.getTransactionHistory(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{userId}/contribution-score")
    public ResponseEntity<?> getContributionScore(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        double score = pocService.calculateContributionScore(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("contributionScore", score);
        response.put("verifiedUploads", user.getVerifiedUploads());
        response.put("totalUploads", user.getTotalUploads());
        response.put("reputationScore", user.getReputationScore());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-contributors")
    public ResponseEntity<List<User>> getTopContributors() {
        List<User> topContributors = userService.getTopContributors();
        return ResponseEntity.ok(topContributors);
    }

    @PostMapping("/resources/{resourceId}/review")
    public ResponseEntity<?> addReview(@PathVariable Long resourceId, 
                                       @RequestBody Map<String, String> reviewData,
                                       HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("Please login first");
        }

        try {
            User reviewer = userService.getUserById(userId);
            Resource resource = resourceService.getResource(resourceId);

            // Actually create and save the review to the database
            Review review = new Review();
            review.setReviewer(reviewer);
            review.setResource(resource);
            review.setRating(Integer.parseInt(reviewData.get("rating")));
            review.setComment(reviewData.get("comment"));
            review.setIsHelpful(reviewData.containsKey("helpful") && reviewData.get("helpful").equals("on"));
            review.setCreatedAt(java.time.LocalDateTime.now());

            reviewRepository.save(review);

            // Update the resource's quality score so the stars change on the UI
            resource.setQualityScore((double) review.getRating()); 
            resourceRepository.save(resource);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review added successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        
        User user = userService.getUserById(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("tokenBalance", user.getTokenBalance());
        response.put("reputationScore", user.getReputationScore());
        response.put("role", user.getRole());
        
        return ResponseEntity.ok(response);
    }
}