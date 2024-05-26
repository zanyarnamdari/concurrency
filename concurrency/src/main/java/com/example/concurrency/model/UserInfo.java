package com.example.concurrency.model;

import java.time.LocalDate;

public record UserInfo(String userId, String username, LocalDate birthDate) {}