package com.edu.hnu.sparrow.service.seckill.task;


import com.alibaba.fastjson.JSON;

//import com.changgou.seckill.config.ConfirmMessageSender;
//import com.changgou.seckill.config.RabbitMQConfig;


import com.edu.hnu.sparrow.common.conts.CacheKey;
import com.edu.hnu.sparrow.common.entity.SeckillStatus;
import com.edu.hnu.sparrow.common.util.IdWorker;
import com.edu.hnu.sparrow.service.seckill.dao.SeckillOrderMapper;
import com.edu.hnu.sparrow.service.seckill.pojo.SeckillGoods;
import com.edu.hnu.sparrow.service.seckill.pojo.SeckillOrder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

//与订单相关的一共有俩个对象 一个是要落库的order对象，一个是要供用户查询订单状态的status对象
@Component
public class MultingThreadCreateOrder {
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SECKILL_GOODS_KEY="seckill_goods_";

    public static final String SECKILL_GOODS_STOCK_COUNT_KEY="seckill_goods_stock_count_";
    //订单首先落到redis中
    public static final String SECKILL_ORDER_KEY="seckill_order_key_;";

    public final static String SECKILL_ORDER_LIST="seckill_order_list";

    public final static String SECKILL_ORDER_HASH="seckill_order_hash";

    @Autowired
    private IdWorker idWorker;

//    @Autowired
//    private ConfirmMessageSender confirmMessageSender;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;



    //加了这个注解这个方法调用时候就是多线程异步执行了
    @Async
    public void creadOrder(){

        SeckillStatus seckillStatus=(SeckillStatus) redisTemplate.boundListOps(CacheKey.SEC_KILL_USER_PAIDUI).rightPop();
        Long id=seckillStatus.getGoodsID();
        String time=seckillStatus.getTime();
        String username=seckillStatus.getUserName();


        /**
         * 1.获取redis中的商品信息与库存信息,并进行判断
         * 2.执行redis的预扣减库存操作,并获取扣减之后的库存值
         * 3.如果扣减之后的库存值<=0,则删除redis中响应的商品信息与库存信息
         * 4.基于mq完成mysql的数据同步,进行异步下单并扣减库存(mysql)
         */
        //防止用户恶意刷单

        //传入name和商品id，同一个商品同一个用户只能抢一次
        if (preventRepeatCommit(username)==null){
            //设置状态为抢单失败
            seckillStatus.setStatus(4);
            return ;
        }

        //防止相同商品重复购买
        SeckillOrder order = seckillOrderMapper.getOrderInfoByUserNameAndGoodsId(username, id);
        //同一个商品同一个用户防止重复下单
        if (order != null){
            //删除排队信息
            seckillStatus.setStatus(4);


            return;
        }


        //获取商品信息
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(CacheKey.SEC_KILL_GOODS_PREFIX+time).get(id);

        //获取库存信息
        String redisStock = (String) redisTemplate.opsForValue().get(CacheKey.SECKKILL_GOODS_KUCUN+id);

        if (StringUtils.isEmpty(redisStock)){
            //删除排队信息
            //如何区分是抢了没强成功，还是根本就没抢呢？
            //这里设置排队信息以后，用户再查询，如果是4，会删除
            seckillStatus.setStatus(4);

            return ;
        }

        int stock = Integer.parseInt(redisStock);
        //如果库存为0的化后续的直接就退出了，但是这里无法阻止高并发
        if (seckillGoods == null || stock<=0){
            //设置状态为抢单失败
            seckillStatus.setStatus(4);

            return ;
        }

        //尝试获取令牌
        String lingpai=(String)redisTemplate.boundListOps(CacheKey.SECKILL_LINPAI).rightPop();
        if(lingpai==null){
            redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).delete(username);
            return;
        }

        //发送消息(保证消息生产者对于消息的不丢失实现)
        //消息体: 秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(id);
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setUserId(username);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");

        //这三部都很重要！这才是实际的抢到单以后的status信息
        //改变redis中的订单信息
        seckillStatus.setStatus(3);
        //设置需要支付的金额
        //这里用到的数据类型是BigDicimal
        seckillStatus.setMoney(seckillGoods.getCostPrice());
        //更新订单号
        //这一步很重要！
        seckillStatus.setOrderID(seckillOrder.getId());

        redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).put(username,seckillGoods);



        return ;
    }

    private SeckillOrder preventRepeatCommit(String username){
        SeckillOrder seckillOrder=(SeckillOrder) redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).get(username);
        return  seckillOrder;

    }
}

