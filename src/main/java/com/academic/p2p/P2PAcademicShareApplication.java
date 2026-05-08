package com.academic.p2p;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class P2PAcademicShareApplication {
    public static void main(String[] args) {
        SpringApplication.run(P2PAcademicShareApplication.class, args);
        System.out.println("""
                
                ╔═══════════════════════════════════════════════════════════╗
                ║     Decentralized P2P Academic Resource Sharing Platform  ║
                ║                   Started Successfully!                    ║
                ║                                                           ║
                ║  Access URL: http://localhost:8081                        ║
                ║  H2 Console: http://localhost:8081/h2-console             ║
                ║                                                           ║
                ║  IPFS Connection: localhost:5001                          ║
                ╚═══════════════════════════════════════════════════════════╝
                """);
    }
}