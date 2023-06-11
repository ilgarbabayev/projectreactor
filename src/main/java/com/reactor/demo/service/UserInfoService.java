package com.reactor.demo.service;

import org.springframework.stereotype.Service;

import com.reactor.demo.model.User;
import com.reactor.demo.repository.UserInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserInfoService {

  private final UserInfoRepository userInfoRepository;

  public Mono<User> getUserById(String userId) {
    return userInfoRepository.findById(userId);
  }
}
