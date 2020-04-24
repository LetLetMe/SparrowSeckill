package com.edu.hnu.sparrow.service.seckill.service.impl;



import com.edu.hnu.sparrow.service.seckill.pojo.SeckillGoods;
import com.edu.hnu.sparrow.service.seckill.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecKillGoodsServiceImpl implements SecKillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    //缓存中也就这俩个，一个是hash结构本身的名字前缀，一个是库存count的前缀

    public static final String SECKILL_GOODS_KEY="seckill_goods_";

    public static final String SECKILL_GOODS_STOCK_COUNT_KEY="seckill_goods_stock_count_";

    //秒杀商品主页的信息没有做静态化，而是每次从redis中查询
    //这里给你吧时区传入了，至于前端怎么计算时区那是前端端事情，要保证前后端计算端计算格式一致性
    //当然你也可以写个时间接口，前段来调用，返回一个时间列表
    @Override
    public List<SeckillGoods> list(String time) {
        //获取每个时间段的所有参与秒杀的商品
        List<SeckillGoods> list = redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).values();

        //更新库存数据的来源
        //注意这里这个更新操作！hash结构里边那个库存是假库存！
        for (SeckillGoods seckillGoods : list) {
            String value = (String) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY+seckillGoods.getId());
            seckillGoods.setStockCount(Integer.parseInt(value));
        }
        return list;
    }

    //从redis中查询商品详情
    @Override
    public SeckillGoods one(String time ,Long id){
        //返回的是object类型的，要强制转换
        //这里也需要更新一下库存！

        SeckillGoods seckillGoods=(SeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS_KEY+time).get(id);
        //从redis查出来的都是object，要转换成你需要的类型
        seckillGoods.setStockCount((Integer)redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY+seckillGoods.getId()));
        return  seckillGoods;
    }
}
