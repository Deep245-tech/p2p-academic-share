package com.academic.p2p.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_transactions")
public class TokenTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private Integer amount;
    
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    private TransactionReason transactionReason;
    
    private Long resourceId;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private String description;
    private LocalDateTime createdAt;
    
    public enum TransactionType {
        CREDIT, DEBIT
    }
    
    public enum TransactionReason {
        INITIAL_CREDIT,
        UPLOAD_REWARD,
        VERIFICATION_REWARD,
        QUALITY_BONUS,
        DOWNLOAD_PURCHASE,
        REFUND,
        PENALTY
    }
    
    // Constructors
    public TokenTransaction() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { 
        this.transactionType = transactionType; 
    }
    
    public TransactionReason getTransactionReason() { return transactionReason; }
    public void setTransactionReason(TransactionReason transactionReason) { 
        this.transactionReason = transactionReason; 
    }
    
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    
    public Integer getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(Integer balanceBefore) { this.balanceBefore = balanceBefore; }
    
    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}