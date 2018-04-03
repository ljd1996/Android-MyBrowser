package com.example.admin.mybrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by admin on 2017/3/9.
 */

public class ViewMatrix extends ImageView {

    public static final int STATUS_INIT = 1;//初始化状态常量
    public static final int STATUS_ZOOM_OUT = 2;//图片放大
    public static final int STATUS_ZOOM_IN = 3;// 图片缩小
    public static final int STATUS_MOVE = 4; //图片拖动
    private Matrix matrix = new Matrix();
    private Bitmap sourceBitmap;
    private int currentStatus;//当前操作的状态
    private int width;//MyViewThree的宽度
    private int height;//MyViewThree控件的高度
    private float centerPointX;//两手指中心点的横坐标值
    private float centerPointY;//中心点的纵坐标值
    private float currentBitmapWidth;//当前图片的宽度，图片被缩放时，这个值会一起变动
    private float currentBitmapHeight; //当前图片的高度，图片被缩放时，这个值会一起变动
    private float lastX = -1;//上次手指移动时的横坐标
    private float lastY = -1;//上次手指移动时的纵坐标
    private float movedDistanceX;//手指在横坐标方向上的移动距离
    private float movedDistanceY;//手指在纵坐标方向上的移动距离
    private float totalTranslateX;//图片在矩阵上的横向偏移值
    private float totalTranslateY;//图片在矩阵上的纵向偏移值
    private float totalScale;//图片在矩阵上的总缩放比例
    private float currentScale;//手指移动的距离所造成的缩放比例
    private float initScale;//图片初始化时的缩放比例
    private double lastDistance;//上次两指之间的距离

    public ViewMatrix(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentStatus = STATUS_INIT;
    }

