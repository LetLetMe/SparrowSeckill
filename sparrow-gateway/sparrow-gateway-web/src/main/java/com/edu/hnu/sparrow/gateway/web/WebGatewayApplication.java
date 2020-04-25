package com.edu.hnu.sparrow.gateway.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableEurekaClient
public class WebGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebGatewayApplication.class,args);
    }

    @Bean(name = "ipKeyResolver")
    public KeyResolver keyResolver(){
        return exchange -> {
            // 获取远程客户端IP
            String hostAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just(hostAddress);
        };
    }
}
