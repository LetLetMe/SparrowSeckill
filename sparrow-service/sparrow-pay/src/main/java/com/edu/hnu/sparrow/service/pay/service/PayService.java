package com.edu.hnu.sparrow.service.pay.service;

import com.edu.hnu.sparrow.service.pay.pojo.SeckillOrder;


public interface PayService {

    SeckillOrder setOrder(String username, String phone, String address);
}
