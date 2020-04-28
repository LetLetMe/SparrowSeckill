package com.edu.hnu.sparrow.service.pay.service;

import com.edu.hnu.sparrow.service.pay.pojo.PayOrder;
import com.edu.hnu.sparrow.service.pay.pojo.SeckillOrder;

import javax.persistence.Column;
import javax.persistence.Table;


public interface PayService {

    SeckillOrder setOrder(String username, String phone, String address);
}
