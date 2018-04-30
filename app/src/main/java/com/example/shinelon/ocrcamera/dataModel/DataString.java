package com.example.shinelon.ocrcamera.dataModel;

/**
 * Created by Shinelon on 2017/11/27.
 */

public class DataString {
    private String itemString;
    private int x;
    private int y;

    public void setItemString(String itemString) {
        this.itemString = itemString;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getItemString() {
        return itemString;
    }

    public int getXY(int or) {
        if(or==1){
            return x;
        }else {
            return y;
        }
    }
}
