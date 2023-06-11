package com.reactor.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.reactor.demo.model.Order;
import com.reactor.demo.model.OrderInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import static com.reactor.demo.service.LogHelper.logOnError;
import static com.reactor.demo.service.LogHelper.logOnNext;

@RequiredArgsConstructor
@Service
@Slf4j
public class OrderService {

  private final UserInfoService userInfoService;
  private final ProductService productService;
  private final WebClient orderWebClient;

  @Value("${orders.path}")
  private String ordersPath;

  public Flux<OrderInfo> getOrdersByUserId(String userId, String requestId) {
    var userMongo = userInfoService.getUserById(userId)
                                   .doOnEach(logOnNext(v -> log.info("getting order for user {}", v)))
                                   .doOnEach(logOnError(e -> log.error("error when getting user", e)))
                                   .contextWrite(Context.of("requestId", requestId));

    return userMongo.flatMapMany(user ->
                                     getOrders(user.getPhone())
                                         .flatMap(order -> {
                                           var orderInfo = OrderInfo.builder()
                                                                    .orderNumber(order.getOrderNumber())
                                                                    .userName(user.getName())
                                                                    .phoneNumber(user.getPhone())
                                                                    .productCode(order.getProductCode())
                                                                    .build();

                                           var productMono = productService.getProducts(order.getProductCode())
                                                                           .reduce((p1, p2) -> p1.getScore() > p2.getScore() ? p1 : p2)
                                                                           .onErrorResume(ex -> {
                                                                             log.error("Error occurred: ", ex);
                                                                             return Mono.empty();
                                                                           })
                                                                           .doOnEach(logOnNext(v -> log.info("getting product {}", v)))
                                                                           .doOnEach(logOnError(e -> log.error("error when getting product", e)))
                                                                           .contextWrite(Context.of("requestId", requestId));

                                           return productMono
                                               .map(product -> {
                                                 orderInfo.setProductName(product.getProductName());
                                                 orderInfo.setProductId(product.getProductId());
                                                 return orderInfo;
                                               })
                                               .switchIfEmpty(Mono.just(orderInfo));
                                         })
                                         .doOnEach(logOnNext(v -> log.info("getting order {}", v)))
                                         .doOnEach(logOnError(e -> log.error("error when getting order", e)))
                                         .contextWrite(Context.of("requestId", requestId))
    );
  }

  private Flux<Order> getOrders(String phoneNumber) {
    var uri = UriComponentsBuilder
        .fromUriString(ordersPath)
        .queryParam("phoneNumber", phoneNumber)
        .buildAndExpand()
        .toUriString();

    return orderWebClient
        .get()
        .uri(uri)
        .retrieve()
        .bodyToFlux(Order.class);
  }
}
