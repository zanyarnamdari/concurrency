
## Overview
This repository showcases an implementation of concurrent programming using Java 21. The code demonstrates how to efficiently handle multiple tasks while ensuring success of each request.

## Usage

### Step 1: Defining the Scope
In Java 21, the StructuredTaskScope class in the java.util.concurrent package is introduced as a preview feature. To guarantee that every request is successful, the ShutdownOnFailure scope is utilized.

```java
private UserCompleteProfile composeUserProfile(String userId) {
    try (final var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        // Your implementation here...
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}
``` 
### Step 2: Generating Subtasks
Within the scope block, various subtasks are created using the fork method to parallelize operations.

```java
// Inside the scope block
Subtask<List<Follower>> mostRelevantFollowersTask =
    scope.fork(() -> followersRepository.findFollowersByUserId(userId));
Subtask<UserFollowersCount> followersCountTask =
    scope.fork(() -> followersRepository.findFollowersCountByUserId(userId));
Subtask<UserInfo> userInfoTask =
    scope.fork(() -> userInfoRepository.findUserInfoByUserId(userId));
``` 
### Step 3: Ensuring Completion
To wait for the completion of all tasks, use the join() method on the scope.
```java
scope.join();
``` 

### Step 4: Retrieving Results
Once the tasks are completed, retrieve the results to construct the user profile.

```java
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

return profileResult;
``` 
