package com.reactor.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.reactor.demo.model.User;

public interface UserInfoRepository extends ReactiveMongoRepository<User, String> {
}
