package com.edu.hnu.sparrow.service.user.controller;

import com.edu.hnu.sparrow.common.entity.AuthToken;
import com.edu.hnu.sparrow.common.entity.Result;
import com.edu.hnu.sparrow.common.entity.StatusCode;
import com.edu.hnu.sparrow.service.user.pojo.User;
import com.edu.hnu.sparrow.service.user.service.AuthService;
import com.edu.hnu.sparrow.service.user.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/oauth")
public class AuthController {
    private static final String AUTHORIZATION="Authorization";

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;
//
//    @Value("${auth.clientId}")
//    private String clientId;
//
//    @Value("${auth.clientSecret}")
//    private String clientSecret;
//
//    @Value("${auth.cookieDomain}")
//    private String cookieDomain;
//
//    @Value("${auth.cookieMaxAge}")
//    private int cookieMaxAge;
//
//    @RequestMapping("/toLogin")
//    public String toLogin(@RequestParam(value = "FROM",required = false,defaultValue = "") String from, Model model){
//        model.addAttribute("from",from);
//        return "login";
//    }


    @PostMapping("/login")
    @ResponseBody
    public Result login(String username, String password, HttpServletResponse httpServletResponse){

        //校验参数
        if (StringUtils.isEmpty(username)){
//
            return  new Result(false,StatusCode.ERROR,"请输入用户名",null);
        }
        if (StringUtils.isEmpty(password)){
//
            return  new Result(false,StatusCode.ERROR,"请输入密码",null);
        }
        //申请令牌 authtoken


        User user=userService.queryUser(username,password);
        if(user==null){

            return new Result(true, StatusCode.ERROR,"登录失败，用户不存在",null);
        }
        AuthToken authToken = authService.login(username, password);
        if(authToken==null){
            return  new Result(false,StatusCode.ERROR,"登陆失败，申请token失败",null);
        }

        //将jti的值存入cookie中
        Cookie cookie = new Cookie(AUTHORIZATION, authToken.getJti());
        cookie.setDomain("localhost"); //域名
        cookie.setPath("/"); //设置到跟路径下

        httpServletResponse.addCookie(cookie);

        //返回结果
        //就把jti加入返回的对象头中

        return new Result(true, StatusCode.OK,"登录成功",authToken.getJti());
    }

}

