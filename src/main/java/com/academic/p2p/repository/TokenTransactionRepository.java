package com.academic.p2p.repository;

import com.academic.p2p.model.TokenTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, Long> {
    List<TokenTransaction> findByUserId(Long userId);
    List<TokenTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT SUM(t.amount) FROM TokenTransaction t WHERE t.user.id = :userId " +
           "AND t.transactionType = 'CREDIT'")
    Integer calculateTotalEarned(@Param("userId") Long userId);
    
    @Query("SELECT t FROM TokenTransaction t WHERE t.createdAt BETWEEN :start AND :end")
    List<TokenTransaction> findTransactionsInPeriod(@Param("start") LocalDateTime start, 
                                                     @Param("end") LocalDateTime end);
}