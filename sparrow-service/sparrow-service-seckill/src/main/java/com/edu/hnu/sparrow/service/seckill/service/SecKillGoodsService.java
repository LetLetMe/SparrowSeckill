package com.edu.hnu.sparrow.service.seckill.service;



import com.edu.hnu.sparrow.service.seckill.pojo.SeckillGoods;

import java.util.List;

public interface SecKillGoodsService {

    /**
     * 商品首页，获取各个时间段的商品
     * @param time
     * @return
     */
    List<SeckillGoods> list(String time);

    /**
     * 从redis中查询商品详情
     * @param time
     * @param id
     * @return
     */
    SeckillGoods one(String time ,Long id);



    /**
     * 封装mapper中的几个数据
     */
    int updateById(SeckillGoods seckillGoods);
    SeckillGoods getById(long id);

}
