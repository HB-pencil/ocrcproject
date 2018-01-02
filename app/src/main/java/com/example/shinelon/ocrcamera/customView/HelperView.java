package com.example.shinelon.ocrcamera.customView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Shinelon on 2017/12/15.自定义辅助View
 */

public class HelperView extends View {
    private Paint paint = new Paint();
    private Path path = new Path();
    public HelperView(Context context){
        this(context,null);
    }
    public HelperView(Context context, AttributeSet set){
        super(context,set,0);
        paint.setColor(Color.BLUE);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5F);
        paint.setPathEffect(new DashPathEffect(new float[]{20F,10F},0F));
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;
        layoutParams.width = (int) (w * 0.85);
        layoutParams.height =(int) (h * 0.70);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        setLayoutParams(layoutParams);
        setMeasuredDimension(layoutParams.width,layoutParams.height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.moveTo(5F,5F);
        path.lineTo(getWidth()-5F,5F);
        path.lineTo(getWidth()-5F,getHeight()-5F);
        path.lineTo(5F,getHeight()-5F);
        path.close();
        canvas.drawPath(path,paint);
    }
}
