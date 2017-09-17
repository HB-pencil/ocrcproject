package com.example.shinelon.ocrcamera.helper;

/**
 * Created by Shinelon on 2017/9/16.
 */

public class UpdateInfo {

    private int code;
    private DataBean data;
    private Boolean success;
    private String message;

    public void setCode(int code) {
        this.code = code;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public DataBean getData() {
        return data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public static class DataBean{
        private String appName;
        private int isForce;
        private int latestVersionCode;
        private String latestVersionName;
        private String updateUrl;
        private String updateInfo;
        private String updateTime;

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public void setIsForce(int isForce) {
            this.isForce = isForce;
        }

        public void setLatestVersionCode(int latestVersionCode) {
            this.latestVersionCode = latestVersionCode;
        }

        public void setLatestVersionName(String latestVersionName) {
            this.latestVersionName = latestVersionName;
        }

        public void setUpdateUrl(String updateUrl) {
            this.updateUrl = updateUrl;
        }

        public void setUpdateInfo(String updateInfo) {
            this.updateInfo = updateInfo;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public String getAppName() {
            return appName;
        }

        public int getIsForce() {
            return isForce;
        }

        public int getLatestVersionCode() {
            return latestVersionCode;
        }

        public String getLatestVersionName() {
            return latestVersionName;
        }

        public String getUpdateUrl() {
            return updateUrl;
        }

        public String getUpdateInfo() {
            return updateInfo;
        }

        public String getUpdateTime() {
            return updateTime;
        }
    }
}
