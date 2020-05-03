package com.edu.hnu.sparrow.service.seckill;

import com.edu.hnu.sparrow.service.seckill.config.TestConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Test {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext=new AnnotationConfigApplicationContext(TestConfig.class);
    }
}
