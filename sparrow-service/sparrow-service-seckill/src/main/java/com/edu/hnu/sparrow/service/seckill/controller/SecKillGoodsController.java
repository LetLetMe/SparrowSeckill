package com.edu.hnu.sparrow.service.seckill.controller;


import com.edu.hnu.sparrow.common.entity.Result;
import com.edu.hnu.sparrow.common.entity.StatusCode;
import com.edu.hnu.sparrow.common.util.DateUtil;
import com.edu.hnu.sparrow.service.seckill.pojo.SeckillGoods;
import com.edu.hnu.sparrow.service.seckill.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/seckillgoods")
public class SecKillGoodsController {

    @Autowired
    private SecKillGoodsService secKillGoodsService;


    //这里原版的获取主页的方式不是获取静态主页，而是从redis中查询指定时间范围内的数据
    @RequestMapping("/list/{time}")
    public Result<List<SeckillGoods>> list(@PathVariable("time") String time){
        List<SeckillGoods> seckillGoodsList = secKillGoodsService.list(time);
        return new Result<>(true, StatusCode.OK,"查询成功",seckillGoodsList);
    }

    //自己写时间菜单接口，让后端来调用，当然这里的时间具体格式有些出入的化，前端是可以转换格式的
    @GetMapping("/manues")
    public Result<List<Date>> manues(){
        List<Date> dates= DateUtil.getDateMenus();
        return new Result<List<Date>>(true,StatusCode.OK,"查询秒杀列表成功",dates);
    }

    @PostMapping("/one/{time}/{id}")
    //根据时间和id查询商品详情
    public Result<SeckillGoods> one(@PathVariable("time") String time, @PathVariable("id") Long id){
        return new Result<SeckillGoods>(true,StatusCode.OK,"查询商品详情成功",secKillGoodsService.one(time,id));
    }

    @GetMapping("/newData")
    public Result<SeckillGoods> newDate(){
        Date date=new Date();
        SeckillGoods seckillGoods=secKillGoodsService.addGoods(date);
        if(seckillGoods==null){
            return new Result<SeckillGoods>(true,StatusCode.OK,"添加商品失败",null);
        }else {
            return new Result<SeckillGoods>(true,StatusCode.OK,"添加商品成功",seckillGoods);
        }

    }


}

