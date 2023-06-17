package com.reactor.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.reactor.demo.model.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.reactor.demo.service.LogHelper.logOnError;
import static com.reactor.demo.service.LogHelper.logOnNext;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProductService {

  private final WebClient productWebClient;

  @Value("${products.path}")
  private String productPath;


  public Flux<Product> getProducts(String productCode) {
    var uri = UriComponentsBuilder
        .fromUriString(productPath)
        .queryParam("productCode", productCode)
        .buildAndExpand()
        .toUriString();

    return productWebClient
        .get()
        .uri(uri)
        .retrieve()
        .bodyToFlux(Product.class)
        .onErrorResume(ex -> {
          log.error("Error occurred: ", ex);
          return Mono.empty();
        })
        .doOnEach(logOnNext(v -> log.info("getting product {}", v)))
        .doOnEach(logOnError(e -> log.error("error when getting product", e)));
  }
}
