package com.edu.hnu.sparrow.service.seckill.service.impl;

import com.edu.hnu.sparrow.common.entity.SeckillStatus;
import com.edu.hnu.sparrow.common.conts.CacheKey;
import com.edu.hnu.sparrow.common.util.IdWorker;
import com.edu.hnu.sparrow.service.seckill.dao.SeckillOrderMapper;
import com.edu.hnu.sparrow.service.seckill.pojo.SeckillOrder;
import com.edu.hnu.sparrow.service.seckill.service.SecKillOrderService;
import com.edu.hnu.sparrow.service.seckill.task.MultingThreadCreateOrder;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sun.misc.Cache;

import java.util.Date;



@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {



    @Autowired
    public MultingThreadCreateOrder multingThreadCreateOrder;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    IdWorker idWorker;

    //注意下单要传递几个参数
    //用户需要是在登陆的状态下，从商品详情页面发起下单请求

    //这个方法只关系重复排队就行了
    @Override
    public boolean add(Long id, String time, String username) {

        SeckillStatus seckillStatus=new SeckillStatus();
        //2代表排队
        seckillStatus.setStatus(2);
        seckillStatus.setGoodsID(id);
        seckillStatus.setTime(time);
        seckillStatus.setCreateTime(new Date());


        //放入排队队列中
        //所有商品一个总的排队队列
        redisTemplate.boundListOps(CacheKey.SEC_KILL_USER_PAIDUI).leftPush(seckillStatus);

        //防止重复排队
        //所有用户一个重复排队的hash
        //住了status记录的订单id是假的,
        //1. 生成订单时候改一次订单号 改状态  2. 支付以后改一次 状态
        redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).put(username,seckillStatus);

        multingThreadCreateOrder.creadOrder();
        //真正想实现异步抢单，还是用mq靠谱
        return true;
    }

    //用户一次只能有一个status对象，只能有一中商品处于排队或者是为支付状态
    @Override
    public SeckillStatus status(String username){
        SeckillStatus seckillStatus=(SeckillStatus) redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).get(username);
        if(seckillStatus==null){
            //记得防止空指针啊...
            seckillStatus=new SeckillStatus();
            seckillStatus.setStatus(0);}
        if(seckillStatus.getStatus()==4){
            //当然推荐你给status对象设置过期时间，几分钟内这个订单没处理就自动删除掉
            //如果查询到这个订单处理失败的化，也会自动删除掉
            redisTemplate.boundHashOps(CacheKey.SECKILL_USER_CHONGFU_PAIDUI).delete(username);
        }
        return  seckillStatus;

    }
    public int save(SeckillOrder seckillOrder){
        int target=seckillOrderMapper.insert(seckillOrder);
        return target;
    }


}
