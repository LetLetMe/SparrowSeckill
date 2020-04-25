package com.edu.hnu.sparrow.service.user.pojo;

//这俩个是tkmapper帮你依赖的
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * user实体类
 * @author 黑马架构师2.5
 *
 */
@Table(name="tb_user")
public class User implements Serializable {

    @Id
    private String username;//用户名



    private String password;//密码，加密存储
    private String phone;//注册手机号
    private String email;//注册邮箱
    private java.util.Date created;//创建时间
    private java.util.Date updated;//修改时间
    private String sourceType;//会员来源：1:PC，2：H5，3：Android，4：IOS
    private String nickName;//昵称
    private String name;//真实姓名
    private String status;//使用状态（1正常 0非正常）
    private String headPic;//头像地址
    private String qq;//QQ号码
    private String isMobileCheck;//手机是否验证 （0否  1是）
    private String isEmailCheck;//邮箱是否检测（0否  1是）
    private String sex;//性别，1男，0女
    private Integer userLevel;//会员等级
    private Integer points;//积分
    private Integer experienceValue;//经验值
    private java.util.Date birthday;//出生年月日
    private java.util.Date lastLoginTime;//最后登录时间

    private String salt;//盐值，md5加密是不能反解的，只能再加密一次验证一样不一样

    public String getSalt() { return salt; }

    public void setSalt(String salt) { this.salt = salt; }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public java.util.Date getCreated() {
        return created;
    }
    public void setCreated(java.util.Date created) {
        this.created = created;
    }

    public java.util.Date getUpdated() {
        return updated;
    }
    public void setUpdated(java.util.Date updated) {
        this.updated = updated;
    }

    public String getSourceType() {
        return sourceType;
    }
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getHeadPic() {
        return headPic;
    }
    public void setHeadPic(String headPic) {
        this.headPic = headPic;
    }

    public String getQq() {
        return qq;
    }
    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getIsMobileCheck() {
        return isMobileCheck;
    }
    public void setIsMobileCheck(String isMobileCheck) {
        this.isMobileCheck = isMobileCheck;
    }

    public String getIsEmailCheck() {
        return isEmailCheck;
    }
    public void setIsEmailCheck(String isEmailCheck) {
        this.isEmailCheck = isEmailCheck;
    }

    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getUserLevel() {
        return userLevel;
    }
    public void setUserLevel(Integer userLevel) {
        this.userLevel = userLevel;
    }

    public Integer getPoints() {
        return points;
    }
    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getExperienceValue() {
        return experienceValue;
    }
    public void setExperienceValue(Integer experienceValue) {
        this.experienceValue = experienceValue;
    }

    public java.util.Date getBirthday() {
        return birthday;
    }
    public void setBirthday(java.util.Date birthday) {
        this.birthday = birthday;
    }

    public java.util.Date getLastLoginTime() {
        return lastLoginTime;
    }
    public void setLastLoginTime(java.util.Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }



}
