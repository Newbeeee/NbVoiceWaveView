package com.newbeeee.qt.nbvoicewaveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by xiuxiongding on 2017/4/7.
 */

public class VoiceWaveView extends View {

    private static final int DEFAULT_MAX_VOLUME = 100;
    private static final int DEFAULT_WAVE_COLOR = Color.parseColor("#4fe7ef");
    private static final int DEFAULT_ADJUST_VALUE = 50;


    // 默认声音最大值
    private float maxVolume = DEFAULT_MAX_VOLUME;
    // 波浪颜色
    private int waveColor = DEFAULT_WAVE_COLOR;
    // 可以调整波浪高度在屏幕的显示
    private int adjustValue = DEFAULT_ADJUST_VALUE;

    private Paint mBitPaint;
    private static Bitmap mBitmap;
    // voiceWaveView 的宽、高
    private int mTotalWidth, mTotalHeight;
    // 绘制的背景图片的区域，绘制在屏幕的区域
    private Rect mSrcRect, mDstRect;
    // 动态波浪 rect
    private Rect mDynamicRect;
    // 波浪 rect 目前的 top
    private int mCurrentTop;
    // 波浪高度百分比
    private float waveHeightPer = 0;


    public VoiceWaveView(Context context) {
        super(context);
        initPaint();
    }

    public VoiceWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        initPaint();
    }


    public VoiceWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.voiceWaveView);
        if (typedArray != null) {
            int n = typedArray.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = typedArray.getIndex(i);
                switch (attr) {
                    case R.styleable.voiceWaveView_imageSrc:
                        mBitmap = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(attr, 0));
                        break;
                }
            }
            maxVolume = typedArray.getFloat(R.styleable.voiceWaveView_maxVolume, DEFAULT_MAX_VOLUME);
            waveColor = typedArray.getColor(R.styleable.voiceWaveView_waveColor, DEFAULT_WAVE_COLOR);
            adjustValue = typedArray.getInteger(R.styleable.voiceWaveView_adjustValue, DEFAULT_ADJUST_VALUE);
            typedArray.recycle();
        }
    }

    private void initPaint() {
        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);
        mBitPaint.setColor(waveColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        } else{
            int desired = getPaddingLeft() + getPaddingRight();
            desired += (mBitmap != null) ? mBitmap.getWidth():0;
            width = desired;
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desired, widthSize);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            int desired = getPaddingTop() + getPaddingBottom();
            desired += (mBitmap != null) ? mBitmap.getHeight():0;
            height = desired;
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desired, heightSize);
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int saveLayerCount = canvas.saveLayer(0, 0, mTotalWidth, mTotalHeight, mBitPaint,
                Canvas.ALL_SAVE_FLAG);
        /**
         *  @param mSrcRect 表示要绘制的 bitmap 区域
         *  @param mDstRect 表示要绘制在屏幕的什么地方
         */
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, mBitPaint);
            // 原图片为 dst，新画的 mDynamicRect 为 src
            mBitPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawRect(mDynamicRect, mBitPaint);
            mBitPaint.setXfermode(null);
            canvas.restoreToCount(saveLayerCount);
            // top 为 0 和 计算值的最大值，如果计算值为负数，则 top 为 0 , 即声波占满图片
            mCurrentTop = (int) Math.max((mTotalHeight - waveHeightPer / adjustValue * mTotalHeight), 0);
            mDynamicRect.top = mCurrentTop;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w;
        mTotalHeight = h;
        /**
         *  从原来的 bitmap 创建一个根据需要伸缩的 bitmap
         *  @param mTotalWidth 期望的图片宽度
         *  @param mTotalHeight 期望的图片高度
         *  @param true 设置之后图片质量不会变差
         */
        if (mBitmap != null) {
            mBitmap = Bitmap.createScaledBitmap(mBitmap, mTotalWidth, mTotalHeight, true);
        }
        mSrcRect = new Rect(0, 0, mTotalWidth, mTotalHeight);
        mDstRect = new Rect(0, 0, mTotalWidth, mTotalHeight);
        mCurrentTop = mTotalHeight;
        mDynamicRect = new Rect(0, mCurrentTop, mTotalWidth, mTotalHeight);
    }

    public void setVolume(int volume) {
        waveHeightPer = (float) (volume + 0.0 / maxVolume);
        invalidate();
    }
}
