package com.example.concurrency.controller;

import com.example.concurrency.model.UserCompleteProfile;
import com.example.concurrency.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{userId}")
public class ProfileController {
    public ProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    private final UserProfileService userProfileService;

    @GetMapping("/complete-profile")
    public ResponseEntity<UserCompleteProfile> getCompleteProfile(@PathVariable String userId) {
        System.out.println("Current thread="+ Thread.currentThread() + " isVirtual=" + Thread.currentThread().isVirtual());
        var userProfile = userProfileService.findCompleteUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }
}