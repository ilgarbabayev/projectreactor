package com.reactor.demo.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.reactor.demo.model.Product;

import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


@SpringBootTest
class ProductServiceTest {

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
                                                             .options(wireMockConfig().port(8082))
                                                             .build();

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("productInfoService", wireMockServer::baseUrl);
  }

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private ProductService productService;


  @Test
  void getProductsWithSuccessResponse() {

    wireMockServer.stubFor(get(urlPathEqualTo("/productInfoService/product/names"))
                               .willReturn(
                                   aResponse()
                                       .withStatus(200)
                                       .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                       .withBody(getProductsBody())));

    var products = productService.getProducts("1234567");

    StepVerifier.create(products)
                .expectNext(getProducts().get(0))
                .expectNext(getProducts().get(1))
                .verifyComplete();
  }

  @Test
  void getProductsWithErrorResponse() {

    wireMockServer.stubFor(get(urlPathEqualTo("/productInfoService/product/names"))
                               .willReturn(
                                   aResponse()
                                       .withStatus(400)));

    var products = productService.getProducts("1234567");

    StepVerifier.create(products)
                .expectNextCount(0)
                .verifyComplete();
  }

  private String getProductsBody() {
    try {
      return objectMapper.writeValueAsString(getProducts());
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private List<Product> getProducts() {
    return List.of(new Product("111", "555", "Product 1", 5d),
                   new Product("222", "666", "Product 2", 3d));
  }
}