package com.edu.hnu.sparrow.service.pay.controller;

import com.edu.hnu.sparrow.common.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PayController {
    @PostMapping("/pay")
    public Result pay(@RequestParam("phone") String phone,
                      @RequestParam("address") String address,
                      @RequestParam("orderId") String orderId,
                      @RequestParam("goodId") String goodId){

        return  null;
    }
}
