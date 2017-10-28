package com.example.shinelon.ocrcamera.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;


/**
 * Created by Shinelon on 2017/10/27.
 */

public class CircleImageView extends ImageView {
    private int mRadius;
    private Paint mPaint;
    private BitmapShader bitmapShader;
    private Matrix matrix;

    public CircleImageView(Context context){
          this(context,null);
    }
    public CircleImageView(Context context, AttributeSet set){
          this(context,set,0);
    }
    public CircleImageView(Context context,AttributeSet set,int defStyle){
          super(context,set,defStyle);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mRadius = Math.min(width,height)/2;
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (getDrawable()==null){
            return;
        }
        initView();
        canvas.drawCircle(mRadius,mRadius,mRadius,mPaint);
    }

    public void initView(){
        mPaint = new Paint();
        matrix = new Matrix();
        mPaint.setAntiAlias(true);
        Drawable drawable = getDrawable();
        Log.e("Drawable",(drawable==null)+"");
        Bitmap bitmap = drawableTobitmap(drawable);
        float scale = Math.max(getMeasuredWidth()*1.0f/bitmap.getWidth(),getMeasuredHeight()*1.0f/bitmap.getHeight());
        matrix.setScale(scale,scale);
        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapShader.setLocalMatrix(matrix);
        mPaint.setShader(bitmapShader);
    }

    public Bitmap drawableTobitmap(Drawable drawable){
       int width = drawable.getIntrinsicWidth();
       int height = drawable.getIntrinsicHeight();
       Bitmap.Config config = drawable.getOpacity()!= PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
       Bitmap bitmap = Bitmap.createBitmap(width,height,config);
       Canvas canvas = new Canvas(bitmap);
       drawable.setBounds(0,0,width,height);
       drawable.draw(canvas);
       return bitmap;
    }
}
