package com.academic.p2p.service;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class IPFSService {
    
    @Autowired
    private IPFS ipfs;
    
    public String uploadFile(MultipartFile file) throws IOException {
        // Convert MultipartFile to File
        File tempFile = convertMultiPartToFile(file);
        try {
            // Add file to IPFS
            NamedStreamable.FileWrapper fileWrapper = new NamedStreamable.FileWrapper(tempFile);
            MerkleNode addResult = ipfs.add(fileWrapper).get(0);
            
            return addResult.hash.toString();
        } finally {
            // Clean up temp file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    public byte[] downloadFile(String hash) throws IOException {
        Multihash filePointer = Multihash.fromBase58(hash);
        return ipfs.cat(filePointer);
    }
    
    public void pinFile(String hash) throws IOException {
        Multihash filePointer = Multihash.fromBase58(hash);
        ipfs.pin.add(filePointer);
    }
    
    public void unpinFile(String hash) throws IOException {
        Multihash filePointer = Multihash.fromBase58(hash);
        ipfs.pin.rm(filePointer);
    }
    
    public String getFileInfo(String hash) throws IOException {
        Multihash filePointer = Multihash.fromBase58(hash);
        return ipfs.object.stat(filePointer).toString();
    }
    
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        Path tempFile = Files.createTempFile("ipfs_upload_", "." + extension);
        
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            fos.write(file.getBytes());
        }
        
        return tempFile.toFile();
    }
}