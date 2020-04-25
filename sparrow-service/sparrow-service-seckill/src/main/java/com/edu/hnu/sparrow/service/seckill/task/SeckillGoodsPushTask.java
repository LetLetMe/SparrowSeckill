package com.edu.hnu.sparrow.service.seckill.task;



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

    public static final String SECKILL_GOODS_KEY="seckill_goods_";

    public static final String SECKILL_GOODS_STOCK_COUNT_KEY="seckill_goods_stock_count_";

    @Scheduled(cron = "0/50 * * * * ?")
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
            String redisExtName = DateUtil.date2Str(dateMenu);

            //构造查询条件第一步
            //构造example
            Example example = new Example(SeckillGoods.class);
            //然后生成一个criteria
            Example.Criteria criteria = example.createCriteria();

            //一个example就把三个筛选条件都覆盖了
            criteria.andEqualTo("status","1");
            criteria.andGreaterThan("stockCount",0);
            //注意java中的时间和数据库中的时间怎么做比较
            criteria.andGreaterThanOrEqualTo("startTime",simpleDateFormat.format(dateMenu));
            //这里提供了一个工具类，可以加2小时，返回一个data对象
            criteria.andLessThan("endTime",simpleDateFormat.format(DateUtil.addDateHour(dateMenu,2)));


            //这个.keys()方法可以获取到这个hash下的所有keys
            Set keys = redisTemplate.boundHashOps(SECKILL_GOODS_KEY + redisExtName).keys();//key field value

            //构造条件的时候，居然可以直接把以前存在的东西给排除掉，数据库就不查了！但是这个sql估计会很长，mysql压力不小
            if (keys != null && keys.size()>0){
                criteria.andNotIn("id",keys);
            }

            //这里演示了tkmapper如何查询
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

            //添加到缓存中
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                //redis的配置要写在application.yml中，springboot会自动识别，并注入redisTamplate
                //操作redis，直接以商品的id作为hash结构中的key，以商品对象作为values，你直接传入对象，redisTemplate对帮你序列化
                //这里redis中hash结构本身的key是  前缀+时间
                redisTemplate.opsForHash().put(SECKILL_GOODS_KEY + redisExtName,seckillGoods.getId(),seckillGoods);
                //下边这种操作方式也行，后边那个put可以直接传入对象的
                //redisTemplate.boundHashOps(SECKILL_GOODS_KEY+redisExtName).put(seckillGoods.getId(),seckillGoods);


                //加载秒杀商品的库存
                //这个是把库存放入一个列表中，用于后来的秒杀减库存
                redisTemplate.opsForValue().set(SECKILL_GOODS_STOCK_COUNT_KEY+seckillGoods.getId(),seckillGoods.getStockCount());
            }
        }



    }

}

