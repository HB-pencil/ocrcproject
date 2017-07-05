package com.example.shinelon.ocrcamera.helper;

/**
 * Created by Shinelon on 2017/7/5.
 */

public class JavaBean {

    /**
     * code : 200
     * success : true
     * message : 登录成功
     * user : {"userId":"1b8f1ce2-0d67-4b0f-a16f-be1d31f1832c","username":"carway","phone":"15692011935"}
     * token : eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIxYjhmMWNlMi0wZDY3LTRiMGYtYTE2Zi1iZTFkMzFmMTgzMmMiLCJleHAiOjE0OTkyMjYxMjksImlhdCI6MTQ5OTIxNTMyOX0.cEGsDFoM8HSGl5jeGC7fyU-mjfIqdxpQw3pqrCxHlQA
     */

    private int code;
    private boolean success;
    private String message;
    private UserBean user;
    private String token;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class UserBean {
        /**
         * userId : 1b8f1ce2-0d67-4b0f-a16f-be1d31f1832c
         * username : carway
         * phone : 15692011935
         */

        private String userId;
        private String username;
        private String phone;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
