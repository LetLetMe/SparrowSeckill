package com.edu.hnu.sparrow.service.pay.dao;



import com.edu.hnu.sparrow.service.pay.pojo.SeckillOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;


public interface SeckillOrderMapper extends Mapper<SeckillOrder> {

    @Select("select * from tb_seckill_order where user_id=#{username} and seckill_id=#{id}")
    SeckillOrder getOrderInfoByUserNameAndGoodsId(@Param("username") String username, @Param("id") Long id);
}
