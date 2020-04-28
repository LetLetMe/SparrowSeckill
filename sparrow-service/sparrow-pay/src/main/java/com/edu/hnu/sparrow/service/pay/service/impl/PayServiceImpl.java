package com.edu.hnu.sparrow.service.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.edu.hnu.sparrow.common.conts.CacheKey;
import com.edu.hnu.sparrow.service.pay.dao.SeckillOrderMapper;
import com.edu.hnu.sparrow.service.pay.pojo.SeckillOrder;
import com.edu.hnu.sparrow.service.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PayServiceImpl implements PayService {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;


    /**
     * 订单落库
     * @return
     */
    @Override
    public SeckillOrder setOrder(String username,String phone,String address){

        //删除status信息
        //要是直接删除后来的回滚服务获取不到怎么办？

        //查询到预订单信息
//        SeckillOrder seckillOrder=(SeckillOrder) redisTemplate.boundHashOps(CacheKey.SECKILL_QUEUE_REPEAT).get(username);
//        SeckillOrder seckillOrder= JSON.parseObject((String)redisTemplate.boundHashOps(CacheKey.SECKILL_QUEUE_REPEAT).get(username),SeckillOrder.class);
        SeckillOrder seckillOrder=JSON.parseObject((String)redisTemplate.boundHashOps(CacheKey.SECKILL_QUEUE_REPEAT).get(username),SeckillOrder.class);


//        System.out.println(seckillOrder);
        seckillOrder.setReceiverAddress(address);
        seckillOrder.setReceiver(username);
        seckillOrder.setReceiverMobile(phone);

        //订单落库
        seckillOrderMapper.insert(seckillOrder);
        //删除预订单
        redisTemplate.boundHashOps(CacheKey.SECKILL_QUEUE_REPEAT).delete(username);

        //删除status
        redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).delete(username);



        return seckillOrder;
    }

}
