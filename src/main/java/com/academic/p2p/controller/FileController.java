package com.academic.p2p.controller;

import com.academic.p2p.dto.ResourceDTO;
import com.academic.p2p.model.Resource;
import com.academic.p2p.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
@RequestMapping("/api/files")
public class FileController {
    
    @Autowired
    private ResourceService resourceService;
    
    @PostMapping("/upload")
    public String uploadFile(@ModelAttribute ResourceDTO resourceDTO,
                            @RequestParam("file") MultipartFile file,
                            HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Resource resource = resourceService.uploadResource(resourceDTO, file, userId);
        return "redirect:/resources/" + resource.getId();
    }
    
    @GetMapping("/download/{resourceId}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long resourceId) throws IOException {
        byte[] data = resourceService.downloadResource(resourceId);
        Resource resource = resourceService.getResource(resourceId);
        
        ByteArrayResource resource_file = new ByteArrayResource(data);
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(resource.getFileType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + resource.getFileName() + "\"")
            .body(resource_file);
    }
    
    @GetMapping("/view/{resourceId}")
    public ResponseEntity<ByteArrayResource> viewFile(@PathVariable Long resourceId) throws IOException {
        byte[] data = resourceService.downloadResource(resourceId);
        Resource resource = resourceService.getResource(resourceId);
        
        ByteArrayResource resource_file = new ByteArrayResource(data);
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(resource.getFileType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + resource.getFileName() + "\"")
            .body(resource_file);
    }
}