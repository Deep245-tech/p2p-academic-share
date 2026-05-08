package com.academic.p2p.controller;

import com.academic.p2p.dto.ReviewDTO;
import com.academic.p2p.model.Resource;
import com.academic.p2p.model.Review;
import com.academic.p2p.model.User;
import com.academic.p2p.service.ProofOfContributionService;
import com.academic.p2p.service.ResourceService;
import com.academic.p2p.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class WebController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private ProofOfContributionService pocService;
    
    @GetMapping("/")
    public String home(Model model) {
        List<Resource> topResources = resourceService.getTopQualityResources(6);
        List<User> topContributors = userService.getTopContributors();
        long totalResources = resourceService.getTotalVerifiedResources();
        
        model.addAttribute("topResources", topResources);
        model.addAttribute("topContributors", topContributors);
        model.addAttribute("totalResources", totalResources);
        
        return "index";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          RedirectAttributes redirectAttributes) {
        try {
            User user = userService.registerUser(username, email, password);
            redirectAttributes.addFlashAttribute("success", "Registration successful! You received 100 tokens.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(username);
            if (user.getPassword().equals(password)) {
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                return "redirect:/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid password");
                return "redirect:/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/login";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        User user = userService.getUserById(userId);
        List<Resource> userResources = resourceService.getUserResources(userId);
        var tokenBalance = userService.getTokenBalance(userId);
        var transactions = userService.getTransactionHistory(userId);
        double contributionScore = pocService.calculateContributionScore(user);
        
        model.addAttribute("user", user);
        model.addAttribute("userResources", userResources);
        model.addAttribute("tokenBalance", tokenBalance);
        model.addAttribute("transactions", transactions);
        model.addAttribute("contributionScore", contributionScore);
        
        return "dashboard";
    }
    
    @GetMapping("/upload")
    public String uploadPage(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        return "upload";
    }
    
    @GetMapping("/resources")
    public String listResources(@RequestParam(required = false) String search,
                                @RequestParam(required = false) String type,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String sort,
                                @RequestParam(defaultValue = "false") boolean verified,
                                @RequestParam(defaultValue = "0") int page,
                                Model model) {
                                
        // Pass all the new parameters down to the service
        Page<Resource> resources = resourceService.searchAndFilterResources(search, type, category, sort, verified, page, 12);
        
        model.addAttribute("resources", resources);
        model.addAttribute("search", search);
        
        return "resources";
    }
    
    @GetMapping("/resources/{id}")
    public String resourceDetail(@PathVariable Long id, Model model, HttpSession session) {
        Resource resource = resourceService.getResource(id);
        model.addAttribute("resource", resource);
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("isOwner", resource.getUploader().getId().equals(userId));
        }
        
        return "resource-detail";
    }
    
    @GetMapping("/profile/{id}")
    public String userProfile(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        List<Resource> userResources = resourceService.getUserResources(id);
        var tokenBalance = userService.getTokenBalance(id);
        double contributionScore = pocService.calculateContributionScore(user);
        
        model.addAttribute("profileUser", user);
        model.addAttribute("userResources", userResources);
        model.addAttribute("tokenBalance", tokenBalance);
        model.addAttribute("contributionScore", contributionScore);
        
        return "profile";
    }
    
    @PostMapping("/resources/{id}/verify")
    public String verifyResource(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        resourceService.verifyResource(id, userId);
        return "redirect:/resources/" + id;
    }
}