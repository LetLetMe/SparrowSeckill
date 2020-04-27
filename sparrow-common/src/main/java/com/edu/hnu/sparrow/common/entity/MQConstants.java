package com.edu.hnu.sparrow.common.entity;

/**
 * @author jiangli
 * @since 2020/2/20 10:15
 */
public class MQConstants {
    //订单相关的消息路由器
    public static final String ORDER_EXCHANGE = "ORDER-EXCHANGE";

    //支付成功消息的routingKey
    public static final String PAY_SUCCESS_ROUTING_KEY = "pay.success";

    //秒杀订单支付成功消息的routingKey
    public static final String SECKILL_PAY_SUCCESS_ROUTING_KEY = "seckillpay.success";

}