    public void setImageBitmap(Bitmap bitmap) {
        sourceBitmap = bitmap;
        invalidate();//调用onDraw方法初始化
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            // 分别获取到MyViewThree的宽度和高度
            width = getWidth();
            height = getHeight();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    // 当有两个手指按在屏幕上时，计算两指之间的距离
                    lastDistance = distance(event);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    // 只有单指按在屏幕上移动时，为拖动状态
                    float xMove = event.getX();
                    float yMove = event.getY();
                    if (lastX == -1 && lastY == -1) {
                        lastX = xMove;
                        lastY = yMove;
                    }
                    currentStatus = STATUS_MOVE;
                    movedDistanceX = xMove - lastX;
                    movedDistanceY = yMove - lastY;
                    // 进行边界检查，不允许将图片拖出边界
                    if (totalTranslateX + movedDistanceX > 0) {
                        movedDistanceX = 0;
                    } else if (width - (totalTranslateX + movedDistanceX) > currentBitmapWidth) {
                        movedDistanceX = 0;
                    }
                    if (totalTranslateY + movedDistanceY > 0) {
                        movedDistanceY = 0;
                    } else if (height - (totalTranslateY + movedDistanceY) > currentBitmapHeight) {
                        movedDistanceY = 0;
                    }

                    invalidate();// 调用onDraw()方法绘制图片
                    lastX = xMove;
                    lastY = yMove;
                } else if (event.getPointerCount() == 2) {
                    // 有两个手指按在屏幕上移动时，为缩放状态
                    centerPoint(event);
                    double fingerDis = distance(event);
                    if (fingerDis > lastDistance) {
                        currentStatus = STATUS_ZOOM_OUT;
                    } else {
                        currentStatus = STATUS_ZOOM_IN;
                    }
                    // 进行缩放倍数检查，最大只允许将图片放大4倍，最小可以缩小到初始化比例
                    if ((currentStatus == STATUS_ZOOM_OUT && totalScale < 4 * initScale)
                            || (currentStatus == STATUS_ZOOM_IN && totalScale > initScale)) {
                        currentScale = (float) (fingerDis / lastDistance);
                        totalScale = totalScale * currentScale;
                        if (totalScale > 4 * initScale) {
                            totalScale = 4 * initScale;
                        } else if (totalScale < initScale) {
                            totalScale = initScale;
                        }
                        // 调用onDraw()方法绘制图片
                        invalidate();
                        lastDistance = fingerDis;
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    // 手指离开屏幕时将临时值还原
                    lastX = -1;
                    lastY = -1;
                }
                break;
            case MotionEvent.ACTION_UP:
                // 手指离开屏幕时将临时值还原
                lastX = -1;
                lastY = -1;
                break;
            default:
                break;
        }
        return true;
    }

    //根据currentStatus的值来决定对图片进行什么样的绘制操作。
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (currentStatus) {
            case STATUS_ZOOM_OUT:
            case STATUS_ZOOM_IN:
                zoom(canvas);
                break;
            case STATUS_MOVE:
                move(canvas);
                break;
            case STATUS_INIT:
                initBitmap(canvas);
        }
    }

    //对图片进行缩放处理。
    private void zoom(Canvas canvas) {
        matrix.reset();
        // 将图片按总缩放比例进行缩放
        matrix.postScale(totalScale, totalScale);
        float scaledWidth = sourceBitmap.getWidth() * totalScale;
        float scaledHeight = sourceBitmap.getHeight() * totalScale;
        float translateX = 0f;
        float translateY = 0f;
        // 如果当前图片宽度小于组件宽度，则按屏幕中心的横坐标进行水平缩放。否则按两指的中心点的横坐标进行水平缩放
        if (currentBitmapWidth < width) {
            translateX = (width - scaledWidth) / 2f;
        } else {
            translateX = totalTranslateX * currentScale + centerPointX * (1 - currentScale);
            // 进行边界检查，保证图片缩放后在水平方向上不会偏移出屏幕
            if (translateX > 0) {
                translateX = 0;
            } else if (width - translateX > scaledWidth) {
                translateX = width - scaledWidth;
            }
        }
        // 如果当前图片高度小于组件高度，则按屏幕中心的纵坐标进行垂直缩放。否则按两指的中心点的纵坐标进行垂直缩放
        if (currentBitmapHeight < height) {
            translateY = (height - scaledHeight) / 2f;
        } else {
            translateY = totalTranslateY * currentScale + centerPointY * (1 - currentScale);
            // 进行边界检查，保证图片缩放后在垂直方向上不会偏移出屏幕
            if (translateY > 0) {
                translateY = 0;
            } else if (height - translateY > scaledHeight) {
                translateY = height - scaledHeight;
            }
        }
        // 缩放后对图片进行偏移，以保证缩放后中心点位置不变
        matrix.postTranslate(translateX, translateY);
        totalTranslateX = translateX;
        totalTranslateY = translateY;
        currentBitmapWidth = scaledWidth;
        currentBitmapHeight = scaledHeight;
        canvas.drawBitmap(sourceBitmap, matrix, null);
    }

    //对图片进行平移处理
    private void move(Canvas canvas) {
        matrix.reset();
        // 根据手指移动的距离计算出总偏移值
        float translateX = totalTranslateX + movedDistanceX;
        float translateY = totalTranslateY + movedDistanceY;
        // 先按照已有的缩放比例对图片进行缩放
        matrix.postScale(totalScale, totalScale);
        // 再根据移动距离进行偏移
        matrix.postTranslate(translateX, translateY);
        totalTranslateX = translateX;
        totalTranslateY = translateY;
        canvas.drawBitmap(sourceBitmap, matrix, null);
    }

    //对图片进行初始化操作
    private void initBitmap(Canvas canvas) {
        if (sourceBitmap != null) {
            matrix.reset();
            int bitmapWidth = sourceBitmap.getWidth();
            int bitmapHeight = sourceBitmap.getHeight();
            if (bitmapWidth > width || bitmapHeight > height) {
                if (bitmapWidth - width > bitmapHeight - height) {
                    // 当图片宽度大于屏幕宽度时，将图片等比例压缩，使它可以完全显示出来
                    float ratio = width / (bitmapWidth * 1.0f);
                    matrix.postScale(ratio, ratio);
                    float translateY = (height - (bitmapHeight * ratio)) / 2f;
                    // 在纵坐标方向上进行偏移，以保证图片居中显示
                    matrix.postTranslate(0, translateY);
                    totalTranslateY = translateY;
                    totalScale = initScale = ratio;
                } else {
                    // 当图片高度大于屏幕高度时，将图片等比例压缩，使它可以完全显示出来
                    float ratio = height / (bitmapHeight * 1.0f);
                    matrix.postScale(ratio, ratio);
                    float translateX = (width - (bitmapWidth * ratio)) / 2f;
                    // 在横坐标方向上进行偏移，以保证图片居中显示
                    matrix.postTranslate(translateX, 0);
                    totalTranslateX = translateX;
                    totalScale = initScale = ratio;
                }
                currentBitmapWidth = bitmapWidth * initScale;
                currentBitmapHeight = bitmapHeight * initScale;
            } else {
                // 当图片的宽高都小于屏幕宽高时，直接让图片居中显示
                float translateX = (width - sourceBitmap.getWidth()) / 2f;
                float translateY = (height - sourceBitmap.getHeight()) / 2f;
                matrix.postTranslate(translateX, translateY);
                totalTranslateX = translateX;
                totalTranslateY = translateY;
                totalScale = initScale = 1f;
                currentBitmapWidth = bitmapWidth;
                currentBitmapHeight = bitmapHeight;
            }
            canvas.drawBitmap(sourceBitmap, matrix, null);
        }
    }

    //计算两个手指之间的距离。
    private double distance(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return Math.sqrt(disX * disX + disY * disY);
    }

    //计算两个手指之间中心点的坐标。
    private void centerPoint(MotionEvent event) {
        float xPoint0 = event.getX(0);
        float yPoint0 = event.getY(0);
        float xPoint1 = event.getX(1);
        float yPoint1 = event.getY(1);
        centerPointX = (xPoint0 + xPoint1) / 2;
        centerPointY = (yPoint0 + yPoint1) / 2;
    }
}
