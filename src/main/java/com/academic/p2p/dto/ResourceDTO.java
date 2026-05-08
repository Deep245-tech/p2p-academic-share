package com.academic.p2p.dto;

import com.academic.p2p.model.Resource.ResourceType;
import com.academic.p2p.model.Resource.ResourceCategory;
import lombok.Data;
import java.util.Set;

@Data
public class ResourceDTO {
    private String title;
    private String description;
    private ResourceType resourceType;
    private ResourceCategory category;
    private Set<String> tags;
    private String courseCode;
    private String institution;
}