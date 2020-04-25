package com.edu.hnu.sparrow.common.entity;

import java.io.Serializable;


public class AuthToken implements Serializable{

    //令牌信息 jwt
    String accessToken;

    //jwt短令牌
    String jti;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }



    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }
}