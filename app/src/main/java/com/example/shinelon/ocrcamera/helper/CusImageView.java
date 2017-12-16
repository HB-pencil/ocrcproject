package com.example.shinelon.ocrcamera.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by Shinelon on 2017/12/16.
 */

public class CusImageView extends ImageView {
    private Paint mPaint = new Paint();
    private RectF rectF;
    private Matrix matrix = new Matrix();
    private BitmapShader bitmapShader;
    public CusImageView(Context context){
        super(context);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5F);
        mPaint.setAntiAlias(true);
        rectF = new RectF(2.5F,2.5F,getWidth()-2.5F,getHeight()-2.5F);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable drawable = getDrawable();
        if (drawable==null){
            return;
        }else {
            Bitmap bitmap = drawable2Bitmap(drawable);
            float scale = Math.max(getMeasuredWidth()*1.0f/bitmap.getWidth(),getMeasuredHeight()*1.0f/bitmap.getHeight());
            matrix.setScale(scale,scale);
            initBitmap(bitmap);
            canvas.drawRect(rectF,mPaint);
        }
    }

    private void initBitmap(Bitmap bitmap){
        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapShader.setLocalMatrix(matrix);
        mPaint.setShader(bitmapShader);
    }

    private Bitmap drawable2Bitmap(Drawable drawable){
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(width,height,config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0,0,width,height);
        drawable.draw(canvas);
        return bitmap;
    }


}
