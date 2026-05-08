package com.academic.p2p.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenBalanceDTO {
    
    private Long id;
    private String username;
    
    // These need to match the builder calls in UserService perfectly
    private Integer currentBalance; 
    private Integer totalEarned;    
    
    private Double reputationScore;
    private Integer verifiedUploads;
    private Integer totalUploads;
    private Integer totalReviews;
}