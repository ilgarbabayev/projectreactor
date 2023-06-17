package com.reactor.demo.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.reactor.demo.model.Order;
import com.reactor.demo.model.OrderInfo;
import com.reactor.demo.model.Product;
import com.reactor.demo.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;


@SpringBootTest
class OrderServiceTest {

  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
                                                             .options(wireMockConfig().port(8081))
                                                             .build();

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("productInfoService", wireMockServer::baseUrl);
  }

  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  private OrderService orderService;

  @MockBean
  private ProductService productService;
  @MockBean
  private UserInfoService userInfoService;

  @SuppressWarnings("unchecked")
  @Test
  void getOrdersByUserIdWithSuccessResponse() {
    var userId = "123";
    var requestId = "555555";

    when(userInfoService.getUserById(userId)).thenReturn(Mono.just(getUser()));
    when(productService.getProducts(anyString())).thenReturn(Flux.fromIterable(getProducts(0)), Flux.fromIterable(getProducts(1)));

    wireMockServer.stubFor(get(urlPathEqualTo("/orderSearchService/order/phone"))
                               .willReturn(
                                   aResponse()
                                       .withStatus(200)
                                       .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                       .withBody(getOrders())));

    var response = orderService.getOrdersByUserId(userId, requestId);

    StepVerifier.create(response)
                .expectNext(getOrderInfo(0))
                .expectNext(getOrderInfo(1))
                .verifyComplete();
  }

  @Test
  void getOrdersByUserIdWithoutProducts() {
    var userId = "123";
    var requestId = "555555";

    when(userInfoService.getUserById(userId)).thenReturn(Mono.just(getUser()));
    when(productService.getProducts(anyString())).thenReturn(Flux.empty());

    wireMockServer.stubFor(get(urlPathEqualTo("/orderSearchService/order/phone"))
                               .willReturn(
                                   aResponse()
                                       .withStatus(200)
                                       .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                       .withBody(getOrders())));

    var response = orderService.getOrdersByUserId(userId, requestId);

    StepVerifier.create(response)
                .expectNext(getOrderInfoWOProduct(0))
                .expectNext(getOrderInfoWOProduct(1))
                .verifyComplete();
  }

  private User getUser() {
    return new User("123", "Test name", "999 99 99");
  }

  private String getOrders() {
    var orders = List.of(new Order("999 99 99", "ord111", "prdt111"),
                         new Order("999 99 99", "ord222", "prdt222"));
    try {
      return objectMapper.writeValueAsString(orders);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private List<Product> getProducts(int index) {
    var products = List.of(
        List.of(new Product("111", "prdt111", "Product 1", 5d),
                new Product("222", "prdt111", "Product 2", 3d)),

        List.of(new Product("333", "prdt222", "Product 3", 8d),
                new Product("444", "prdt222", "Product 4", 4d)));
    return products.get(index);
  }

  private OrderInfo getOrderInfo(int index) {
    var orderInfos = List.of(OrderInfo.builder()
                                      .orderNumber("ord111")
                                      .userName("Test name")
                                      .phoneNumber("999 99 99")
                                      .productName("Product 1")
                                      .productId("111")
                                      .productCode("prdt111")
                                      .build(),
                             OrderInfo.builder()
                                      .orderNumber("ord222")
                                      .userName("Test name")
                                      .phoneNumber("999 99 99")
                                      .productName("Product 3")
                                      .productId("333")
                                      .productCode("prdt222")
                                      .build());

    return orderInfos.get(index);
  }

  private OrderInfo getOrderInfoWOProduct(int index) {
    var orderInfos = List.of(OrderInfo.builder()
                                      .orderNumber("ord111")
                                      .userName("Test name")
                                      .phoneNumber("999 99 99")
                                      .productCode("prdt111")
                                      .build(),
                             OrderInfo.builder()
                                      .orderNumber("ord222")
                                      .userName("Test name")
                                      .phoneNumber("999 99 99")
                                      .productCode("prdt222")
                                      .build());

    return orderInfos.get(index);
  }
}