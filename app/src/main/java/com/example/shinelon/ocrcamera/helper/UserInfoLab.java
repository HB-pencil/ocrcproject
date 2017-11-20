package com.example.shinelon.ocrcamera.helper;

/**
 * Created by Shinelon on 2017/7/7.
 */

public class UserInfoLab {

    private String phone;
    private String name;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    private static UserInfoLab mUserInfoLab;

    private UserInfoLab(){}

    public static UserInfoLab getUserInfo(){
        if(mUserInfoLab == null){
            mUserInfoLab = new UserInfoLab();
        }
        return  mUserInfoLab;
    }

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
}
