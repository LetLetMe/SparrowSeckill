package com.edu.hnu.sparrow.service.pay.service.impl;

import com.edu.hnu.sparrow.service.pay.dao.PayOrderMapper;
import com.edu.hnu.sparrow.service.pay.pojo.PayOrder;
import com.edu.hnu.sparrow.service.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;

public class PayServiceImpl implements PayService {
    @Autowired
    private PayOrderMapper payOrderMapper;


    /**
     * 订单落库
     * @param payOrder
     * @return
     */
    @Override
    public boolean setOrder(PayOrder payOrder){

        payOrderMapper.insert(payOrder);
        return true;
    }

}
