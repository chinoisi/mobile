package yesuaini.chinoisinteractif.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.Scroller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import yesuaini.chinoisinteractif.R;
import yesuaini.chinoisinteractif.models.Episode;

public class EpisodesMap extends ImageView {

    public ImageMapLoader imageLoader;
    private static final String TAG = "EpisodeMap";
    private List<Episode> episodes;

    private boolean mScaleFromOriginal = true;
    private float mMaxSize = 1.5f;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private Scroller mScroller;
    private boolean mIsBeingDragged = false;
    protected Integer nbLevels = 25;

    HashMap<Integer, TouchPoint> mTouchPoints = new HashMap<Integer, TouchPoint>();
    TouchPoint mMainTouch = null;
    TouchPoint mPinchTouch = null;

    Paint textPaint;
    Paint textOutlinePaint;
    Paint bubblePaint;
    Paint bubbleShadowPaint;
    Paint idPaint = new Paint();

    Bitmap mImage;
    Bitmap mOriginal;

    int mImageHeight;
    int mImageWidth;
    float mAspect;
    int mExpandWidth;
    int mExpandHeight;
    int mRightBound;
    int mBottomBound;
    protected float mResizeFactorX;
    protected float mResizeFactorY;
    int mMinWidth = -1;
    int mMinHeight = -1;
    int mMaxWidth = -1;
    int mMaxHeight = -1;

    int mScrollTop;
    int mScrollLeft;

    int mViewHeight = -1;
    int mViewWidth = -1;

    ArrayList<Area> mAreaList = new ArrayList<Area>();
    SparseArray<Area> mIdToArea = new SparseArray<Area>();
    ArrayList<OnImageMapClickedHandler> mCallbackList;
    SparseArray<Bubble> mBubbleMap = new SparseArray<Bubble>();

    protected float densityFactor;
    protected BitmapFactory.Options options;
    protected int level = 1;

    public EpisodesMap(Context context) {
        super(context);
        initView();
    }

