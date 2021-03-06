package com.edu.hnu.sparrow.service.seckill.service;

import com.edu.hnu.sparrow.common.entity.SeckillStatus;
import com.edu.hnu.sparrow.service.seckill.pojo.SeckillOrder;

public interface SecKillOrderService {

    /**
     * 秒杀下单
     * @param id
     * @param time
     * @param username
     * @return
     */
    SeckillStatus add(Long id,String time,String username);

    /**
     * 状态查询,这里还要传入哪来个参数，用户页面其实会阻塞在等待状态，然后后台调用这个接口，就是插叙此次抢购的商品是否完成下单
     * 而不关心用户是否还抢购了其他商品
     * @param username
     * @param
     * @param
     * @return
     */
    SeckillStatus status(String username);
    /**
     * 封装mapper中的操作
     */
    int save(SeckillOrder seckillOrder);
}
