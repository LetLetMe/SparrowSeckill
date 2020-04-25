package com.edu.hnu.sparrow.service.user.service.impl;


import com.alibaba.fastjson.JSON;


import com.edu.hnu.sparrow.common.util.CodecUtil;
import com.edu.hnu.sparrow.common.util.NumberUtil;
import com.edu.hnu.sparrow.service.user.dao.UserMapper;
import com.edu.hnu.sparrow.service.user.pojo.User;
import com.edu.hnu.sparrow.service.user.service.UserService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AmqpTemplate amqpTemplate;


    @Autowired
    private UserMapper userMapper;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<User> findAll() {
        return userMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param username
     * @return
     */
    @Override
    public User findById(String username){
        return  userMapper.selectByPrimaryKey(username);
    }


    /**
     * 增加
     * @param user
     */
    @Override
    public void add(User user){
        userMapper.insert(user);
    }


    /**
     * 修改
     * @param user
     */
    @Override
    public void update(User user){
        userMapper.updateByPrimaryKey(user);
    }

    /**
     * 删除
     * @param username
     */
    @Override
    public void delete(String username){
        userMapper.deleteByPrimaryKey(username);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<User> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return userMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<User> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<User>)userMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<User> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<User>)userMapper.selectByExample(example);
    }


    @Autowired
    private RedisTemplate redisTemplate;



    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 用户名
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andEqualTo("username",searchMap.get("username"));
            }
            // 密码，加密存储
            if(searchMap.get("password")!=null && !"".equals(searchMap.get("password"))){
                criteria.andEqualTo("password",searchMap.get("password"));
            }
            // 注册手机号
            if(searchMap.get("phone")!=null && !"".equals(searchMap.get("phone"))){
                criteria.andLike("phone","%"+searchMap.get("phone")+"%");
            }
            // 注册邮箱
            if(searchMap.get("email")!=null && !"".equals(searchMap.get("email"))){
                criteria.andLike("email","%"+searchMap.get("email")+"%");
            }
            // 会员来源：1:PC，2：H5，3：Android，4：IOS
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andEqualTo("sourceType",searchMap.get("sourceType"));
            }
            // 昵称
            if(searchMap.get("nickName")!=null && !"".equals(searchMap.get("nickName"))){
                criteria.andLike("nickName","%"+searchMap.get("nickName")+"%");
            }
            // 真实姓名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 使用状态（1正常 0非正常）
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
            }
            // 头像地址
            if(searchMap.get("headPic")!=null && !"".equals(searchMap.get("headPic"))){
                criteria.andLike("headPic","%"+searchMap.get("headPic")+"%");
            }
            // QQ号码
            if(searchMap.get("qq")!=null && !"".equals(searchMap.get("qq"))){
                criteria.andLike("qq","%"+searchMap.get("qq")+"%");
            }
            // 手机是否验证 （0否  1是）
            if(searchMap.get("isMobileCheck")!=null && !"".equals(searchMap.get("isMobileCheck"))){
                criteria.andEqualTo("isMobileCheck",searchMap.get("isMobileCheck"));
            }
            // 邮箱是否检测（0否  1是）
            if(searchMap.get("isEmailCheck")!=null && !"".equals(searchMap.get("isEmailCheck"))){
                criteria.andEqualTo("isEmailCheck",searchMap.get("isEmailCheck"));
            }
            // 性别，1男，0女
            if(searchMap.get("sex")!=null && !"".equals(searchMap.get("sex"))){
                criteria.andEqualTo("sex",searchMap.get("sex"));
            }

            // 会员等级
            if(searchMap.get("userLevel")!=null ){
                criteria.andEqualTo("userLevel",searchMap.get("userLevel"));
            }
            // 积分
            if(searchMap.get("points")!=null ){
                criteria.andEqualTo("points",searchMap.get("points"));
            }
            // 经验值
            if(searchMap.get("experienceValue")!=null ){
                criteria.andEqualTo("experienceValue",searchMap.get("experienceValue"));
            }

        }
        return example;
    }

    @Override
    public boolean checkData(String data, Integer type) {
        User record = new User();
        switch (type) {
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                return false;
        }
        return this.userMapper.selectCount(record) == 0;
    }


    static final String KEY_PREFIX = "user:code:phone:";

    static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public boolean sendVerifyCode(String phone) {
        // 生成验证码
        String code = NumberUtil.generateCode(6);
        try {
            // 发送短信
            Map<String, String> msg = new HashMap<>();
            msg.put("phone", phone);
            msg.put("code", code);

            this.amqpTemplate.convertAndSend("mymall.sms.exchange", "sms.verify.code", msg);

            // 将code存入redis
            this.redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);
            return true;

        } catch (Exception e) {
            logger.error("发送短信失败。phone：{}， code：{}", phone, code);
            return false;
        }
    }
    @Override
    public boolean register(User user, String code) {
        String key = KEY_PREFIX + user.getPhone();
        // 从redis取出验证码
        String codeCache = (String)this.redisTemplate.opsForValue().get(key);
        // 检查验证码是否正确
        if (!code.equals(codeCache)) {
            // 不正确，返回
            return false;
        }
//        user.setId(null);
        user.setCreated(new Date());
        // 生成盐
        String salt = CodecUtil.generateSalt();
        user.setSalt(salt);
        // 对密码进行加密
        user.setPassword(CodecUtil.md5Hex(user.getPassword(), salt));
        // 写入数据库
        boolean boo = this.userMapper.insertSelective(user) == 1;

        // 如果注册成功，删除redis中的code
        if (boo) {
            try {
                this.redisTemplate.delete(key);
            } catch (Exception e) {
                logger.error("删除缓存验证码失败，code：{}", code, e);
            }
        }
        return boo;
    }

    @Override
    public User queryUser(String username, String password) {
        // 查询
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        // 校验用户名
        if (user == null) {
            return null;
        }
        // 校验密码
        //数据库存的是盐值和加密后的密码，md5算法是不能反向破解的
        if (!user.getPassword().equals(CodecUtil.md5Hex(password, user.getSalt()))) {
            return null;
        }
        // 用户名密码都正确
        return user;
    }

}

