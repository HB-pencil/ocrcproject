package com.example.shinelon.ocrcamera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by Shinelon on 2017/4/2.自定义有边框ImageView
 */

public class myImageView extends AppCompatImageView {
    private Paint mPaint;
    private int borderColor;
    private float borderWith;

    public myImageView(Context context, AttributeSet attrs){
        super(context,attrs);
        init(attrs);
    }

    /**
     * 重新绘制mageView
     * @param canvas
     */
    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        //Retrieve the bounds of the current clip (in local coordinates).
        Rect rect = canvas.getClipBounds();
        rect.bottom --;
        rect.right --;
        //画边框
        canvas.drawRect(rect,mPaint);
    }
    public void init(AttributeSet attrs){

        if(attrs != null){
            //获取自定义属性
            TypedArray array = getContext().obtainStyledAttributes(attrs,R.styleable.myImageView);
            borderColor = array.getColor(R.styleable.myImageView_borderColor,0x000000);
            borderWith = array.getDimension(R.styleable.myImageView_borderWith,2);
            //初始化画笔
            mPaint = new Paint();
            mPaint.setColor(borderColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(borderWith);
            //回收array;
            array.recycle();
        }
    }
}
