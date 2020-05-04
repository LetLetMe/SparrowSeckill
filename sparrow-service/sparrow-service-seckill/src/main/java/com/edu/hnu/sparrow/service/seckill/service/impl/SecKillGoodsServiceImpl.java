package com.edu.hnu.sparrow.service.seckill.service.impl;



import com.edu.hnu.sparrow.common.conts.CacheKey;
import com.edu.hnu.sparrow.common.util.DateUtil;
import com.edu.hnu.sparrow.common.util.IdWorker;
import com.edu.hnu.sparrow.service.seckill.dao.SeckillGoodsMapper;
import com.edu.hnu.sparrow.service.seckill.dao.SeckillOrderMapper;
import com.edu.hnu.sparrow.service.seckill.pojo.SeckillGoods;
import com.edu.hnu.sparrow.service.seckill.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataUnit;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class SecKillGoodsServiceImpl implements SecKillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    //这个是用来生成spu和sku的id的
    @Autowired
    private IdWorker idWorker;

    //设置日期的
    private  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //秒杀商品主页的信息没有做静态化，而是每次从redis中查询
    //这里给你吧时区传入了，至于前端怎么计算时区那是前端端事情，要保证前后端计算端计算格式一致性
    //当然你也可以写个时间接口，前段来调用，返回一个时间列表
    @Override
    public List<SeckillGoods> list(String time) {
        //获取每个时间段的所有参与秒杀的商品
        List<SeckillGoods> list = redisTemplate.boundHashOps(CacheKey.SEC_KILL_GOODS_PREFIX + time).values();

        //更新库存数据的来源
        //注意这里这个更新操作！hash结构里边那个库存是假库存！
        for (SeckillGoods seckillGoods : list) {

            String value = (String) redisTemplate.boundHashOps(CacheKey.SECKKILL_GOODS_KUCUN).get(seckillGoods.getId());
            seckillGoods.setStockCount(Integer.parseInt(value));

        }
        return list;
    }



    //从redis中查询商品详情
    //这个需要时区和商品id才能定位
    @Override
    public SeckillGoods one(String time ,Long id){
        //返回的是object类型的，要强制转换
        //这里也需要更新一下库存！

        SeckillGoods seckillGoods=(SeckillGoods) redisTemplate.boundHashOps(CacheKey.SEC_KILL_GOODS_PREFIX +time).get(id);
        //从redis查出来的都是object，要转换成你需要的类型
        //这里还需要查询一下库存，吧库存装进去
//        seckillGoods.setStockCount((Integer)redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY+seckillGoods.getId()));
//        String value = (String) redisTemplate.boundHashOps(CacheKey.SECKKILL_GOODS_KUCUN).get(seckillGoods.getId());
        //放在hash结构中无法原子减少
        String value = (String) redisTemplate.opsForValue().get(CacheKey.SECKKILL_GOODS_KUCUN+seckillGoods.getId());
        seckillGoods.setStockCount(Integer.parseInt(value));
        return  seckillGoods;
    }
    /**
     * 封装mapper中的一些操作，mapper直接暴露给controller不太妥
     */
    @Override
    public int updateById(SeckillGoods seckillGoods){
        int target =seckillGoodsMapper.updateByPrimaryKey(seckillGoods);

        return target;
    }

    /**
     * 封装mapper中的数据
     * @param id
     * @return
     */
    @Override
    public SeckillGoods getById(long id){
        SeckillGoods seckillGoods=seckillGoodsMapper.selectByPrimaryKey(id);
        return  seckillGoods;
    }

    /**
     * 添加秒杀商品
     */

    @Override
    public SeckillGoods addGoods(Date startTime){


        SeckillGoods seckillGoods=new SeckillGoods();

        //总的id
        seckillGoods.setId(idWorker.nextId());

        //总共参与秒杀的商品数
        seckillGoods.setNum(100);
        //初始库存
        seckillGoods.setStockCount(100);
        //价格
        seckillGoods.setCostPrice(new BigDecimal("100"));
        //spuID
        seckillGoods.setGoodsId(idWorker.nextId());


        //商品添加时间
        seckillGoods.setCreateTime(new Date());
        //开始时间
        seckillGoods.setStartTime(startTime);
        //设置结束时间
//        seckillGoods.setEndTime(DateUtil.addDateHour(new Date(),1));
        //这里设置的太长了不方便测试
        seckillGoods.setEndTime(DateUtil.addDateMinutes(new Date(),5));
        //商品描述
        seckillGoods.setTitle("测试商品"+startTime.toString());
        //设置审核状态
        seckillGoods.setStatus("1");

        seckillGoodsMapper.insert(seckillGoods);

        return  seckillGoods;
    }
}
