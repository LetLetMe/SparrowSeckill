package com.edu.hnu.sparrow.service.seckill.task;



import com.alibaba.fastjson.JSON;
import com.edu.hnu.sparrow.common.conts.CacheKey;
import com.edu.hnu.sparrow.common.util.DateUtil;
import com.edu.hnu.sparrow.service.seckill.dao.SeckillGoodsMapper;
import com.edu.hnu.sparrow.service.seckill.pojo.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class SeckillGoodsPushTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;



    @Scheduled(cron = "0/30 * * * * ?")
//    @Scheduled(cron = "0 */3 * * * ?")
    public void  loadSecKillGoodsToRedis(){
        /**
         * 1.查询所有符合条件的秒杀商品
         * 	1) 获取时间段集合并循环遍历出每一个时间段
         * 	2) 获取每一个时间段名称,用于后续redis中key的设置
         * 	3) 状态必须为审核通过 status=1
         * 	4) 商品库存个数>0
         * 	5) 秒杀商品开始时间>=当前时间段
         * 	6) 秒杀商品结束<当前时间段+2小时
         * 	7) 排除之前已经加载到Redis缓存中的商品数据
         * 	8) 执行查询获取对应的结果集
         * 2.将秒杀商品存入缓存
         */

        List<Date> dateMenus = DateUtil.getDateMenus(); // 5个

        //这里记录的是每个时间段的开始时间
        //比如 2020042312  2020042314  2020042316 ... 然后那个俩小时时间段是下边算出来的
        for (Date dateMenu : dateMenus) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            //这里把data转化成字符串了，用于接下来存redis
            String time = DateUtil.date2Str(dateMenu);
            //构造查询条件第一步
            //构造example
            Example example = new Example(SeckillGoods.class);
            //然后生成一个criteria
            Example.Criteria criteria = example.createCriteria();

            //一个example就把三个筛选条件都覆盖了
            criteria.andEqualTo("status","1");
            criteria.andGreaterThan("stockCount",0);
            //注意java中的时间和数据库中的时间怎么做比较
            //原来怎么可能能和string比啊，应该直接和data比
//            criteria.andGreaterThanOrEqualTo("startTime",simpleDateFormat.format(dateMenu));
            criteria.andGreaterThanOrEqualTo("startTime",dateMenu);
            //这里提供了一个工具类，可以加2小时，返回一个data对象
            criteria.andLessThan("endTime",DateUtil.addDateHour(dateMenu,2));


            //这个.keys()方法可以获取到这个hash下的所有keys
            Set keys = redisTemplate.boundHashOps(CacheKey.SEC_KILL_GOODS_PREFIX + time).keys();//key field value

            //构造条件的时候，居然可以直接把以前存在的东西给排除掉，数据库就不查了！但是这个sql估计会很长，mysql压力不小
            if (keys != null && keys.size()>0){
                criteria.andNotIn("id",keys);
            }

            //这里演示了tkmapper如何查询
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

            //添加到缓存中
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                System.out.println(seckillGoods);

                //这种方式插入json不能反解决
//                redisTemplate.opsForHash().put(CacheKey.SEC_KILL_GOODS_PREFIX + time,seckillGoods.getId(), JSON.toJSONString(seckillGoods));
                //这种方式插入json可以反解
                redisTemplate.boundHashOps(CacheKey.SEC_KILL_GOODS_PREFIX + time).put( seckillGoods.getId(),JSON.toJSONString(seckillGoods));
                //下边这种操作方式也行，后边那个put可以直接传入对象的
                //redisTemplate.boundHashOps(SECKILL_GOODS_KEY+redisExtName).put(seckillGoods.getId(),seckillGoods);


                //加载秒杀商品的实际库存
                //1. 区分id和goodsId 2. 由于redis 的string只能存string，所有这里要把库存值转换为string才行，当然取出来时候还要反解一下
                redisTemplate.opsForValue().append(CacheKey.SECKKILL_GOODS_KUCUN+seckillGoods.getId(),JSON.toJSONString(seckillGoods.getNum()));

                System.out.println(seckillGoods.getId());
                System.out.println(seckillGoods.getNum());
                //这个是把库存放入一个列表中，用于后来的秒杀减库存
                for(int i=0;i<seckillGoods.getNum();i++){

                    redisTemplate.boundListOps(CacheKey.SECKILL_LINPAI+seckillGoods.getId()).leftPush("1");

                }

            }
        }



    }

}

