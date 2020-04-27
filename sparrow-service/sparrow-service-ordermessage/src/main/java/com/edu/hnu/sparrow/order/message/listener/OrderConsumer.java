package com.edu.hnu.sparrow.order.message.listener;



import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;

import com.edu.hnu.sparrow.common.entity.CacheKey;
import com.edu.hnu.sparrow.common.entity.MQConstants;
import com.edu.hnu.sparrow.common.entity.SeckillStatus;

import com.edu.hnu.sparrow.service.seckill.pojo.SeckillGoods;
import com.edu.hnu.sparrow.service.seckill.pojo.SeckillOrder;
import com.edu.hnu.sparrow.service.seckill.service.SecKillGoodsService;
import com.edu.hnu.sparrow.service.seckill.service.SecKillOrderService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author jiangli
 * @since 2020/2/20 10:21
 */
@Component
//@Slf4j
public class OrderConsumer {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SecKillOrderService seckillOrderService;
    @Autowired
    private SecKillGoodsService seckillGoodsService;

    /**
     * 监听订单支付回调中的消息然后修改订单状态
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cg.seckillorder.pay.queue", durable = "true"),
            exchange = @Exchange(
                    value = MQConstants.ORDER_EXCHANGE,
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {MQConstants.SECKILL_PAY_SUCCESS_ROUTING_KEY}
    ))
    public void updateOrderStatus(Map<String, String> msg) {
        //通信标识 return_code  //业务结果 result_code
        //3.判断 是否成功(通信是否成)
        if (msg != null) {
            String return_code = msg.get("return_code");
            if ("SUCCESS".equalsIgnoreCase(return_code)) {
                String result_code = msg.get("result_code");
                String attach = msg.get("attach");//json格式的字符串 (里面有用户名信息)
                Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
                String username = attachMap.get("username");

                //获取seckillstatus 状态信息
                SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps(CacheKey.SEC_KILL_USER_STATUS_KEY).get(username);


                if ("SUCCESS".equalsIgnoreCase(result_code)) {
                    //4.判断业务状态是否成功  如果 成功  1.删除预订单 2.同步到数据库 3.删除排队标识 4.删除状态信息
                    //4.1 根据用户名redis中获取订单的数据
                    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps(CacheKey.SEC_KILL_ORDER_KEY).get(username);
                    //4.2 删除预订单
                    redisTemplate.boundHashOps(CacheKey.SEC_KILL_ORDER_KEY).delete(username);
                    //4.3 同步到数据库中
                    //订单这个时候才落库！
                    seckillOrder.setStatus("1");//已经支付
                    seckillOrder.setPayTime(DateUtil.parse(msg.get("time_end"), "yyyyMMddHHmmss"));
                    seckillOrder.setTransactionId(msg.get("transaction_id"));
                    seckillOrderService.save(seckillOrder);

                    //4.4 删除 防止重复排队的标识
                    redisTemplate.boundHashOps(CacheKey.SEC_KILL_QUEUE_REPEAT_KEY).delete(username);
                    //4.5 删除 排队标识
                    redisTemplate.boundHashOps(CacheKey.SEC_KILL_USER_STATUS_KEY).delete(username);

                } else {
                    //关闭微信订单  判断微信关闭订单的状态(1,已支付:调用方法 更新数据到数据库中.2 调用成功:(关闭订单成功:执行删除订单的业务 ) 3.系统错误: 人工处理.   )
                    //5.判断业务状态是否成功  如果 不成功 1.删除预订单 2.恢复库存 3.删除排队标识 4.删除状态信息
                    SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps(CacheKey.SEC_KILL_ORDER_KEY).get(username);
                    redisTemplate.boundHashOps(CacheKey.SEC_KILL_ORDER_KEY).delete(username);

                    // 2.恢复库存  压入商品的超卖的问题的队列中
                    redisTemplate.boundListOps(CacheKey.SEC_KILL_CHAOMAI_LIST_KEY_PREFIX + seckillOrder.getSeckillId()).leftPush(seckillOrder.getSeckillId());

                    //2.恢复库存  获取商品的数据 商品的库存+1
                    SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(CacheKey.SEC_KILL_GOODS_PREFIX + seckillStatus.getTime()).get(seckillOrder.getSeckillId());
                    if (seckillGoods == null) {//说明你买的是最后一个商品 在redis中被删除掉了
                        seckillGoods = seckillGoodsService.getById(seckillOrder.getSeckillId());
                    }
                    Long increment = redisTemplate.boundHashOps(CacheKey.SECK_KILL_GOODS_COUNT_KEY).increment(seckillOrder.getSeckillId(), 1);
                    seckillGoods.setStockCount(increment.intValue());
                    //更新数据的库存
                    seckillGoodsService.updateById(seckillGoods);

                    //3 删除 防止重复排队的标识
                    redisTemplate.boundHashOps(CacheKey.SEC_KILL_QUEUE_REPEAT_KEY).delete(username);
                    //4 删除 排队标识
                    redisTemplate.boundHashOps(CacheKey.SEC_KILL_USER_STATUS_KEY).delete(username);

                }
            }
        }
    }
}
