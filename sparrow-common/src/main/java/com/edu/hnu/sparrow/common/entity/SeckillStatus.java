package com.edu.hnu.sparrow.common.entity;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * 用户排队抢单信息
 */
public class SeckillStatus {
    private String userName;

    private Date createTime;

    //秒杀状态 1. 成功 2 排队 3 等待支付(已经下单了) 4 支付超时  5 秒杀失败  6 支付完成
    private Integer status;

    //作用于超时订单回滚
    private Long goodsID;

    //这里用这个数据类型存钱可能会有问题吧！
    private BigDecimal money;

    private Long orderID;

    //时区
    private String time;

    //不推荐构造方法重载，只需要空构造方法，然后getter和setter方法就够了？
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getGoodsID() {
        return goodsID;
    }

    public void setGoodsID(Long goodsID) {
        this.goodsID = goodsID;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Long getOrderID() {
        return orderID;
    }

    public void setOrderID(Long orderID) {
        this.orderID = orderID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}