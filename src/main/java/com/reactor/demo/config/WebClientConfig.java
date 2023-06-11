package com.reactor.demo.config;


import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Value("${orders.baseUrl}")
  private String ordersBaseUrl;

  @Value("${products.baseUrl}")
  private String productsBaseUrl;

  @Value("${products.timeout}")
  private long productTimeout;

  @Bean
  public WebClient orderWebClient() {
    return WebClient.builder()
                    .baseUrl(ordersBaseUrl)
                    .build();
  }

  @Bean
  public WebClient productWebClient() {
    return WebClient.builder()
                    .baseUrl(productsBaseUrl)
                    .clientConnector(
                        new ReactorClientHttpConnector(getHttpClient(productTimeout)))
                    .build();
  }

  private HttpClient getHttpClient(long timeout) {
    return HttpClient.create()
                     .responseTimeout(Duration.ofSeconds(timeout));
  }
}
