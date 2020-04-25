package com.edu.hnu.sparrow.service.seckill;

import com.edu.hnu.sparrow.common.util.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.edu.hnu.sparrow.service.seckill.dao"})
@EnableScheduling
//这个是用来支持多线程异步任务的
@EnableAsync
public class SeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class,args);
    }

    //idwork
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(1,1);
    }

    //设置redistemplate的序列化


//    @Bean
//    public TokenDecode tokenDecode(){
//        return new TokenDecode();
//    }
}
