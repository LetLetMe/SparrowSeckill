package com.edu.hnu.sparrow.service.user.controller;

import com.edu.hnu.sparrow.common.entity.Result;
import com.edu.hnu.sparrow.common.entity.StatusCode;
import com.edu.hnu.sparrow.service.user.pojo.User;
import com.edu.hnu.sparrow.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/register")
public class RegisterController {
    @Autowired
    private UserService userService;

    /**
     * 校验数据是否可用
     * @param data
     * @param type
     * @return
     */
    @GetMapping("check/{data}/{type}")
    public Result checkUserData(@PathVariable("data") String data, @PathVariable(value = "type") Integer type) {
        boolean boo = this.userService.checkData(data, type);
        //这里如果数据库查询失败，同意返回false了，可能不易于区分
        return new Result(boo, StatusCode.OK,"检验是否可用");
    }

    /**
     * 发送手机验证码
     * @param phone
     * @return
     */
    @PostMapping("/code")
    public Result sendVerifyCode(String phone) {
        boolean boo = this.userService.sendVerifyCode(phone);

        if(boo){
            return new Result(boo, StatusCode.OK,"成功发送短信");
        }
        return new Result(boo, StatusCode.OK,"发送短信失败");

    }

    /**
     * 注册
     * @param
     * @param code
     * @return
     */
    @PostMapping("/register")
//    public Result register(@Valid User user, @RequestParam("code") String code) {
    public Result register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("code") String code,
                           @RequestParam("phone") String phone) {
        User user=new User();
        //区分username和name
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        System.out.println(user);

        boolean boo = this.userService.register(user, code);

        if(boo){
            return new Result(boo, StatusCode.OK,"注册成功");
        }
        return new Result(boo, StatusCode.ERROR,"注册失败");
    }

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    public Result queryUser(@RequestParam("username") String username, @RequestParam("password") String password) {
        //可以设置全局异常处理器，前提是你得知道他抛的什么异常
        //也可以设置参数非必须，然后自己校验是否为null
        User user = this.userService.queryUser(username, password);
        if (user == null) {
            return new Result(false, StatusCode.ERROR,"无此用户");
        }
        return new Result(true, StatusCode.OK,"有此用户");
    }
}
