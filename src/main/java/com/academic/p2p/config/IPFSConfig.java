package com.academic.p2p.config;

import io.ipfs.api.IPFS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IPFSConfig {

    @Value("${ipfs.host:127.0.0.1}")
    private String ipfsHost;

    @Value("${ipfs.port:5001}")
    private int ipfsPort;

    @Bean
    public IPFS ipfs() {
        try {
            // Attempt to connect to the IPFS network
            IPFS ipfsNode = new IPFS(ipfsHost, ipfsPort);
            System.out.println("SUCCESS: Connected to local IPFS node!");
            return ipfsNode;
        } catch (Exception e) {
            // If IPFS is not installed or running, catch the error so the app doesn't crash!
            System.err.println("=====================================================");
            System.err.println("WARNING: Could not connect to IPFS on " + ipfsHost + ":" + ipfsPort);
            System.err.println("The application will still start, but IPFS features will fail.");
            System.err.println("Please install and run IPFS Desktop to use file sharing.");
            System.err.println("=====================================================");
            return null; 
        }
    }
}