    public EpisodesMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        imageLoader = new ImageMapLoader(context);
        initView();
        loadData();
        centerToLevelArea();
    }

    public EpisodesMap(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        imageLoader = new ImageMapLoader(context);
        initView();
        loadData();
        centerToLevelArea();
    }

    protected void loadData() {
        try {
            InputStream inputStream = getResources().getAssets().open("episodes.json");
            InputStreamReader reader = new InputStreamReader(inputStream);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a");
            Gson gson = gsonBuilder.create();
            episodes = Arrays.asList(gson.fromJson(reader, Episode[].class));
            nbLevels = episodes.size();
            inputStream.close();
        } catch (Exception ex) {
            Log.e(TAG, "Failed to parse JSON due to: " + ex);
        }

        Integer initialX = 10;
        Integer initialY = 10 * nbLevels;
        Integer width = 70;
        Integer height = 50;
        Integer x = initialX;
        Integer y = initialY;

        for (int i = 1; i <= nbLevels; i++) {
            if (i % 15 == 0) {
                x = initialX + 300;
                y = y - 25;
            } else if (i % 10 == 0 || i % 11 == 0 || i % 13 == 0) {
                x = x + 125;
                y = y - 45;
            } else if (i % 4 == 0) {
                x = x + 55 - i * 4;
                y = y + 45;
            } else if (i % 6 == 0 || i % 7 == 0 || i % 5 == 0) {
                x = x - 100;
                y = y + 25;
            } else if (i % 3 == 0) {
                x = x + 125;
                y = y + 35;
            } else {
                x = x + (100 + i * 5);
                y = y + i * 7;
            }

            Area a = new RectArea(i, i + "", x, y, x + width, y + height);
            a.addValue("id", i + "");
            a.addValue("name", "Mission " + i);
            addArea(a);
        }
    //    setMapResource();
    }

    public void setMapResource() {
         setImageResource(R.drawable.map);
    }

    public void addArea(Area a) {
        mAreaList.add(a);
        mIdToArea.put(a.getId(), a);
    }

    public void addBubble(String text, int areaId) {
        if (mBubbleMap.get(areaId) == null) {
            Bubble b = new Bubble(text, areaId);
            mBubbleMap.put(areaId, b);
        }
    }

    public void showBubble(String text, int areaId) {
        mBubbleMap.clear();
        addBubble(text, areaId);
        invalidate();
    }

    public void showBubble(int areaId) {
        mBubbleMap.clear();
        Area a = mIdToArea.get(areaId);
        if (a != null) {
            addBubble(a.getName(), areaId);
        }
        invalidate();
    }

    public void centerArea(int areaId) {
        Area a = mIdToArea.get(areaId);
        if (a != null) {
            float x = a.getOriginX() * mResizeFactorX;
            float y = a.getOriginY() * mResizeFactorY;
            int left = (int) ((mViewWidth / 2) - x);
            int top = (int) ((mViewHeight / 2) - y);
            moveTo(left, top);
        }
    }

    public void centerToLevelArea() {
        centerArea(mAreaList.get(level-1).getId());
    }

    public void centerAndShowArea(String text, int areaId) {
        centerArea(areaId);
        showBubble(text, areaId);
    }

    public void centerAndShowArea(int areaId) {
        Area a = mIdToArea.get(areaId);
        if (a != null) {
            centerAndShowArea(a.getName(), areaId);
        }
    }

    private void initView() {
        initDrawingTools();
        mScroller = new Scroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        densityFactor = getResources().getDisplayMetrics().density;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (mImage == mOriginal) {
            mOriginal = null;
        } else {
            mOriginal.recycle();
            mOriginal = null;
        }
        if (mImage != null) {
            mImage.recycle();
            mImage = null;
        }

        mImage = bm;
        mOriginal = bm;
        mImageHeight = mImage.getHeight();
        mImageWidth = mImage.getWidth();
        mAspect = (float) mImageWidth / mImageHeight;
        setInitialImageBounds();
    }

    @Override
    public void setImageResource(int resId) {
        final String imageKey = String.valueOf(resId);
        BitmapHelper bitmapHelper = BitmapHelper.getInstance();
        Bitmap bitmap = bitmapHelper.getBitmapFromMemCache(imageKey);
        options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), resId, options);
            bitmapHelper.addBitmapToMemoryCache(imageKey, bitmap);
        }
        setImageBitmap(Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()*nbLevels/15,bitmap.getHeight()*nbLevels/15, false));
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            setImageBitmap(bd.getBitmap());
        }
    }

    private void initDrawingTools() {
        textPaint = new Paint();
        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(30);
        textPaint.setTypeface(Typeface.SERIF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        textOutlinePaint = new Paint();
        textOutlinePaint.setColor(0xFF000000);
        textOutlinePaint.setTextSize(18);
        textOutlinePaint.setTypeface(Typeface.SERIF);
        textOutlinePaint.setTextAlign(Paint.Align.CENTER);
        textOutlinePaint.setStyle(Paint.Style.STROKE);
        textOutlinePaint.setStrokeWidth(2);

        bubblePaint = new Paint();
        bubblePaint.setColor(0xFFFFFFFF);
        bubbleShadowPaint = new Paint();
        bubbleShadowPaint.setColor(0xFF000000);

        idPaint.setTextSize(30);
        idPaint.setTypeface(Typeface.SERIF);
        idPaint.setTextAlign(Paint.Align.CENTER);
        idPaint.setAntiAlias(true);
        idPaint.setColor(Color.WHITE);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int oldX = mScrollLeft;
            int oldY = mScrollTop;
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x) {
                moveX(x - oldX);
            }
            if (oldY != y) {
                moveY(y - oldY);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int chosenWidth = chooseDimension(widthMode, widthSize);
        int chosenHeight = chooseDimension(heightMode, heightSize);
        setMeasuredDimension(chosenWidth, chosenHeight);
    }

    private int chooseDimension(int mode, int size) {
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            return size;
        } else {
            return getPreferredSize();
        }
    }

    void setInitialImageBounds() {
            setInitialImageBoundsFillScreen();
     }

    void setInitialImageBoundsFillScreen() {
        if (mImage != null) {
            if (mViewWidth > 0) {
                boolean resize = false;

                int newWidth = mImageWidth;
                int newHeight = mImageHeight;

                if (mMinWidth == -1) {
                    if (mViewWidth > mViewHeight) {
                        mMinWidth = mViewWidth;
                        mMinHeight = (int) (mMinWidth / mAspect);
                    } else {
                        mMinHeight = mViewHeight;
                        mMinWidth = (int) (mAspect * mViewHeight);
                    }
                    mMaxWidth = (int) (mMinWidth * 1.5f);
                    mMaxHeight = (int) (mMinHeight * 1.5f);
                }

                if (newWidth < mMinWidth) {
                    newWidth = mMinWidth;
                    newHeight = (int) (((float) mMinWidth / mImageWidth) * mImageHeight);
                    resize = true;
                }
                if (newHeight < mMinHeight) {
                    newHeight = mMinHeight;
                    newWidth = (int) (((float) mMinHeight / mImageHeight) * mImageWidth);
                    resize = true;
                }

                mScrollTop = 0;
                mScrollLeft = 0;

                if (resize) {
                    scaleBitmap(newWidth, newHeight);
                } else {
                    mExpandWidth = newWidth;
                    mExpandHeight = newHeight;

                    mResizeFactorX = ((float) newWidth / mImageWidth);
                    mResizeFactorY = ((float) newHeight / mImageHeight);
                    mRightBound = 0 - (mExpandWidth - mViewWidth);
                    mBottomBound = 0 - (mExpandHeight - mViewHeight);
                }
            }
        }
    }

    public void scaleBitmap(int newWidth, int newHeight) {
        if ((newWidth > mMaxWidth) || (newHeight > mMaxHeight)) {
            newWidth = mMaxWidth;
            newHeight = mMaxHeight;
        }
        if ((newWidth < mMinWidth) || (newHeight < mMinHeight)) {
            newWidth = mMinWidth;
            newHeight = mMinHeight;
        }

        if ((newWidth != mExpandWidth) || (newHeight != mExpandHeight)) {
            Bitmap newbits = Bitmap.createScaledBitmap(mScaleFromOriginal ? mOriginal : mImage, newWidth,
                    newHeight, true);
            if (newbits != null) {
                if (mImage != mOriginal) {
                    mImage.recycle();
                }
                mImage = newbits;
                mExpandWidth = newWidth;
                mExpandHeight = newHeight;
                mResizeFactorX = ((float) newWidth / mImageWidth);
                mResizeFactorY = ((float) newHeight / mImageHeight);

                mRightBound = mExpandWidth > mViewWidth ? 0 - (mExpandWidth - mViewWidth) : 0;
                mBottomBound = mExpandHeight > mViewHeight ? 0 - (mExpandHeight - mViewHeight) : 0;
            }
        }
    }

