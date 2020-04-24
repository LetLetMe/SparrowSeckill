package com.edu.hnu.sparrow.service.seckill.task;


import com.alibaba.fastjson.JSON;

//import com.changgou.seckill.config.ConfirmMessageSender;
//import com.changgou.seckill.config.RabbitMQConfig;


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

        SeckillStatus seckillStatus=(SeckillStatus) redisTemplate.boundListOps(SECKILL_ORDER_LIST).rightPop();
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
        String result = this.preventRepeatCommit(username, id);
        //传入name和商品id，同一个商品同一个用户只能抢一次
        if ("fail".equals(result)){
            //设置状态为抢单失败
            seckillStatus.setStatus(4);
            return ;
        }

        //防止相同商品重复购买
        SeckillOrder order = seckillOrderMapper.getOrderInfoByUserNameAndGoodsId(username, id);
        //同一个商品同一个用户防止重复下单
        if (order != null){
            //设置状态为抢单失败
            seckillStatus.setStatus(4);
            return;
        }


        //获取商品信息
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS_KEY+time).get(id);

        //获取库存信息
        String redisStock = (String) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY+id);
        if (StringUtils.isEmpty(redisStock)){
            //设置状态为抢单失败
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

        //执行redis的预扣减库存,并获取到扣减之后的库存值
        //decrement:减 increment:加     ->    Lua脚本语言
        Long decrement = redisTemplate.opsForValue().decrement(SECKILL_GOODS_STOCK_COUNT_KEY + id);
        if (decrement<=0){
            //扣减完库存之后,当前商品已经没有库存了.
            //删除redis中的商品信息与库存信息
            redisTemplate.boundHashOps(SECKILL_GOODS_KEY+time).delete(id);
            redisTemplate.delete(SECKILL_GOODS_STOCK_COUNT_KEY + id);
            //下边这种可能是常规下单操作，秒杀这样搞简直就是作死
//            //而且要把商品信息落盘（要是有人下单不结账怎么办？）
//            //而且这里超卖了怎么办？
//            seckillGoods.setStockCount(0);
//            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
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

        //改变redis中的订单信息
        seckillStatus.setStatus(3);
        //设置需要支付的金额
        //这里用到的数据类型是BigDicimal
        seckillStatus.setMoney(seckillGoods.getCostPrice());
        //更新订单号
        seckillStatus.setOrderID(seckillOrder.getId());

        redisTemplate.boundHashOps(SECKILL_ORDER_HASH).put(username,seckillGoods);


        //当然你把订单信息先落盘到redis好像也可以
        //hash中每个key对应的value直接存对象即可！
        //redisTemplate.boundHashOps(SECKILL_ORDER_KEY).put(username,order);

        //发送消息
        //秒杀订单不能立即落盘mysql，而是发到mq中，等到支付以后再异步落盘，但是redis中的库存是立马就减掉了的
//        confirmMessageSender.sendMessage("", RabbitMQConfig.SECKILL_ORDER_QUEUE, JSON.toJSONString(seckillOrder));

        return  ;
    }

    private String preventRepeatCommit(String username,Long id){
        String redis_key = "seckill_user_"+username+"_id_"+id;

        //自增可以每个用户搞一个string类型，也可以把他们合在一个hash结构中，hash中的每个key都可以原子自增的
        long count = redisTemplate.opsForValue().increment(redis_key, 1);
        if (count == 1){
            //代表当前用户是第一次访问.
            //对当前的key设置一个五分钟的有效期
            redisTemplate.expire(redis_key,5, TimeUnit.MINUTES);
            return "success";
        }

        if (count>1){
            return "fail";
        }

        return "fail";
    }
}

