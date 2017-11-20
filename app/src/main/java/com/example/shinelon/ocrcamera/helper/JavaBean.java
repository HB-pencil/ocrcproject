package com.example.shinelon.ocrcamera.helper;

/**
 * Created by Shinelon on 2017/7/5.
 */

public class JavaBean {

    /**
     * code : 200
     * success : true
     * message : 登录成功
     * data : {"userId":"1b8f1ce2-0d67-4b0f-a16f-be1d31f1832c","username":"carway","phone":"15692011935"}
     * token : eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIxYjhmMWNlMi0wZDY3LTRiMGYtYTE2Zi1iZTFkMzFmMTgzMmMiLCJleHAiOjE0OTkyMjYxMjksImlhdCI6MTQ5OTIxNTMyOX0.cEGsDFoM8HSGl5jeGC7fyU-mjfIqdxpQw3pqrCxHlQA
     */

    private int code;
    private boolean success;
    private String message;
    private DataBean data;
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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class DataBean {
        /**
         * userId : 1b8f1ce2-0d67-4b0f-a16f-be1d31f1832c
         * username : carway
         * userPhone : 15692011935
         * userEmail:hardblack@aliyun.com
         **/
        private String userId;
        private String username;
        private String userPhone;
        private String userEmail;

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {

            this.userEmail = userEmail;
        }

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

        public String getUserPhone() {
            return userPhone;
        }

        public void setUserPhone(String userPhone) {
            this.userPhone = userPhone;
        }
    }
}
