package com.reactor.demo.service;

import org.springframework.stereotype.Service;

import com.reactor.demo.model.User;
import com.reactor.demo.repository.UserInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static com.reactor.demo.service.LogHelper.logOnError;
import static com.reactor.demo.service.LogHelper.logOnNext;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserInfoService {

  private final UserInfoRepository userInfoRepository;

  public Mono<User> getUserById(String userId) {
    return userInfoRepository.findById(userId)
                             .doOnEach(logOnNext(v -> log.info("getting order for user {}", v)))
                             .doOnEach(logOnError(e -> log.error("error when getting user", e)));
  }
}
