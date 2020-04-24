package com.edu.hnu.sparrow.service.seckill.service.impl;

import com.edu.hnu.sparrow.common.entity.SeckillStatus;
import com.edu.hnu.sparrow.common.util.IdWorker;
import com.edu.hnu.sparrow.service.seckill.service.SecKillOrderService;
import com.edu.hnu.sparrow.service.seckill.task.MultingThreadCreateOrder;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {

    public final static String SECKILL_ORDER_STATUS="seckill_order_status_";

    public final static String SECKILL_ORDER_LIST="seckill_order_list";

    //主要用来查询用户订单状态
    public final static String SECKILL_ORDER_HASH="seckill_order_hash";
    @Autowired
    public MultingThreadCreateOrder multingThreadCreateOrder;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    IdWorker idWorker;

    //注意下单要传递几个参数
    //用户需要是在登陆的状态下，从商品详情页面发起下单请求
    @Override
    public boolean add(Long id, String time, String username) {

        SeckillStatus seckillStatus=new SeckillStatus();
        //2代表排队
        seckillStatus.setStatus(2);
        seckillStatus.setGoodsID(id);
        seckillStatus.setTime(time);
        seckillStatus.setCreateTime(new Date());


        //搞一个队列，把封装好的存入队列中
        redisTemplate.boundListOps(SECKILL_ORDER_LIST+id).leftPush(seckillStatus);

        //然后再存一个hash结构，方便后续查询状态
        redisTemplate.boundHashOps(SECKILL_ORDER_HASH).put(username,seckillStatus);




        //把下单操作交给多线程异步来完成
        //这里好像不能直接把参数传入，那样就不是异步了？
        //这里等待异步方法返回数据不还是同步么？
        multingThreadCreateOrder.creadOrder();
        //真正想实现异步抢单，还是用mq靠谱
        return true;
    }

    @Override
    public SeckillStatus status(String username,Long id,String time){
        SeckillStatus seckillStatus=(SeckillStatus) redisTemplate.boundHashOps(SECKILL_ORDER_HASH).get(username);
        if(seckillStatus==null){
            seckillStatus.setStatus(0);}
        if(seckillStatus.getStatus()==4){
            //当然推荐你给status对象设置过期时间，几分钟内这个订单没处理就自动删除掉
            //如果查询到这个订单处理失败的化，也会自动删除掉
            redisTemplate.boundHashOps(SECKILL_ORDER_HASH).delete(username);
        }
        return  seckillStatus;

    }


}
