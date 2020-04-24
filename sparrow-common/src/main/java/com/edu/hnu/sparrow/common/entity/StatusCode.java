package com.edu.hnu.sparrow.common.entity;


/**
 * 返回码
 */
public class StatusCode {
    //静态变量和枚举类有什么区别啊？

    public static final int OK=20000;//成功
    public static final int ERROR =20001;//失败
    public static final int LOGINERROR =20002;//用户名或密码错误
    public static final int ACCESSERROR =20003;//权限不足
    public static final int REMOTEERROR =20004;//远程调用失败
    public static final int REPERROR =20005;//重复操作

}
