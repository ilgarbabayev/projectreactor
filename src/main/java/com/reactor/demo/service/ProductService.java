package com.reactor.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.reactor.demo.model.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

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
        .bodyToFlux(Product.class);
  }
}
