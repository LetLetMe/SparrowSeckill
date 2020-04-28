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
import org.springframework.amqp.core.AmqpTemplate;
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

    @Autowired
    private AmqpTemplate amqpTemplate;




    //加了这个注解这个方法调用时候就是多线程异步执行了
    @Async
    public void creadOrder(){

        SeckillStatus seckillStatus=(SeckillStatus) redisTemplate.boundListOps(CacheKey.SEC_KILL_USER_PAIDUI).rightPop();
        Long id=seckillStatus.getGoodsID();
        String time=seckillStatus.getTime();
        String username=seckillStatus.getUserName();



        //1. 防止用户恶意刷单
        //传入name和商品id，同一个商品同一个用户只能抢一次
        if (preventRepeatCommit(username)==null){
            //设置状态为抢单失败
            seckillStatus.setStatus(4);
            return ;
        }

        //2. 同一个商品同一个用户防止重复下单
        SeckillOrder order = seckillOrderMapper.getOrderInfoByUserNameAndGoodsId(username, id);
        if (order != null){
            //删除排队信息
            seckillStatus.setStatus(4);


            return;
        }


        //3. 获取商品信息
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(CacheKey.SEC_KILL_GOODS_PREFIX+time).get(id);
        if (seckillGoods==null){
            //删除排队信息
            //如何区分是抢了没强成功，还是根本就没抢呢？
            //这里设置排队信息以后，用户再查询，如果是4，会删除
            seckillStatus.setStatus(4);

            return ;
        }

        //4. 获取库存信息
        String redisStock = (String) redisTemplate.opsForValue().get(CacheKey.SECKKILL_GOODS_KUCUN+id);



        int stock = Integer.parseInt(redisStock);
        //5. 如果库存为0的化后续的直接就退出了，但是这里无法阻止高并发
        if (stock<=0){
            //设置状态为抢单失败
            seckillStatus.setStatus(4);

            return ;
        }

        //6. 尝试获取令牌
        String lingpai=(String)redisTemplate.boundListOps(CacheKey.SECKILL_LINPAI).rightPop();
        if(lingpai==null){
            redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).delete(username);
            return;
        }
        //7. 原子减少库存
        redisTemplate.opsForValue().decrement(id);

       //8. 生成order对象，进行预购
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(id);
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setUserId(username);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");

        //9. 更新status信息，方便用户查询订单状态
        //改变redis中的订单信息
        seckillStatus.setStatus(3);
        //设置需要支付的金额
        //这里用到的数据类型是BigDicimal
        seckillStatus.setMoney(seckillGoods.getCostPrice());
        //更新订单号，因为这里已经生成订单了，因为我设置了一个用户只能抢一个，所有下单可以根据username来查询
        seckillStatus.setOrderID(seckillOrder.getId());

        redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).put(username,seckillGoods);

        //10. 预订单
        redisTemplate.boundHashOps(CacheKey.SECKILL_QUEUE_REPEAT).put(username,seckillOrder);


        //11. 订单创建之后发送消息到延时队列进行定时关单
        amqpTemplate.convertAndSend("CLOSE-ORDER-EXCHANGE", "order.create", seckillStatus.getUserName());

        return ;
    }

    private SeckillOrder preventRepeatCommit(String username){
        SeckillOrder seckillOrder=(SeckillOrder) redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).get(username);
        return  seckillOrder;

    }
}

