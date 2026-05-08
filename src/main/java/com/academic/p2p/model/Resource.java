package com.academic.p2p.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resources")
public class Resource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String ipfsHash;
    
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;
    
    @Enumerated(EnumType.STRING)
    private ResourceCategory category;
    
    @ElementCollection
    private Set<String> tags = new HashSet<>();
    
    private String courseCode;
    private String institution;
    
    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private User uploader;
    
    @ManyToOne
    @JoinColumn(name = "verified_by_id")
    private User verifiedBy;
    
    private LocalDateTime verifiedAt;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    private Integer downloadCount = 0;
    private Integer viewCount = 0;
    private Double qualityScore = 0.0;

    // Connect the Resource to its Reviews
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    private java.util.List<Review> reviews = new java.util.ArrayList<>();

    public java.util.List<Review> getReviews() { 
        return reviews; 
    }
    
    public void setReviews(java.util.List<Review> reviews) { 
        this.reviews = reviews; 
    }
    
    // Enums
    public enum ResourceType {
        PDF, DOCUMENT, VIDEO, AUDIO, IMAGE, OTHER
    }
    
    public enum ResourceCategory {
        LECTURE_NOTES, ASSIGNMENTS, PAST_PAPERS, TEXTBOOKS, RESEARCH_PAPERS, OTHER
    }
    
    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }
    
    // Constructors
    public Resource() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getIpfsHash() { return ipfsHash; }
    public void setIpfsHash(String ipfsHash) { this.ipfsHash = ipfsHash; }
    
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    
    public ResourceCategory getCategory() { return category; }
    public void setCategory(ResourceCategory category) { this.category = category; }
    
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
    
    public User getUploader() { return uploader; }
    public void setUploader(User uploader) { this.uploader = uploader; }
    
    public User getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(User verifiedBy) { this.verifiedBy = verifiedBy; }
    
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { 
        this.verificationStatus = verificationStatus; 
    }
    
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    
    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }
    
    // Helper methods
    public void incrementDownloads() { this.downloadCount++; }
    public void incrementViews() { this.viewCount++; }
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}