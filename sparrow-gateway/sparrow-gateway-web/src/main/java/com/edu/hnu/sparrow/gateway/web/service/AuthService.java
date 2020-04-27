package com.edu.hnu.sparrow.gateway.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final String LONG_TOKEN="long_token";
    private static final String AUTHORIZATION="Authorization";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //这里相当于分布式session存储了
    //从cookie中获取jti的值
    public String getJtiFromCookie(ServerHttpRequest request) {
        HttpCookie httpCookie = request.getCookies().getFirst(AUTHORIZATION);
        if (httpCookie != null){
            String jti = httpCookie.getValue();

            return jti;
        }
        return null;
    }


    //查询jwt
    public String getJwtFromRedis(String jti) {
//        String jwt = stringRedisTemplate.boundValueOps(LONG_TOKEN).get();
        String jwt = (String)stringRedisTemplate.boundHashOps(LONG_TOKEN).get(jti);
        return jwt;
    }
}
