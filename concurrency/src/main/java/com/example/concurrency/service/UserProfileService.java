package com.example.concurrency.service;

import com.example.concurrency.entity.UserFollowersCount;
import com.example.concurrency.model.Follower;
import com.example.concurrency.model.UserCompleteProfile;
import com.example.concurrency.model.UserInfo;
import com.example.concurrency.repository.FollowersRepository;
import com.example.concurrency.repository.UserInfoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.concurrent.TimeoutException;

@Service
public class UserProfileService {
    private final Logger log = LoggerFactory.getLogger(UserProfileService.class);
    private final UserInfoRepository userInfoRepository;
    private final FollowersRepository followersRepository;

    public UserProfileService(UserInfoRepository userInfoRepository, FollowersRepository followersRepository) {
        this.userInfoRepository = userInfoRepository;
        this.followersRepository = followersRepository;
    }

    private final Map<String, UserCompleteProfile> cachedUserCompleteProfiles = new ConcurrentHashMap<>();
    public UserCompleteProfile findCompleteUserProfile(String userId) {
        try (final var scope = new StructuredTaskScope.ShutdownOnSuccess<UserCompleteProfile>()) {
            scope.fork(() -> {
                log.info("[Start] CompleteUserProfile from cache");
                final var cachedUser = findCachedUserOrHangFor(userId, 3000L);
                log.info("[End] CompleteUserProfile from cache");
                return cachedUser;
            });
            scope.fork(() -> composeUserProfile(userId));

            // Wait until one of the following happens (in this specific order):
            // - ANY completes successfully; or
            // - at least fails; or
            // - timeouts.
            return scope.joinUntil(Instant.now().plusSeconds(3)).result();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private UserCompleteProfile composeUserProfile(String userId) {
        try (final var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            log.info("[Start] composeUserProfile");
            Subtask<List<Follower>> mostRelevantFollowersTask =
                    scope.fork(() -> followersRepository.findFollowersByUserId(userId));
            Subtask<UserFollowersCount> followersCountTask =
                    scope.fork(() -> followersRepository.findFollowersCountByUserId(userId));
            Subtask<UserInfo> userInfoTask =
                    scope.fork(() -> userInfoRepository.findUserInfoByUserId(userId));

            // Wait until ALL are complete or at least one fails
            scope.join();

            // Get results
            final var userInfo = userInfoTask.get();
            final var mostRelevantFollowers = mostRelevantFollowersTask.get();
            final var rawFollowersCount = followersCountTask.get();
            final var followersCount = rawFollowersCount.followersCount();

            final var profileResult = new UserCompleteProfile(
                    userInfo.userId(),
                    userInfo.username(),
                    Period.between(LocalDate.now(), userInfo.birthDate()).getYears(),
                    followersCount == null ? 0L : followersCount,
                    mostRelevantFollowers
            );
            cachedUserCompleteProfiles.put(userId, profileResult);
            log.info("[End] composeUserProfile");
            return profileResult;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private UserCompleteProfile findCachedUserOrHangFor(String userId, Long hangFoMillis) {
        return Optional
                .ofNullable(cachedUserCompleteProfiles.get(userId))
                .orElseGet(() -> {
                    try {
                        Thread.sleep(hangFoMillis);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                });
    }
}