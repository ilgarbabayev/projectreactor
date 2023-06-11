package com.reactor.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reactor.demo.model.OrderInfo;
import com.reactor.demo.service.OrderService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping("/{userId}")
  public Flux<OrderInfo> getUserOrders(@RequestHeader("requestId") String requestId, @PathVariable String userId) {
    return orderService.getOrdersByUserId(userId, requestId);
  }
}
