package com.edu.hnu.sparrow.service.pay.controller;

import com.edu.hnu.sparrow.common.entity.Result;
import com.edu.hnu.sparrow.common.entity.StatusCode;
import com.edu.hnu.sparrow.service.pay.pojo.PayOrder;
import com.edu.hnu.sparrow.service.pay.pojo.SeckillOrder;
import com.edu.hnu.sparrow.service.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private PayService payService;



    /**
     * 支付接口，这里没有对接微信，就打了个桩
     * @param phone
     * @param address
     * @param userName
     * @return
     */
    @PostMapping("/pay")
    public Result<PayOrder> pay(@RequestParam("phone") String phone,
                      @RequestParam("address") String address,
                      @RequestParam("username") String userName){



        SeckillOrder seckillOrder =payService.setOrder(phone, address, userName);

        if(seckillOrder!=null){
            return new Result(true, StatusCode.OK,"结账成功",seckillOrder);
        }
        return new Result(true, StatusCode.OK,"结账失败",null);
    }
}
