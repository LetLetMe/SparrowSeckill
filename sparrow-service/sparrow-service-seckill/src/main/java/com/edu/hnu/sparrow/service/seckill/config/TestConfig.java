package com.edu.hnu.sparrow.service.seckill.config;

import com.edu.hnu.sparrow.service.seckill.pojo.SeckillOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {
    @Bean
    public SeckillOrder seckillOrder(){
        return new SeckillOrder();
    }
}
