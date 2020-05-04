//package com.edu.hnu.sparrow.order.message.listener;
//
//
//
//import com.edu.hnu.sparrow.common.conts.CacheKey;
//import com.edu.hnu.sparrow.common.entity.MQConstants;
//import com.edu.hnu.sparrow.common.entity.SeckillStatus;
//
//import com.edu.hnu.sparrow.service.seckill.pojo.SeckillGoods;
//import com.edu.hnu.sparrow.service.seckill.pojo.SeckillOrder;
//import com.edu.hnu.sparrow.service.seckill.service.SecKillGoodsService;
//import com.edu.hnu.sparrow.service.seckill.service.SecKillOrderService;
//import org.springframework.amqp.core.ExchangeTypes;
//import org.springframework.amqp.rabbit.annotation.Exchange;
//import org.springframework.amqp.rabbit.annotation.Queue;
//import org.springframework.amqp.rabbit.annotation.QueueBinding;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
///**
// * @author jiangli
// * @since 2020/2/20 10:21
// */
//@Component
////@Slf4j
//public class OrderConsumer {
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private SecKillGoodsService seckillGoodsService;
//
//
//    /**
//     * 监听订单支付回调中的消息然后修改订单状态
//     */
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "sparrow.seckillorder.pay.queue", durable = "true"),
//            exchange = @Exchange(
//                    value = MQConstants.ORDER_EXCHANGE,
//                    ignoreDeclarationExceptions = "true",
//                    type = ExchangeTypes.TOPIC),
//            key = {MQConstants.SECKILL_PAY_SUCCESS_ROUTING_KEY}
//    ))
//    public void updateOrderStatus(String msg) {
//
//
//        if (msg != null) {
//            String username=msg;
//
//                //获取seckillstatus 状态信息
//                SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).get(username);
//
//                //取到了就能直接删除排队信息了
//                redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).delete(username);
//
//                if (seckillStatus!=null) {
//                    //如果没有查询到，那说明订单支付了，不需要操作
//
//
//                } else {
//
//                    //如果查到了，说明没有支付，才需要回滚库存
//                    //删除预订单
//                    redisTemplate.boundHashOps(CacheKey.SECKILL_QUEUE_REPEAT).delete(username);
//
//                    //2.恢复库存  先尝试在内存中恢复库存
//                    //因为你一个商品秒杀完了，你肯定要删除的，不然给用户展示个库存为0不合适
//                    SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(CacheKey.SEC_KILL_GOODS_PREFIX + seckillStatus.getTime()).get(seckillStatus.getId());
//
//                    //3. 如果内存中的已经被删除了，那么就从数据库获取
//                    // 说明你买的是最后一个商品 在redis中被删除掉了
//                    if (seckillGoods == null) {
//                        //重新设置商品，设置库存
//                        seckillGoods = seckillGoodsService.getById(seckillStatus.getId());
//                        //这里可能出现线程不安全问题，但是这个库存是假的，不准确无所谓
//                        seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
//                        redisTemplate.opsForHash().put(CacheKey.SEC_KILL_GOODS_PREFIX + seckillStatus.getTime(),seckillGoods.getId(),seckillGoods);
//
//                        //原子增加库存
//                        //这个库存不能删除的，即使减少为0也不行
//                        redisTemplate.opsForValue().increment(CacheKey.SECKKILL_GOODS_KUCUN+seckillGoods.getId());
//                    }
//
//
//
//
//
//                }
//            }
//        }
//    }
//
