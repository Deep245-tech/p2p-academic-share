package com.academic.p2p.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private Integer tokenBalance = 100;
    private Double reputationScore = 0.0;
    private Integer totalUploads = 0;
    private Integer verifiedUploads = 0;
    private Integer totalReviews = 0;
    private String institution;
    
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.STUDENT;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "uploader")
    private Set<Resource> uploadedResources = new HashSet<>();
    
    @OneToMany(mappedBy = "user")
    private Set<TokenTransaction> transactions = new HashSet<>();
    
    public enum UserRole {
        STUDENT, VERIFIER, ADMIN
    }
    
    // Constructors
    public User() {}
    public String getInstitution() {
    return institution;
}

public void setInstitution(String institution) {
    this.institution = institution;
}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Integer getTokenBalance() { return tokenBalance; }
    public void setTokenBalance(Integer tokenBalance) { this.tokenBalance = tokenBalance; }
    
    public Double getReputationScore() { return reputationScore; }
    public void setReputationScore(Double reputationScore) { 
        this.reputationScore = reputationScore; 
    }
    
    public Integer getTotalUploads() { return totalUploads; }
    public void setTotalUploads(Integer totalUploads) { this.totalUploads = totalUploads; }
    
    public Integer getVerifiedUploads() { return verifiedUploads; }
    public void setVerifiedUploads(Integer verifiedUploads) { 
        this.verifiedUploads = verifiedUploads; 
    }
    
    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Set<Resource> getUploadedResources() { return uploadedResources; }
    public void setUploadedResources(Set<Resource> uploadedResources) { 
        this.uploadedResources = uploadedResources; 
    }
    
    public Set<TokenTransaction> getTransactions() { return transactions; }
    public void setTransactions(Set<TokenTransaction> transactions) { 
        this.transactions = transactions; 
    }
    
    // Helper methods
    public void addTokens(int amount) { 
        this.tokenBalance += amount; 
    }
    
    public boolean deductTokens(int amount) { 
        if (this.tokenBalance >= amount) {
            this.tokenBalance -= amount;
            return true;
        }
        return false;
    }
    
    public void updateReputation(double delta) { 
        this.reputationScore = Math.max(0, Math.min(100, this.reputationScore + delta));
    }
    
    public void incrementUploads() { 
        this.totalUploads++; 
    }
    
    public void incrementVerifiedUploads() { 
        this.verifiedUploads++; 
    }
    
    public void incrementReviews() { 
        this.totalReviews++; 
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}