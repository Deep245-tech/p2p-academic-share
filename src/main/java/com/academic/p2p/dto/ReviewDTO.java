package com.academic.p2p.dto;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class ReviewDTO {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    
    private String comment;
    private Boolean isHelpful = false;
}