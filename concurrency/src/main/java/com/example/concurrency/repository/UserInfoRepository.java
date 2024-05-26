package com.example.concurrency.repository;

import com.example.concurrency.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UserInfoRepository {
    public UserInfoRepository(WebClient webClient) {
        this.webClient = webClient;
    }

    private final Logger log = LoggerFactory.getLogger(UserInfoRepository.class);

    private final WebClient webClient;

    public UserInfo findUserInfoByUserId(String userId) {
        return webClient
                .get()
                .uri("/users/" + userId)
                .retrieve()
                .bodyToMono(UserInfo.class)
                .doOnError(err -> log.error("Error on findUserInfoById: ", err))
                .block();
    }
}