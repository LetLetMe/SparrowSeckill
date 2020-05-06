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
import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

//与订单相关的一共有俩个对象 一个是要落库的order对象，一个是要供用户查询订单状态的status对象
@Component
public class MultingThreadCreateOrder {
    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private IdWorker idWorker;


    @Autowired
    private AmqpTemplate amqpTemplate;

    //加了这个注解这个方法调用时候就是多线程异步执行了
//    @Async
    public void creadOrder(){
        SeckillStatus seckillStatus=JSON.parseObject((String)redisTemplate.boundListOps(CacheKey.SEC_KILL_USER_PAIDUI).rightPop(),SeckillStatus.class);
        //这个是spuId，别搞错了
        Long id=seckillStatus.getId();
        String time=seckillStatus.getTime();
        String username=seckillStatus.getUserName();


        //如果已经有预订单了，就拒绝
        SeckillOrder seckillOrder=JSON.parseObject((String)redisTemplate.boundHashOps(CacheKey.SECKILL_QUEUE_REPEAT).get(username),SeckillOrder.class);

        if(seckillOrder!=null){
            return ;
        }
        //当订单支付落库以后就可以继续下单了

//        3. 获取商品信息

        SeckillGoods seckillGoods = JSON.parseObject((String)redisTemplate.boundHashOps(CacheKey.SEC_KILL_GOODS_PREFIX+time).get(id),SeckillGoods.class);

        if (seckillGoods==null){
            System.out.println("商品获取不到");
            seckillStatus.setStatus(4);
            redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).put(username,JSON.toJSONString(seckillStatus));
            return ;
        }

        //4. 获取库存信息
        Integer redisStock = (Integer) redisTemplate.opsForValue().get(CacheKey.SECKKILL_GOODS_KUCUN+id);

        //5. 如果库存为0的化后续的直接就退出了，但是这里无法阻止高并发
        if (redisStock<=0){
            //设置状态为抢单失败
//            System.out.println("库存为0了");
            seckillStatus.setStatus(4);
            redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).put(username,JSON.toJSONString(seckillStatus));
            return ;
        }

        //6. 尝试获取令牌
        String lingpai=(String)redisTemplate.boundListOps(CacheKey.SECKILL_LINPAI+seckillStatus.getId()).rightPop();
        if(lingpai==null){
//            System.out.println("没有获取到令牌");
            redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).delete(username);
            return;
        }
        //7. 原子减少库存
        redisTemplate.opsForValue().decrement(CacheKey.SECKKILL_GOODS_KUCUN+id);

       //8. 生成order对象，进行预购
        seckillOrder = new SeckillOrder();
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

        redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).put(username,JSON.toJSONString(seckillStatus));

        //10. 预订单
        redisTemplate.boundHashOps(CacheKey.SECKILL_QUEUE_REPEAT).put(username,JSON.toJSONString(seckillOrder));

        //11. 订单创建之后发送消息到延时队列进行定时关单
        amqpTemplate.convertAndSend("CLOSE-ORDER-EXCHANGE", "order.create", seckillStatus.getUserName());

//        System.out.println("下订单完成！！！！！！");
        return ;
    }


}

