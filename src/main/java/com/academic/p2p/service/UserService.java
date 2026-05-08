package com.academic.p2p.service;

import com.academic.p2p.dto.TokenBalanceDTO;
import com.academic.p2p.model.TokenTransaction;
import com.academic.p2p.model.User;
import com.academic.p2p.repository.TokenTransactionRepository;
import com.academic.p2p.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TokenTransactionRepository transactionRepository;
    
    @Value("${poc.initial-tokens}")
    private int initialTokens;
    
    @Transactional
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); // In production, use password encoder
        user.setTokenBalance(initialTokens);
        
        User savedUser = userRepository.save(user);
        
        // Record initial token grant
        TokenTransaction transaction = new TokenTransaction();
        transaction.setUser(savedUser);
        transaction.setAmount(initialTokens);
        transaction.setTransactionType(TokenTransaction.TransactionType.CREDIT);
        transaction.setTransactionReason(TokenTransaction.TransactionReason.INITIAL_CREDIT);
        transaction.setBalanceBefore(0);
        transaction.setBalanceAfter(initialTokens);
        transaction.setDescription("Initial welcome tokens");
        transactionRepository.save(transaction);
        
        return savedUser;
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Transactional
    public void addTokens(Long userId, int amount, TokenTransaction.TransactionReason reason, 
                          Long resourceId, String description) {
        User user = getUserById(userId);
        int balanceBefore = user.getTokenBalance();
        user.addTokens(amount);
        userRepository.save(user);
        
        TokenTransaction transaction = new TokenTransaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setTransactionType(TokenTransaction.TransactionType.CREDIT);
        transaction.setTransactionReason(reason);
        transaction.setResourceId(resourceId);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(user.getTokenBalance());
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }
    
    @Transactional
    public boolean deductTokens(Long userId, int amount, TokenTransaction.TransactionReason reason,
                                Long resourceId, String description) {
        User user = getUserById(userId);
        int balanceBefore = user.getTokenBalance();
        boolean success = user.deductTokens(amount);
        
        if (success) {
            userRepository.save(user);
            
            TokenTransaction transaction = new TokenTransaction();
            transaction.setUser(user);
            transaction.setAmount(amount);
            transaction.setTransactionType(TokenTransaction.TransactionType.DEBIT);
            transaction.setTransactionReason(reason);
            transaction.setResourceId(resourceId);
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(user.getTokenBalance());
            transaction.setDescription(description);
            transactionRepository.save(transaction);
        }
        
        return success;
    }
    
    public TokenBalanceDTO getTokenBalance(Long userId) {
        User user = getUserById(userId);
        Integer totalEarned = transactionRepository.calculateTotalEarned(userId);
        
        return TokenBalanceDTO.builder()
            .id(userId) // <-- Fixed: Passes the actual userId parameter instead of "someId"
            .username(user.getUsername())
            .currentBalance(user.getTokenBalance())
            .totalEarned(totalEarned != null ? totalEarned : 0)
            .reputationScore(user.getReputationScore())
            .verifiedUploads(user.getVerifiedUploads())
            .totalUploads(user.getTotalUploads())
            .totalReviews(user.getTotalReviews())
            .build();
    }
    
    public List<TokenTransaction> getTransactionHistory(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<User> getTopContributors() {
        return userRepository.findTopContributors();
    }
}