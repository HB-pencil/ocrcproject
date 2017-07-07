package com.example.shinelon.ocrcamera.helper;

/**
 * Created by Shinelon on 2017/7/7.
 */

public class UserInfoLab {
    private String phone;
    private String name;

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    private String userId;
    private UserInfoLab(){

    }
    public static UserInfoLab getUserInfo(){
        return new UserInfoLab();
    }
}
