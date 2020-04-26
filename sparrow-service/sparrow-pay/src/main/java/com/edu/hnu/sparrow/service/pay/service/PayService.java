package com.edu.hnu.sparrow.service.pay.service;

import com.edu.hnu.sparrow.service.pay.pojo.PayOrder;

import javax.persistence.Column;
import javax.persistence.Table;


public interface PayService {

    boolean setOrder(PayOrder payOrder);
}
