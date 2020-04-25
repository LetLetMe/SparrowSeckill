package com.edu.hnu.sparrow.service.user.service.impl;

import com.edu.hnu.sparrow.common.entity.AuthToken;
import com.edu.hnu.sparrow.service.user.dao.UserMapper;
import com.edu.hnu.sparrow.service.user.pojo.User;
import com.edu.hnu.sparrow.service.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private RestTemplate restTemplate;



    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Override
    public AuthToken login(String username, String password, HttpServletResponse httpServletResponse) {
        //1.申请令牌


        //查询用户
        User user=userMapper.selectByPrimaryKey(username);

        if(){

        }
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization",this.getHttpBasic(clientId,clientSecret));
        HttpEntity<MultiValueMap<String,String>> requestEntity = new HttpEntity<>(body,headers);

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map map = responseEntity.getBody();
        if (map == null || map.get("access_token") == null || map.get("refresh_token") == null || map.get("jti") == null){
            //申请令牌失败
            throw new RuntimeException("申请令牌失败");
        }

        //2.封装结果数据
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken((String) map.get("access_token"));
        authToken.setRefreshToken((String) map.get("refresh_token"));
        authToken.setJti((String)map.get("jti"));

        //3.将jti作为redis中的key,将jwt作为redis中的value进行数据的存放
        stringRedisTemplate.boundValueOps(authToken.getJti()).set(authToken.getAccessToken(),ttl, TimeUnit.SECONDS);
        return authToken;
    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String value = clientId+":"+clientSecret;
        byte[] encode = Base64Utils.encode(value.getBytes());
        return "Basic "+new String(encode);
    }
}
