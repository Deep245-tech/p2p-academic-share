package com.academic.p2p;

import com.academic.p2p.model.User;
import com.academic.p2p.repository.UserRepository;
import com.academic.p2p.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class P2PAcademicShareApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void testUserRegistration() {
        User user = userService.registerUser(
            "testuser_" + System.currentTimeMillis(),
            "test@example.com",
            "password123"
        );

        assertNotNull(user.getId());
        assertEquals(100, user.getTokenBalance());
        assertEquals(1.0, user.getReputationScore());
    }

    @Test
    void testTokenBalance() {
        User user = userService.registerUser(
            "tokenuser_" + System.currentTimeMillis(),
            "token@example.com",
            "password123"
        );

        var balance = userService.getTokenBalance(user.getId());
        assertEquals(100, balance.getCurrentBalance());
    }
}