package edu.berkeley.hci.formation;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by daphnenhuch on 4/6/18.
 */

public class ClickView extends View {
    Paint mPaint;
    Path mPath;
    Canvas mCanvas;
    Bitmap mBitmap;
    Float x;
    Float y;
    Long start;
    Long end;
    Dot lastDot;
    Boolean sizeChange;
    Boolean oldPositions;
    Boolean moving;
    HashMap<Pair<Float, Float>, String> toTag;
    Boolean remove;
    ArrayList<Pair<Pair<Float, Float>, Integer>> positions;

    public ClickView(Context context) {
        super(context);
        init(null, 0);
        toTag = new HashMap<>();
    }

    public ClickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
        this.x = new Float(1);
        this.y = new Float(1);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        toTag = new HashMap<>();
        mPath = new Path();
        mPaint = new Paint();
//        mPaint.setAntiAlias(true);
//        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setStrokeWidth(50);
        //List of positions of dots
        positions = new ArrayList<>();
        //Have all of the previously placed dots been set
        oldPositions = false;
        //Is the dot being dragged
        moving = false;
        sizeChange = false;
        //Is a dot being removed
        this.remove = false;



    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        // Are you done setting all the previously placed dots?
        if (!oldPositions) {
            for (Pair<Pair<Float, Float>, Integer> info : positions) {
                //For each dot get its coords and color
                Pair<Float, Float> pos = info.first;
                mPaint.setColor(info.second);
                drawDot(pos.first, pos.second);
                mPaint.setColor(Color.BLACK);
            }
        }
        if (toTag.entrySet() != null) {
            for (HashMap.Entry<Pair<Float, Float>, String> entry : toTag.entrySet()) {
                Paint paint = new Paint();
                paint.setColor(Color.BLACK); // Text Color
                paint.setTextSize(30);
                mCanvas.drawText(entry.getValue(), entry.getKey().first - 50, entry.getKey().second + 60, paint);
            }
        }
        toTag = new HashMap<>();
        oldPositions = true;

        //If a dot is being removed
        if (this.remove) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
            mPaint.setColor(Color.BLACK);

        } else {
            //Otherwise default to setting a dot to be black
            mPaint.setColor(Color.BLACK);
            canvas.drawColor(Color.WHITE);

            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
    }

    //See if this block contains a dot at x, y given 30 pixels of error
    public boolean containsDot(Float x, Float y) {
        for (Pair<Pair<Float, Float>, Integer> pair : positions) {
            Pair<Float, Float> coords = pair.first;
            if (coords.first - 60 < x && coords.first + 60 > x && coords.second - 60 < y && coords.second + 60 > y) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.x = event.getX();
        this.y = event.getY();
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                start = event.getEventTime();
                //If no dot exists at x, y add that dot to the block
                if (!containsDot(x, y)) {
                    start = event.getEventTime();
                    mPath.moveTo(x, y);
                    mPath.lineTo(x, y);
                    Dot d = new Dot();
                    d.setCoordinates(x, y);
                    ((BlockActivity) getContext()).addDot(d);
                    lastDot = d;

                } else {

                    lastDot = ((BlockActivity) getContext()).getDot(x, y);
                }

                break;

            case MotionEvent.ACTION_UP:
                end = event.getEventTime();
                //Draw the dot and add it to positions
                if (end - start > 1000) {
                    if (containsDot(x, y)) {
                        ((BlockActivity) getContext()).showPopup(lastDot, x, y);
                    }
                }
                removeDot(lastDot.x, lastDot.y);
                removeText(lastDot.x, lastDot.y,lastDot.userid);
                lastDot.setCoordinates(x, y);
                tagDancer(x, y, lastDot.userid);
                mPath.moveTo(x, y);
                mPath.lineTo(x, y);

                mCanvas.drawPath(mPath, mPaint);
                if (!containsDot(x, y)) {
                    positions.add(new Pair(new Pair(x, y), Color.BLACK));
                }

                invalidate();
                mPath.reset();
                moving = false;
                break;
            //Doesn't currently work
            case MotionEvent.ACTION_MOVE:
//
                mPath.moveTo(x, y);
                lastDot.changed = true;

                invalidate();
                mPath.reset();
                //invalidate();


                break;
            default:
                break;
        }
        return true;

    }

    //Draws a dot at x, y
    public void drawDot(float x, float y) {

        mPath.moveTo(x, y);
        mPath.lineTo(x, y);
        mCanvas.drawPath(mPath, mPaint);

        invalidate();
        mPath.reset();

    }

    public void tagDancer(float x, float y, String name) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK); // Text Color
        paint.setTextSize(30);
        mCanvas.drawText(name, x - 50, y + 60, paint);
        toTag.put(new Pair(x, y), name);
        invalidate();
    }

    public void removeText(float x, float y, String name){
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setTextSize(30);
        paint.setStrokeWidth(50);
        mCanvas.drawLine(x - 50, y + 60, x+90, y+60,  paint);
        invalidate();


    }
    public void drawDotFromAfar(Dot d) {

        Path mPath = new Path();
        mPath.moveTo(d.x, d.y);
        mPath.lineTo(d.x, d.y);
        mCanvas.drawPath(mPath, mPaint);
    }

    //Adds x, y to positions (used when loading dot positions of an already existing block
    public void addXY(float x, float y, int color) {
        Pair<Float, Float> pos = new Pair<Float, Float>(x, y);
        this.positions.add(new Pair(pos, color));

    }

    //Finds the dot at x y and then erases it and removes it from blocking
    public void removeDot(float x, float y) {
        Pair<Pair<Float, Float>, Integer> toRemove = null;
        for (Pair<Pair<Float, Float>, Integer> info : positions) {
            Pair<Float, Float> pos = info.first;
            if (pos.first == x && pos.second == y) {
                toRemove = info;
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                this.remove = true;
                positions.remove(info);
                drawDot(pos.first, pos.second);
                break;
            }
        }
        mPaint.setXfermode(null);
        this.remove = false;
        positions.remove(toRemove);


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);
        this.sizeChange = true;

    }

    //Changes dot color to color
    public void changeDotColor(Dot dot, int color) {
        mPaint.setColor(color);
        mPath.moveTo(dot.x, dot.y);
        mPath.lineTo(dot.x, dot.y);
        ((BlockActivity) getContext()).changeDotColor(dot, color);
        mCanvas.drawPath(mPath, mPaint);
        invalidate();
        mPath.reset();
        mPaint.setColor(Color.BLACK);

    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public void setCanvasVars() {
        int w = 1794;
        int h = 866;

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

}
