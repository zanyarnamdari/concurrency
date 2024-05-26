package com.example.concurrency.model;

import java.util.List;

public record UserCompleteProfile(
        String userId,
        String username,
        int age,
        long followerCount,
        List<Follower> mostRelevantFollowers
) {}