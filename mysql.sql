create database sparrow_seckill;
use sparrow_seckill;
DROP TABLE IF EXISTS `tb_pay_order` ;
CREATE TABLE `tb_pay_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `good_id` bigint(20) DEFAULT NULL COMMENT 'good ID',
  `user_name` varchar(100) DEFAULT NULL COMMENT '用户名',
  `create_time` datetime DEFAULT NULL COMMENT '添加日期',
  `phone` varchar(11) DEFAULT NULL COMMENT '电话',
  `address` varchar(2000) DEFAULT NULL COMMENT '用户地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11058 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `tb_seckill_goods`;
CREATE TABLE `tb_seckill_goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_id` bigint(20) DEFAULT NULL COMMENT 'spu ID',
  `item_id` bigint(20) DEFAULT NULL COMMENT 'sku ID',
  `title` varchar(100) DEFAULT NULL COMMENT '标题',
  `small_pic` varchar(150) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) DEFAULT NULL COMMENT '原价格',
  `cost_price` decimal(10,2) DEFAULT NULL COMMENT '秒杀价格',
  `seller_id` varchar(100) DEFAULT NULL COMMENT '商家ID',
  `create_time` datetime DEFAULT NULL COMMENT '添加日期',
  `check_time` datetime DEFAULT NULL COMMENT '审核日期',
  `status` char(1) DEFAULT NULL COMMENT '审核状态，0未审核，1审核通过，2审核不通过',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `num` int(11) DEFAULT NULL COMMENT '秒杀商品数',
  `stock_count` int(11) unsigned DEFAULT NULL COMMENT '剩余库存数',
  `introduction` varchar(2000) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11058 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `tb_seckill_order`;
CREATE TABLE `tb_seckill_order` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `seckill_id` bigint(20) DEFAULT NULL COMMENT '秒杀商品ID',
  `money` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
  `user_id` varchar(50) DEFAULT NULL COMMENT '用户',
  `seller_id` varchar(50) DEFAULT NULL COMMENT '商家',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `status` char(1) DEFAULT NULL COMMENT '状态，0未支付，1已支付',
  `receiver_address` varchar(200) DEFAULT NULL COMMENT '收货人地址',
  `receiver_mobile` varchar(20) DEFAULT NULL COMMENT '收货人电话',
  `receiver` varchar(20) DEFAULT NULL COMMENT '收货人',
  `transaction_id` varchar(30) DEFAULT NULL COMMENT '交易流水',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
