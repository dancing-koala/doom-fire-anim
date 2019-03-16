package com.dancing_koala.fireanim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

import androidx.annotation.Nullable;

public class FireView extends View {

    // Colors used to obtain the fire looking gradient
    private static final int[] COLOR_BUFFER = {
            0x00070707, 0xFF1F0707, 0xFF2F0F07, 0xFF470F07, 0xFF571707, 0xFF671F07, 0xFF771F07,
            0xFF8F2707, 0xFF9F2F07, 0xFFAF3F07, 0xFFBF4707, 0xFFC74707, 0xFFDF4F07, 0xFFDF5707,
            0xFFDF5707, 0xFFD75F07, 0xFFD75F07, 0xFFD7670F, 0xFFCF6F0F, 0xFFCF770F, 0xFFCF7F0F,
            0xFFCF8717, 0xFFC78717, 0xFFC78F17, 0xFFC7971F, 0xFFBF9F1F, 0xFFBF9F1F, 0xFFBFA727,
            0xFFBFA727, 0xFFBFAF2F, 0xFFB7AF2F, 0xFFB7B72F, 0xFFB7B737, 0xFFCFCF6F, 0xFFDFDF9F,
            0xFFEFEFC7, 0xFFFFFFFF
    };

    private static final long FRAME_DELAY = 1000L / 27L;

    private final Runnable mAnimRunnable = new Runnable() {
        @Override
        public void run() {
            update();
            mHandler.postDelayed(this, FRAME_DELAY);
        }
    };
    private final Handler mHandler;
    private final Paint mFirePaint;
    private final Random mProgressRandom;
    private final int mFireWidth;
    private final int mFireHeight;

    private int[] mFirePixelColors;
    private int[] mFirePixelBuffer;

    private Bitmap mFireBitmap;
    private int mHeight;
    private float mPixelScale;
    private int mWindOffset;
    private boolean mPlaying;


    public FireView(Context context) {
        this(context, null, 0);
    }

    public FireView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FireView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(Color.TRANSPARENT);

        mHandler = new Handler();
        mFirePaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        mProgressRandom = new Random();

        mFireWidth = 280;
        mFireHeight = 168;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mHeight = h;

        mPixelScale = (float) w / (float) mFireWidth;

        if (mFireBitmap != null) {
            mFireBitmap.recycle();
        }

        mFireBitmap = Bitmap.createBitmap(mFireWidth, mFireHeight, Bitmap.Config.ARGB_8888);

        initFire(mFireWidth, mFireHeight);
        updateBitmap();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.save();
        canvas.scale(mPixelScale, mPixelScale, 0, mHeight);
        canvas.drawBitmap(mFireBitmap, 0, mHeight - mFireHeight, mFirePaint);
        canvas.restore();
    }

    private void initFire(int w, int h) {
        mFirePixelColors = new int[w * h];
        mFirePixelBuffer = new int[w * h];

        for (int i = 0; i < mFirePixelColors.length - mFireWidth; i++) {
            setPixelColor(i, 0);
        }

        for (int x = 0; x < mFireWidth; x++) {
            setPixelColor((mFireHeight - 1) * mFireWidth + x, COLOR_BUFFER.length - 1);
        }
    }

    private void fire() {
        for (int x = 0; x < mFireWidth; x++) {
            for (int y = 1; y < mFireHeight; y++) {
                spreadFire(y * mFireWidth + x);
            }
        }
    }

    private void spreadFire(int src) {
        int pixel = mFirePixelColors[src];

        if (pixel == 0) {
            setPixelColor(src - mFireWidth, 0);
        } else {
            int randIndex = mProgressRandom.nextInt(4);
            int dst = src - randIndex + 1 + mWindOffset;
            setPixelColor(dst - mFireWidth, pixel - (randIndex & 1));
        }
    }

    private void updateBitmap() {
        if (mFireBitmap == null) {
            return;
        }

        mFireBitmap.setPixels(mFirePixelBuffer, 0, mFireWidth, 0, 0, mFireWidth, mFireHeight);
    }

    private void setPixelColor(int index, int colorIndex) {
        mFirePixelColors[index] = colorIndex;
        mFirePixelBuffer[index] = COLOR_BUFFER[colorIndex];
    }

    public void update() {
        fire();
        updateBitmap();
        invalidate();
    }

    public void reset() {
        if (mPlaying) {
            mHandler.removeCallbacks(mAnimRunnable);
            mPlaying = false;
        }

        mWindOffset = 0;
        initFire(mFireWidth, mFireHeight);
        updateBitmap();
        invalidate();
    }

    public void kill() {
        for (int x = 0; x < mFireWidth; x++) {
            int index = (mFireHeight - 1) * mFireWidth + x;

            if (mFirePixelColors[index] > 0) {
                setPixelColor(index, mFirePixelColors[index] - mProgressRandom.nextInt(4) & 3);
            }
        }
    }

    public void wind(int direction) {
        mWindOffset = mWindOffset != 0 ? 0 : direction;
    }

    public void play() {
        if (mPlaying) {
            kill();
        } else {
            mPlaying = true;
            mAnimRunnable.run();
        }
    }
}
