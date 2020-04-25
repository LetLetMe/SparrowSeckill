package com.edu.hnu.sparrow.service.user.controller;

import com.edu.hnu.sparrow.common.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @PostMapping("/login")
    public Result login(String username,String password){



        return  null;
    }
}
