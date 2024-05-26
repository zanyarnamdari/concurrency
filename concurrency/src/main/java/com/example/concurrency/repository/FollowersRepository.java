package com.example.concurrency.repository;

 import com.example.concurrency.entity.UserFollowersCount;
 import com.example.concurrency.model.Follower;
 import org.springframework.stereotype.Repository;
 import org.springframework.web.reactive.function.client.WebClient;

 import java.util.List;

@Repository
public class FollowersRepository {
    public FollowersRepository(WebClient webClient) {
        this.webClient = webClient;
    }

    private final WebClient webClient;

    public UserFollowersCount findFollowersCountByUserId(String userId) {
        return webClient
                .get()
                .uri(String.format("/users/%s/followers-count", userId))
                .retrieve()
                .bodyToMono(UserFollowersCount.class)
                .blockOptional()
                .orElse(null);
    }

    public List<Follower> findFollowersByUserId(String userId) {
        return webClient
                .get()
                .uri(String.format("/users/%s/followers", userId))
                .retrieve()
                .bodyToFlux(Follower.class)
                .collectList()
                .block();
    }
}