package com.edu.hnu.sparrow.service.pay;

import org.springframework.boot.SpringApplication;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = {"com.edu.hnu.sparrow.service.pay.dao"})
public class PayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class);
    }
}