//    void resizeBitmap(int amount) {
//        int adjustWidth = amount;
//        int adjustHeight = (int) (adjustWidth / mAspect);
//        scaleBitmap(mExpandWidth + adjustWidth, mExpandHeight + adjustHeight);
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewHeight = h;
        mViewWidth = w;
        setInitialImageBounds();
    }

    protected int getPreferredSize() {
        return 300;
    }

    protected void drawMap(Canvas canvas) {
        canvas.save();
        if (mImage != null) {
            if (!mImage.isRecycled()) {
                canvas.drawBitmap(mImage, mScrollLeft, mScrollTop, null);
                drawItinery(canvas,1,Color.GREEN);
                drawItinery(canvas,level,Color.LTGRAY);
                drawLevels(canvas);
            }
        }
        canvas.restore();
}

    private void drawLevels(Canvas canvas) {
        for (int i = 0; i < mAreaList.size(); i++) {
            Area area1 = mAreaList.get(i);
            imageLoader.displayLevelImage(canvas, mScrollLeft +  area1.getOriginX(), mScrollTop + area1.getOriginY(),  mResizeFactorX,  mResizeFactorY, getLevelImage(i), i+1+"");
        }
    }


    protected String getLevelImage(int dataId) {
        return episodes.get(dataId).getImage();
    }

    /**
     * @param bitmap The source bitmap.
     * @param opacity a value between 0 (completely transparent) and 255 (completely
     * opaque).
     * @return The opacity-adjusted bitmap.  If the source bitmap is mutable it will be
     * adjusted and returned, otherwise a new bitmap is created.
     */
    private Bitmap adjustOpacity(Bitmap bitmap, int opacity)
    {
        Bitmap mutableBitmap = bitmap.isMutable()
                ? bitmap
                : bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        int colour = (opacity & 0xFF) << 24;
        canvas.drawColor(colour, PorterDuff.Mode.DST_IN);
        return mutableBitmap;
    }

    private void drawItinery(Canvas canvas, int beginLevel, int color) {
        for (int i = beginLevel-1; i < mAreaList.size(); i++) {
            Area area1 = mAreaList.get(i);
            Random rand = new Random();
            if (i < mAreaList.size() - 1) {
                Area area2 = mAreaList.get(i + 1);
                int x1 = Math.round((mScrollLeft + area1.getOriginX() + 50) * mResizeFactorX);
                int y1 = Math.round((mScrollTop + area1.getOriginY() + 40) * mResizeFactorY);
                int x2 = Math.round((mScrollLeft + area2.getOriginX() + 45) * mResizeFactorX);
                int y2 = Math.round((mScrollTop + area2.getOriginY() + 40) * mResizeFactorY);
                int anchor_x1 = Math.round(x1 - 20);
                int anchor_y1 = Math.round(y1 - 10);
                int anchor_x2 = Math.round(x2 - 10);
                int anchor_y2 = Math.round(y2 - 20);
                drawLines(canvas, x1, y1, x2, y2, anchor_x1, anchor_y1, anchor_x2, anchor_y2, 12, color);
            }
        }
    }

    protected void drawBubbles(Canvas canvas) {
        for (int i = 0; i < mBubbleMap.size(); i++) {
            int key = mBubbleMap.keyAt(i);
            Bubble b = mBubbleMap.get(key);
            if (b != null) {
                b.onDraw(canvas);
            }
        }
    }

    protected void drawLocations(Canvas canvas) {
        for (Area a : mAreaList) {
            a.onDraw(canvas);
        }
    }


    protected void drawLines(Canvas canvas, int x1, int y1, int x2, int y2, int anchor1_x, int anchor1_y, int anchor2_x, int anchor2_y, int width, int color) {
        Path mPath;
        Paint paint;
        mPath = new Path();
        mPath.moveTo(x1, y1);
        mPath.cubicTo(anchor1_x, anchor1_y, anchor2_x, anchor2_y, x2, y2);
        paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        canvas.drawPath(mPath, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawMap(canvas);
        drawLocations(canvas);
        drawBubbles(canvas);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int id;

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();

        int pointerCount = ev.getPointerCount();
        int index = 0;

        if (pointerCount > 1) {
            index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK);
            index = index >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                for (TouchPoint t : mTouchPoints.values()) {
                    onLostTouch(t.getTrackingPointer());
                }
            case MotionEvent.ACTION_POINTER_DOWN:
                id = ev.getPointerId(index);
                onTouchDown(id, ev.getX(index), ev.getY(index));
                break;

            case MotionEvent.ACTION_MOVE:
                for (int p = 0; p < pointerCount; p++) {
                    id = ev.getPointerId(p);
                    TouchPoint t = mTouchPoints.get(id);
                    if (t != null) {
                        onTouchMove(t, ev.getX(p), ev.getY(p));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                id = ev.getPointerId(index);
                onTouchUp(id);
                break;
            case MotionEvent.ACTION_CANCEL:
                for (TouchPoint t : mTouchPoints.values()) {
                    onLostTouch(t.getTrackingPointer());
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }


    void onTouchDown(int id, float x, float y) {
        TouchPoint t = null;
        synchronized (mTouchPoints) {
            t = mTouchPoints.get(id);
            if (t == null) {
                t = new TouchPoint(id);
                mTouchPoints.put(id, t);
            }

            if (mMainTouch == null) {
                mMainTouch = t;
            }         }
        t.setPosition(x, y);
    }

    void onTouchMove(TouchPoint t, float x, float y) {
        if (t == mMainTouch) {
            if (mPinchTouch == null) {
                final int deltaX = (int) (t.getX() - x);
                final int xDiff = (int) Math.abs(t.getX() - x);
                final int deltaY = (int) (t.getY() - y);
                final int yDiff = (int) Math.abs(t.getY() - y);

                if (!mIsBeingDragged) {
                    if ((xDiff > mTouchSlop) || (yDiff > mTouchSlop)) {
                        mIsBeingDragged = true;
                    }
                } else {
                    if (xDiff > 0) {
                        moveX(-deltaX);
                    }
                    if (yDiff > 0) {
                        moveY(-deltaY);
                    }
                    t.setPosition(x, y);
                }
            }
        }
    }


    void onTouchUp(int id) {
        synchronized (mTouchPoints) {
            TouchPoint t = mTouchPoints.get(id);
            if (t != null) {
                if (t == mMainTouch) {
                    if (mPinchTouch == null) {
                        if (mIsBeingDragged) {
                            final VelocityTracker velocityTracker = mVelocityTracker;
                            velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                            int xVelocity = (int) velocityTracker.getXVelocity();
                            int yVelocity = (int) velocityTracker.getYVelocity();
                            int xfling = Math.abs(xVelocity) > mMinimumVelocity ? xVelocity : 0;
                            int yfling = Math.abs(yVelocity) > mMinimumVelocity ? yVelocity : 0;
                            if ((xfling != 0) || (yfling != 0)) {
                                fling(-xfling, -yfling);
                            }
                            mIsBeingDragged = false;
                            if (mVelocityTracker != null) {
                                mVelocityTracker.recycle();
                                mVelocityTracker = null;
                            }
                        } else {
                            onScreenTapped((int) mMainTouch.getX(), (int) mMainTouch.getY());
                        }
                    }
                    mMainTouch = null;
                }
                if (t == mPinchTouch) {
                    mPinchTouch = null;
                }
                mTouchPoints.remove(id);
                regroupTouches();
            }
        }
    }

    void onLostTouch(int id) {
        synchronized (mTouchPoints) {
            TouchPoint t = mTouchPoints.get(id);
            if (t != null) {
                if (t == mMainTouch) {
                    mMainTouch = null;
                }
                if (t == mPinchTouch) {
                    mPinchTouch = null;
                }
                mTouchPoints.remove(id);
                regroupTouches();
            }
        }
    }

    TouchPoint getUnboundPoint() {
        TouchPoint ret = null;
        for (Integer i : mTouchPoints.keySet()) {
            TouchPoint p = mTouchPoints.get(i);
            if ((p != mMainTouch) && (p != mPinchTouch)) {
                ret = p;
                break;
            }
        }
        return ret;
    }

    void regroupTouches() {
        int s = mTouchPoints.size();
        if (s > 0) {
            if (mMainTouch == null) {
                if (mPinchTouch != null) {
                    mMainTouch = mPinchTouch;
                    mPinchTouch = null;
                } else {
                    mMainTouch = getUnboundPoint();
                }
            }
        }
    }

    void onScreenTapped(int x, int y) {
        boolean missed = true;
        boolean bubble = false;
        int testx = x - mScrollLeft;
        int testy = y - mScrollTop;

        if (mResizeFactorX > 1) {
            testx = (int) (((float) testx / mResizeFactorX));
        } else {
            testx = (int) (((float) testx / mResizeFactorX) / densityFactor);
        }
        if (mResizeFactorY > 1) {
            testy = (int) (((float) testy / mResizeFactorY));
        } else {
            testy = (int) (((float) testy / mResizeFactorY) / densityFactor);
        }

        for (int i = 0; i < mBubbleMap.size(); i++) {
            int key = mBubbleMap.keyAt(i);
            Bubble b = mBubbleMap.get(key);
            if (b != null) {
                if (b.isInArea((float) x - mScrollLeft, (float) y - mScrollTop)) {
                    b.onTapped();
                    bubble = true;
                    missed = false;
                    break;
                }
            }
        }

        if (!bubble) {
            for (Area a : mAreaList) {
                if (a.isInArea((float) testx, (float) testy)) {
                    if (mCallbackList != null) {
                        for (OnImageMapClickedHandler h : mCallbackList) {
                            h.onImageMapClicked(a.getId(), this);
                        }
                    }
                    missed = false;
                    break;
                }
            }
        }

        if (missed) {
            mBubbleMap.clear();
            invalidate();
        }
    }

    public void fling(int velocityX, int velocityY) {
        int startX = mScrollLeft;
        int startY = mScrollTop;
        mScroller.fling(startX, startY, -velocityX, -velocityY, mRightBound, 0,
                mBottomBound, 0);
        invalidate();
    }

    public void moveTo(int x, int y) {
        mScrollLeft = x;
        if (mScrollLeft > 0) {
            mScrollLeft = 0;
        }
        if (mScrollLeft < mRightBound) {
            mScrollLeft = mRightBound;
        }
        mScrollTop = y;
        if (mScrollTop > 0) {
            mScrollTop = 0;
        }
        if (mScrollTop < mBottomBound) {
            mScrollTop = mBottomBound;
        }
        invalidate();
    }

    public void moveX(int deltaX) {
        mScrollLeft = mScrollLeft + deltaX;
        if (mScrollLeft > 0) {
            mScrollLeft = 0;
        }
        if (mScrollLeft < mRightBound) {
            mScrollLeft = mRightBound;
        }
        invalidate();
    }

    public void moveY(int deltaY) {
        mScrollTop = mScrollTop + deltaY;
        if (mScrollTop > 0) {
            mScrollTop = 0;
        }
        if (mScrollTop < mBottomBound) {
            mScrollTop = mBottomBound;
        }
        invalidate();
    }

    public void setCurrentLevel(int currentLevel) {
        level = getMaxIndex() > currentLevel ? currentLevel : getMaxIndex();
    }

    public int getAreaIndex(int id) {
        int index = 0;
        for (Area area : mAreaList) {
            index++;
            if (area.getId() == id) {
                return index;
            }
        }
        return -1;
    }

    public int getMaxIndex() {
        return mAreaList.size();
    }


    class TouchPoint {
    int _id;
    float _x;
    float _y;

    TouchPoint(int id) {
        _id = id;
        _x = 0f;
        _y = 0f;
    }

    int getTrackingPointer() {
        return _id;
    }

    void setPosition(float x, float y) {
        if ((_x != x) || (_y != y)) {
            _x = x;
            _y = y;
        }
    }

    float getX() {
        return _x;
    }

    float getY() {
        return _y;
    }
}

    public void addOnImageMapClickedHandler(OnImageMapClickedHandler h) {
        if (h != null) {
            if (mCallbackList == null) {
                mCallbackList = new ArrayList<OnImageMapClickedHandler>();
            }
            mCallbackList.add(h);
        }
    }

    public void removeOnImageMapClickedHandler(OnImageMapClickedHandler h) {
        if (mCallbackList != null) {
            if (h != null) {
                mCallbackList.remove(h);
            }
        }
    }

abstract class Area {
    int _id;
    String _name;
    HashMap<String, String> _values;
    Bitmap _decoration = null;

    public Area(int id, String name) {
        _id = id;
        if (name != null) {
            _name = name;
        }
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public void addValue(String key, String value) {
        if (_values == null) {
            _values = new HashMap<String, String>();
        }
        _values.put(key, value);
    }

    public String getValue(String key) {
        String value = null;
        if (_values != null) {
            value = _values.get(key);
        }
        return value;
    }


    public void setBitmap(Bitmap b) {
        _decoration = b;
    }

    public void onDraw(Canvas canvas) {
        if (_decoration != null) {
            float x = (getOriginX() * mResizeFactorX) + mScrollLeft - 17;
            float y = (getOriginY() * mResizeFactorY) + mScrollTop - 17;
            canvas.drawBitmap(_decoration, x, y, null);
        }
    }

    abstract boolean isInArea(float x, float y);

    abstract float getOriginX();

    abstract float getOriginY();
}

class RectArea extends Area {
    float _left;
    float _top;
    float _right;
    float _bottom;

    RectArea(int id, String name, float left, float top, float right, float bottom) {
        super(id, name);
        _left = left;
        _top = top;
        _right = right;
        _bottom = bottom;
    }

    public boolean isInArea(float x, float y) {
        boolean ret = false;
        if ((x > _left) && (x < _right)) {
            if ((y > _top) && (y < _bottom)) {
                ret = true;
            }
        }
        return ret;
    }

    public float getOriginX() {
        return _left;
    }

    public float getOriginY() {
        return _top;
    }
}

class PolyArea extends Area {
    ArrayList<Integer> xpoints = new ArrayList<Integer>();
    ArrayList<Integer> ypoints = new ArrayList<Integer>();

    float _x;
    float _y;
    int _points;
    int top = -1;
    int bottom = -1;
    int left = -1;
    int right = -1;

    public PolyArea(int id, String name, String coords) {
        super(id, name);

        String[] v = coords.split(",");

        int i = 0;
        while ((i + 1) < v.length) {
            int x = Integer.parseInt(v[i]);
            int y = Integer.parseInt(v[i + 1]);
            xpoints.add(x);
            ypoints.add(y);
            top = (top == -1) ? y : Math.min(top, y);
            bottom = (bottom == -1) ? y : Math.max(bottom, y);
            left = (left == -1) ? x : Math.min(left, x);
            right = (right == -1) ? x : Math.max(right, x);
            i += 2;
        }
        _points = xpoints.size();

        xpoints.add(xpoints.get(0));
        ypoints.add(ypoints.get(0));

        computeCentroid();
    }

    public double area() {
        double sum = 0.0;
        for (int i = 0; i < _points; i++) {
            sum = sum + (xpoints.get(i) * ypoints.get(i + 1)) - (ypoints.get(i) * xpoints.get(i + 1));
        }
        sum = 0.5 * sum;
        return Math.abs(sum);
    }

    public void computeCentroid() {
        double cx = 0.0, cy = 0.0;
        for (int i = 0; i < _points; i++) {
            cx = cx + (xpoints.get(i) + xpoints.get(i + 1)) * (ypoints.get(i) * xpoints.get(i + 1) - xpoints.get(i) * ypoints.get(i + 1));
            cy = cy + (ypoints.get(i) + ypoints.get(i + 1)) * (ypoints.get(i) * xpoints.get(i + 1) - xpoints.get(i) * ypoints.get(i + 1));
        }
        cx /= (6 * area());
        cy /= (6 * area());
        _x = Math.abs((int) cx);
        _y = Math.abs((int) cy);
    }


    @Override
    public float getOriginX() {
        return _x;
    }

    @Override
    public float getOriginY() {
        return _y;
    }

    @Override
    public boolean isInArea(float testx, float testy) {
        int i, j;
        boolean c = false;
        for (i = 0, j = _points - 1; i < _points; j = i++) {
            if (((ypoints.get(i) > testy) != (ypoints.get(j) > testy)) &&
                    (testx < (xpoints.get(j) - xpoints.get(i)) * (testy - ypoints.get(i)) / (ypoints.get(j) - ypoints.get(i)) + xpoints.get(i)))
                c = !c;
        }
        return c;
    }

}

class CircleArea extends Area {
    float _x;
    float _y;
    float _radius;

    CircleArea(int id, String name, float x, float y, float radius) {
        super(id, name);
        _x = x;
        _y = y;
        _radius = radius;

    }

    public boolean isInArea(float x, float y) {
        boolean ret = false;
        float dx = _x - x;
        float dy = _y - y;
        float d = (float) Math.sqrt((dx * dx) + (dy * dy));
        if (d < _radius) {
            ret = true;
        }
        return ret;
    }

    public float getOriginX() {
        return _x;
    }

    public float getOriginY() {
        return _y;
    }
}

class Bubble {
    Area _a;
    String _text;
    float _x;
    float _y;
    int _h;
    int _w;
    int _baseline;
    float _top;
    float _left;

    Bubble(String text, float x, float y) {
        init(text, x, y);
    }

    Bubble(String text, int areaId) {
        _a = mIdToArea.get(areaId);
        if (_a != null) {
            float x = _a.getOriginX();
            float y = _a.getOriginY();
            init(text, x, y);
        }
    }

    void init(String text, float x, float y) {
        _text = text;
        _x = x * mResizeFactorX;
        _y = y * mResizeFactorY;
        Rect bounds = new Rect();
        textPaint.setTextScaleX(1.0f);
        textPaint.getTextBounds(text, 0, _text.length(), bounds);
        _h = bounds.bottom - bounds.top + 20;
        _w = bounds.right - bounds.left + 20;

        if (_w > mViewWidth) {
            float newscale = ((float) mViewWidth / (float) _w);
            textPaint.setTextScaleX(newscale);
            textPaint.getTextBounds(text, 0, _text.length(), bounds);
            _h = bounds.bottom - bounds.top + 20;
            _w = bounds.right - bounds.left + 20;
        }

        _baseline = _h - bounds.bottom;
        _left = _x - (_w / 2);
        _top = _y - _h - 30;

        if (_left < 0) {
            _left = 0;
        }
        if ((_left + _w) > mExpandWidth) {
            _left = mExpandWidth - _w;
        }
        if (_top < 0) {
            _top = _y + 20;
        }
    }

    public boolean isInArea(float x, float y) {
        boolean ret = false;
        if ((x > _left) && (x < (_left + _w))) {
            if ((y > _top) && (y < (_top + _h))) {
                ret = true;
            }
        }
        return ret;
    }

    void onDraw(Canvas canvas) {
        if (_a != null) {
            float l = _left + mScrollLeft + 4;
            float t = _top + mScrollTop + 4;
            canvas.drawRoundRect(new RectF(l, t, l + _w, t + _h), 20.0f, 20.0f, bubbleShadowPaint);
            Path path = new Path();
            float ox = _x + mScrollLeft + 1;
            float oy = _y + mScrollTop + 1;
            int yoffset = -35;
            if (_top > _y) {
                yoffset = 35;
            }
            path.moveTo(ox, oy);
            path.lineTo(ox - 5, oy + yoffset);
            path.lineTo(ox + 5 + 4, oy + yoffset);
            path.lineTo(ox, oy);
            path.close();
            canvas.drawPath(path, bubbleShadowPaint);

            l = _left + mScrollLeft;
            t = _top + mScrollTop;
            canvas.drawRoundRect(new RectF(l, t, l + _w, t + _h), 20.0f, 20.0f, bubblePaint);
            path = new Path();
            ox = _x + mScrollLeft;
            oy = _y + mScrollTop;
            yoffset = -35;
            if (_top > _y) {
                yoffset = 35;
            }

            path.moveTo(ox, oy);
            path.lineTo(ox - 5, oy + yoffset);
            path.lineTo(ox + 5, oy + yoffset);
            path.lineTo(ox, oy);
            path.close();
            canvas.drawPath(path, bubblePaint);

            canvas.drawText(_text, l + (_w / 2), t + _baseline - 10, textPaint);
        }
    }

    void onTapped() {
        if (mCallbackList != null) {
            for (OnImageMapClickedHandler h : mCallbackList) {
                h.onBubbleClicked(_a.getId());
            }
        }
    }
}

public interface OnImageMapClickedHandler {
    void onImageMapClicked(int id, EpisodesMap imageMap);
    void onBubbleClicked(int id);
}

}