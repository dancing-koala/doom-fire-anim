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

    // COLOR_BUFFER contains the colors used to obtain the fire looking gradient
    private static final int[] COLOR_BUFFER = {
            0x00070707, 0xFF1F0707, 0xFF2F0F07, 0xFF470F07, 0xFF571707, 0xFF671F07, 0xFF771F07,
            0xFF8F2707, 0xFF9F2F07, 0xFFAF3F07, 0xFFBF4707, 0xFFC74707, 0xFFDF4F07, 0xFFDF5707,
            0xFFDF5707, 0xFFD75F07, 0xFFD75F07, 0xFFD7670F, 0xFFCF6F0F, 0xFFCF770F, 0xFFCF7F0F,
            0xFFCF8717, 0xFFC78717, 0xFFC78F17, 0xFFC7971F, 0xFFBF9F1F, 0xFFBF9F1F, 0xFFBFA727,
            0xFFBFA727, 0xFFBFAF2F, 0xFFB7AF2F, 0xFFB7B72F, 0xFFB7B737, 0xFFCFCF6F, 0xFFDFDF9F,
            0xFFEFEFC7, 0xFFFFFFFF
    };

    // FRAME_DELAY is the delay between each animation frame (27 fps)
    private static final long FRAME_DELAY = 1000L / 27L;

    // mAnimRunnable animates a frame and schedules itself to animate the next one
    private final Runnable mAnimRunnable = new Runnable() {
        @Override
        public void run() {
            update();
            if (mPlaying) {
                mHandler.postDelayed(this, FRAME_DELAY);
            }
        }
    };
    // mKillRunnable slowly kills the fire animation
    private final Runnable mKillRunnable = this::kill;

    // mHandler is the handler used for animation
    private final Handler mHandler;
    // mFirePaint is used to draw rhe fire bitmap on the canvas
    private final Paint mFirePaint;
    // mProgressRandom is used to get the random progression of each pixel
    private final Random mProgressRandom;
    // mFireWidth is the width of the fire bitmap
    private final int mFireWidth;
    // mFireWidth is the height of the fire bitmap
    private final int mFireHeight;

    // mFirePixelBuffer store the current index of the color of a pixel, used for computations
    private int[] mFirePixelBuffer;
    // mFirePixelColors store the current color of a pixel, used for drawing
    private int[] mFirePixelColors;

    // mFireBitmap is the bitmap containing the pixels of the fire
    private Bitmap mFireBitmap;
    // mHeight is the height of the view
    private int mHeight;
    // mPixelScale is used for scaling the canvas
    private float mPixelScale;
    // mWindOffset is the value used to create the wind effect
    private int mWindOffset;
    // mPlaying stores the current state of the animation
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

        // these values can be customized and could even be set using attributes
        mFireWidth = 280;
        mFireHeight = 168;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mHeight = h;

        // The scale is based on the actual width of the view and the width we manually set
        mPixelScale = (float) w / (float) mFireWidth;

        // Recycling the bitmap helps prevent OutOfMemory exceptions
        if (mFireBitmap != null) {
            mFireBitmap.recycle();
        }

        // The bitmap is created from the manually set width and height
        mFireBitmap = Bitmap.createBitmap(mFireWidth, mFireHeight, Bitmap.Config.ARGB_8888);

        initFire(mFireWidth, mFireHeight);
        updateBitmap();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.save();
        // Scaling helps getting a pixelated effect and saves us from doing it manually
        canvas.scale(mPixelScale, mPixelScale, 0, mHeight);
        canvas.drawBitmap(mFireBitmap, 0, mHeight - mFireHeight, mFirePaint);
        canvas.restore();
    }

    // initFire initializes buffers
    private void initFire(int w, int h) {
        mFirePixelBuffer = new int[w * h];
        mFirePixelColors = new int[w * h];

        for (int i = 0; i < mFirePixelBuffer.length - mFireWidth; i++) {
            setPixelColor(i, 0);
        }

        for (int x = 0; x < mFireWidth; x++) {
            setPixelColor((mFireHeight - 1) * mFireWidth + x, COLOR_BUFFER.length - 1);
        }
    }

    // fire spreads the fire for each pixel except for the first row
    private void fire() {
        for (int x = 0; x < mFireWidth; x++) {
            for (int y = 1; y < mFireHeight; y++) {
                spreadFire(y * mFireWidth + x);
            }
        }
    }

    // spreadFire computes the color of the pixel above the src pixel
    private void spreadFire(int src) {
        int pixel = mFirePixelBuffer[src];

        int randIndex = mProgressRandom.nextInt(4);
        // The dst pixel is the combination of a random index offset & the wind offset
        int dst = src - randIndex + 1 + mWindOffset;

        if (pixel == 0) {
            // The src pixel is dead so we kill the dst pixel above
            setPixelColor(Math.max(dst - mFireWidth, 0), 0);
        } else {
            // The dst pixel takes the value of a random pixel near src
            setPixelColor(dst - mFireWidth, pixel - (randIndex & 1));
        }
    }

    // updateBitmap sets the pixels of the bitmap using the color buffer
    private void updateBitmap() {
        if (mFireBitmap == null) {
            return;
        }

        mFireBitmap.setPixels(mFirePixelColors, 0, mFireWidth, 0, 0, mFireWidth, mFireHeight);
    }

    // setPixelColor sets the values for the pixel buffer & the color buffer
    private void setPixelColor(int index, int colorIndex) {
        mFirePixelBuffer[index] = colorIndex;
        mFirePixelColors[index] = COLOR_BUFFER[colorIndex];
    }

    // update computes the next frame & invalidates the view
    public void update() {
        fire();
        updateBitmap();
        invalidate();
    }

    // wind sets the value of the wind used for direction
    public void wind(int direction) {
        mWindOffset = mWindOffset != 0 ? 0 : direction;
    }

    // play starts the fire animation cycle
    public void play() {
        if (!mPlaying) {
            mPlaying = true;
            mAnimRunnable.run();
        }
    }

    // kill slowly kills the fire by progressively & randomly killing all the pixel sources
    public void kill() {
        if (!mPlaying) {
            reset();
            return;
        }

        int alive = 0;
        for (int x = 0; x < mFireWidth; x++) {
            int index = (mFireHeight - 1) * mFireWidth + x;

            if (mFirePixelBuffer[index] > 0) {
                if (mProgressRandom.nextFloat() > 0.2f) {
                    setPixelColor(index, mFirePixelBuffer[index] - mProgressRandom.nextInt(4) & 3);
                }
                alive++;
            }
        }

        if (alive > 0) {
            // The process is repeated every 15 frames if there still might be living source pixels
            mHandler.postDelayed(mKillRunnable, 15 * FRAME_DELAY);
        }
    }

    // reset stops & initializes the animation
    public void reset() {
        mHandler.removeCallbacks(mAnimRunnable);
        mHandler.removeCallbacks(mKillRunnable);
        mPlaying = false;

        mWindOffset = 0;
        initFire(mFireWidth, mFireHeight);
        updateBitmap();
        invalidate();
    }
}
