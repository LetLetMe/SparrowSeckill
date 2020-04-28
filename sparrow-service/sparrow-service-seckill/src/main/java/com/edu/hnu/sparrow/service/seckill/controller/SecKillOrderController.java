package com.edu.hnu.sparrow.service.seckill.controller;



import com.edu.hnu.sparrow.common.conts.Auth;
import com.edu.hnu.sparrow.common.entity.Result;
import com.edu.hnu.sparrow.common.entity.SeckillStatus;
import com.edu.hnu.sparrow.common.entity.StatusCode;
import com.edu.hnu.sparrow.service.seckill.service.SecKillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seckillorder")
public class SecKillOrderController {

//    @Autowired
//    private TokenDecode tokenDecode;

    @Autowired
    private SecKillOrderService secKillOrderService;

    @GetMapping("/add/{time}/{id}")
    public Result add(@PathVariable("time") String time,
                      @PathVariable("id") Long id){
//                      @RequestHeader(value= Auth.AUTHORIZATION) String auth){
        //1.动态获取到当前的登录人
        //这里是根据cookie来解密获取username的，这里怎么获取到cookie的呢？
//        String username = tokenDecode.getUserInfo().get("username");
        //上边这个怎么玩？
        String username = "allen";

        //2.基于业务层进行秒杀下单
        boolean result = secKillOrderService.add(id, time, username);

        //3.返回结果
        if (result){
            //下单成功
            return new Result(true, StatusCode.OK,"排队成功");
        }else{
            //下单失败
            return new Result(false,StatusCode.ERROR,"排队失败");
        }
    }

    /**
     * 查询状态 0 用户没有下单 1 结账  2 排队中 3 下单成功未结账 4 超时订单
     * @return
     */
    @PostMapping("/status/{username}/")
    public Result<SeckillStatus> status(@RequestParam("username") String username ){
        //username是从token中反解出来的
//        String username = tokenDecode.getUserInfo().get("username");
//        String username = "huyitao";
        SeckillStatus seckillStatus =secKillOrderService.status(username);

        if(seckillStatus==null){return new Result<SeckillStatus>(true,StatusCode.OK,"查询状态失败",seckillStatus);}

        //需要返回一个seckillstatus对象的，前端要根据这个对象展示订单信息，并且完成跳转
        //前端要根据status来显示抢单信息
        return new Result<SeckillStatus>(true,StatusCode.OK,"查询状态成功",seckillStatus);
    }
}
