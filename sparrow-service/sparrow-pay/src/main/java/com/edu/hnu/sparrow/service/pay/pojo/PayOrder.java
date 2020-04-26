package com.edu.hnu.sparrow.service.pay.pojo;


import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Table(name = "tb_pay_order")
public class PayOrder {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "good_id")
    private String goodId;

    /**
     * sku ID
     */
    @Column(name = "user_name")
    private String userName;
    /**
     * sku ID
     */
    @Column(name = "create_time")
    private Date createTime;
    /**
     * sku ID
     */
    @Column(name = "phone")
    private String phone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoodId() {
        return goodId;
    }

    public void setGoodId(String goodId) {
        this.goodId = goodId;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "PayOrder{" +
                "id=" + id +
                ", goodId=" + goodId +
                ", userName='" + userName + '\'' +
                ", createTime=" + createTime +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    /**
     * sku ID
     */
    @Column(name = "address")
    private String address;


}
