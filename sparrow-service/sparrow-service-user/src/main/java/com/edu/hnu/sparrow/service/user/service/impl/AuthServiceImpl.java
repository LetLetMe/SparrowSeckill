package com.edu.hnu.sparrow.service.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.edu.hnu.sparrow.common.conts.CacheKey;
import com.edu.hnu.sparrow.common.entity.AuthToken;
import com.edu.hnu.sparrow.common.util.CodecUtil;
import com.edu.hnu.sparrow.common.util.JwtUtil;
import com.edu.hnu.sparrow.service.user.dao.UserMapper;
import com.edu.hnu.sparrow.service.user.pojo.User;
import com.edu.hnu.sparrow.service.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Override
    public AuthToken login(String username, String password) {

        User user = userMapper.selectByPrimaryKey(username);
        // 密码加密 BCrypt
//        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
        //用md5吧别用这个什么BC了
        if (user != null && user.getPassword().equals(CodecUtil.md5Hex(password, user.getSalt()))) {
            // 用户校验通过，生成令牌，保存到客户端(cookie)
            // arg01:id唯一的ID  arg02: 载荷信息  arg03：token过期时间不设置默认一个小时
            Map<String, Object> map = new HashMap<>();
            map.put("role", "ROLE_USER");
            map.put("status", "SUCCESS");
            //我去，这里别把所有都放进去啊，放个用户名方便以后的服务使用就好了
            map.put("userinfo", user.getName());
            // 将map转String
            String info = JSON.toJSONString(map);
            String uuid=UUID.randomUUID().toString();
            String token = JwtUtil.createJWT(uuid, info, null);

            //把长的token存入redis，注意这里用到了一个hash命名空间
            //uuid作为jti了，勉强也行把
            redisTemplate.boundHashOps(CacheKey.USER_JWT).put(uuid,token);


            AuthToken authToken=new AuthToken();
            authToken.setAccessToken(token);
            authToken.setJti(uuid);
            return authToken;
        }
        return null;
    }


}
