package com.edu.hnu.sparrow.common.conts;

/****
 * redis key
 *****/
public class CacheKey {
    /**
     * 秒杀商品存储到前缀的KEY
     * 实际存储的是这个+时间
     * 其实不要这个也可以，只不过加上的话专业一点
     */

    public static final String SEC_KILL_GOODS_PREFIX="SeckillGoods_";


    /**
     * 每个商品秒杀令牌
     */
    public static final String SECKILL_LINPAI="seckill_lingpai";

    /**
     * 用户排队的队列的KEY
     */
    public static final String SEC_KILL_USER_PAIDUI="user_paidui";


    /**
     * 用户排队标识的key (用于存储 谁 买了什么商品 以及抢单的状态)
     * 这个是防止上个订单还没下单，反复排队的！
     * 也是必须的
     */

    public static final String SECKILL_USER_CHONGFU_PAIDUI = "user_chongfu_paidui";


    /**
     * 用于预购的hash的key
     */
    public static final String SECKILL_QUEUE_REPEAT="seckill_repeat";


    /**
     * 防止超卖的问题的 队列的key
     */
    public static final String SECKILL_GOODS_CHAOMAI="seckill_goods_chaomai";


    /**
     * 所有的商品计数的大的key(用于存储所有的 商品 对应的 库存 数据)
     *
     * bigkey    field1(商品ID 1)    value(库存数2)
     *           field1(商品ID 2)    value(库存数5)
     */
    public static final String SECKKILL_GOODS_KUCUN = "seckill_goods_kucun";

    /**
     * 用户jtw hash结构的key
     */

    public static final String USER_JWT="user_jwt";



}
