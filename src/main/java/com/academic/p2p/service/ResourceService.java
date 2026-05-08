package com.academic.p2p.service;

import com.academic.p2p.dto.ResourceDTO;
import com.academic.p2p.model.Resource;
import com.academic.p2p.model.User;
import com.academic.p2p.repository.ResourceRepository;
import com.academic.p2p.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResourceService {
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private IPFSService ipfsService;
    
    @Autowired
    private ProofOfContributionService pocService;
    
    @Transactional
    public Resource uploadResource(ResourceDTO resourceDTO, MultipartFile file, Long uploaderId) throws IOException {
        User uploader = userRepository.findById(uploaderId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Upload to IPFS
        String ipfsHash = ipfsService.uploadFile(file);
        ipfsService.pinFile(ipfsHash); // Pin to ensure persistence
        
        // Create resource entity
        Resource resource = new Resource();
        resource.setTitle(resourceDTO.getTitle());
        resource.setDescription(resourceDTO.getDescription());
        resource.setIpfsHash(ipfsHash);
        resource.setFileName(file.getOriginalFilename());
        resource.setFileType(file.getContentType());
        resource.setFileSize(file.getSize());
        resource.setResourceType(resourceDTO.getResourceType());
        resource.setCategory(resourceDTO.getCategory());
        resource.setTags(resourceDTO.getTags());
        resource.setCourseCode(resourceDTO.getCourseCode());
        resource.setInstitution(resourceDTO.getInstitution());
        resource.setUploader(uploader);
        resource.setVerificationStatus(Resource.VerificationStatus.PENDING);
        
        Resource savedResource = resourceRepository.save(resource);
        
        // Reward user for upload
        pocService.rewardUpload(uploader, savedResource);
        
        return savedResource;
    }
    
    public byte[] downloadResource(Long resourceId) throws IOException {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new RuntimeException("Resource not found"));
        
        resource.incrementDownloads();
        resourceRepository.save(resource);
        
        return ipfsService.downloadFile(resource.getIpfsHash());
    }
    
    public Resource getResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new RuntimeException("Resource not found"));
        
        resource.incrementViews();
        return resourceRepository.save(resource);
    }
    
    public List<Resource> getUserResources(Long userId) {
        return resourceRepository.findByUploaderId(userId);
    }
    
    public Page<Resource> searchResources(String keyword, int page, int size) {
        // Route the old simple search into our new advanced filter system!
        return searchAndFilterResources(keyword, null, null, "newest", false, page, size);
    }
    
    public List<Resource> getTopQualityResources(int limit) {
        return resourceRepository.findTopQualityResources(PageRequest.of(0, limit));
    }
    
    public List<Resource> getMostDownloadedResources(int limit) {
        return resourceRepository.findMostDownloadedResources(PageRequest.of(0, limit));
    }
    
    @Transactional
    public void verifyResource(Long resourceId, Long verifierId) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new RuntimeException("Resource not found"));
        User verifier = userRepository.findById(verifierId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (resource.getVerificationStatus() == Resource.VerificationStatus.PENDING) {
            pocService.rewardVerification(verifier, resource);
        }
    }
    
    @Transactional
    public void rejectResource(Long resourceId, String reason) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new RuntimeException("Resource not found"));
        
        resource.setVerificationStatus(Resource.VerificationStatus.REJECTED);
        resourceRepository.save(resource);
    }
    
    public long getTotalVerifiedResources() {
        return resourceRepository.countVerifiedResources();
    }
    public Page<Resource> searchAndFilterResources(String keyword, String typeStr, String categoryStr, String sort, boolean verified, int page, int size) {
        // Handle the "Sort By" dropdown
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "uploadedAt");
        if ("quality".equals(sort) || "rating".equals(sort)) {
            sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "qualityScore");
        } else if ("downloads".equals(sort)) {
            sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "downloadCount");
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        // Safely convert strings to Enums
        Resource.ResourceType type = (typeStr != null && !typeStr.isEmpty()) ? Resource.ResourceType.valueOf(typeStr) : null;
        Resource.ResourceCategory category = (categoryStr != null && !categoryStr.isEmpty()) ? Resource.ResourceCategory.valueOf(categoryStr) : null;
        String searchParam = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;

        return resourceRepository.filterResources(searchParam, type, category, verified, pageable);
    }
}