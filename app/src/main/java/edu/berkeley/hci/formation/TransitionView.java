package edu.berkeley.hci.formation;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * @author Daphne Nhuch
 */
//Experimental class for transitions
public class TransitionView extends View {

    private static final String QUOTE = "Now is the time for all good men to come to the aid of their country.";
    ArrayList<Button> buttons;
    ArrayList<Path> paths;
    Integer pathIndex;
    Button btn;
    Button btn2;
    Long duration;
    Boolean clearing;
    private Animation animation;
    private Paint mPaint;
    private Paint cPaint;
    private Path circle;
    private Path circle2;
    private Path path;
    private Bitmap bitmap;
    private Bitmap originalBitmap;
    private Canvas mCanvas;
    private float x;
    private float y;
    private boolean translated;

    public TransitionView(Context context, ArrayList<Button> buttons, ArrayList<Path> paths) {
        super(context);

        this.buttons = buttons;
        this.paths = paths;
        //drawn = false;

        System.out.println("hello kitty");
        init();
    }

    private void init() {
        mPaint = new Paint();
//        mPaint.setAntiAlias(true);
//        mPaint.setDither(true);
        mPaint.setColor(getResources().getColor(R.color.pathPurple));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setStrokeWidth(50);
        translated = false;
       // drawn = false;
        pathIndex = -1;
        clearing = false;
        for (int i = 0; i < this.buttons.size(); i++) {
            Path pathBefore = new Path();
            Path pOrig = this.paths.get(i);

            Matrix translate = new Matrix();
           // translate.setTranslate(-80, -40);
            pOrig.transform(translate);
            this.paths.set(i, pOrig);

            float[] approx = pOrig.approximate(0);
            pathBefore.moveTo(approx[1]-80, approx[2]-40);
            pathBefore.lineTo(approx[1]-80, approx[2]-40);

            Button btn = this.buttons.get(i);
            ObjectAnimator.ofFloat(btn, View.X, View.Y, pathBefore).setDuration(5000).start();
        }
        mPaint.setXfermode(null);


    }

    public void initAnimation() {
        invalidate();
        for (int i = 0; i < this.buttons.size(); i++) {
            Button btn = this.buttons.get(i);
            int index = i;
            Path transPath = this.paths.get(index);
//            if (!translated) {
                Matrix translate = new Matrix();
                translate.setTranslate(-80, -40);

                transPath.transform(translate);

            Path nPath = new Path();
            float[] approx = transPath.approximate(0);
            nPath.moveTo(approx[1], approx[2]);
//            nPath.addPath(path);
            nPath.addPath(transPath);
            ObjectAnimator animation = ObjectAnimator.ofFloat(btn, View.X, View.Y, nPath).setDuration(duration);
            animation.start();


        }
        for (Path p : this.paths) {
            Matrix translate = new Matrix();
            translate.setTranslate(80, 40);

            p.transform(translate);
        }

//        path2 = new Path();
//        path2.quadTo(400, 200, 900, 300);
//        ObjectAnimator.ofFloat(this, View.X, View.Y, path).setDuration(5000).start();

    }

    //
    public int findPath(float x, float y) {

        for (int i = 0; i < this.paths.size(); i++) {
            Path p = this.paths.get(i);
            float[] approx = p.approximate(0);
            System.out.println("pineapple");

            System.out.println(approx[1] - 100 < x );
            System.out.println(approx[1] + 100 > x);
            System.out.println(approx[2] - 100 < y );
            System.out.println(approx[1] + 100 > x);
            if (approx[1] - 60 < x && approx[1] + 60 > x && approx[2] - 60 < y && approx[2] + 60 > y) {
                return i;
            } else if (approx[approx.length - 2] - 60 < x && approx[approx.length - 2] + 60 > x && approx[approx.length - 1] - 60 < y && approx[approx.length - 1] + 60 > y) {
                return i;
            }
        }
        return -1;

    }

    public boolean isPathStart(float x, float y) {

        for (int i = 0; i < this.paths.size(); i++) {
            Path p = this.paths.get(i);
            float[] approx = p.approximate(0);


            if (approx[1] -60 < x && approx[1] + 60 > x && approx[2] - 60 < y && approx[2] + 60 > y) {
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                pathIndex = findPath(x, y);
                System.out.println(pathIndex);
                if (pathIndex > -1) {

                  //  drawn = false;
                    if (!isPathStart(x, y)) {
                        path = this.paths.get(pathIndex);
                        path.moveTo(x, y);
                        invalidate();
                    } else {
                        path = this.paths.get(pathIndex);
                        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

                        mCanvas.drawPath(path, mPaint);
                        for (int i = 0; i < this.buttons.size(); i++) {
                            Path pOrig = this.paths.get(i);


                            mCanvas.drawPath(pOrig, mPaint);

                        }
                        mPaint.setXfermode(null);
                        path.reset();
                        path.moveTo(x, y);
                        invalidate();

                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                if (pathIndex > -1) {
                    path.lineTo(x, y);
                    if (path != null && pathIndex > -1) {
                        Path addPath = new Path();
                        addPath.addPath(path);
                        this.paths.set(pathIndex, addPath);
                    }
                    mCanvas.drawPath(path, mPaint);
                    invalidate();
                    path.reset();

                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (pathIndex > -1) {
                    path.lineTo(x, y);
                    mCanvas.drawPath(path, mPaint);

                    invalidate();

                }
                break;
            default:

                break;


        }

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        originalBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < this.buttons.size(); i++) {
            Path pOrig = this.paths.get(i);

                mCanvas.drawPath(pOrig, mPaint);
               // drawn = true;
            //}
            canvas.drawBitmap(bitmap, 0, 0, mPaint);

        }

    }

    public ArrayList<Button> getButtons() {
        return this.buttons;
    }

    public ArrayList<Path> getPaths() {
        return this.paths;
    }

    public void setPaths(ArrayList<Path> newPaths) {
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));





        invalidate();
        for (int i = 0; i < this.paths.size(); i++) {
            Path p = this.paths.get(i);

            mCanvas.drawPath(p, mPaint);

            paths.get(i).reset();

        }
        mPaint.setXfermode(null);
        this.paths = newPaths;



    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCanvasVars() {
        int w = 1794;
        int h = 866;

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        originalBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmap);
    }

    public void draw() {
        //drawn = false;
        for (int i = 0; i < this.buttons.size(); i++) {
            Path pOrig = this.paths.get(i);

            mCanvas.drawPath(pOrig, mPaint);


        }
    }

}