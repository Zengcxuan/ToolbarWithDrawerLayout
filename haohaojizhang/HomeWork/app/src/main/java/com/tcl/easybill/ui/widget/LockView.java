package com.tcl.easybill.ui.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import com.tcl.easybill.Utils.LockViewUtil;

public class LockView extends View{

    private static final int COUNT_PER_RAW = 3;
    private static final int DURATION = 1000;
    private static final int MIN_PWD_NUMBER = 3;
    //@Fields STATUS_NO_PWD : 当前没有保存密码
    public static final int STATUS_NO_PWD = 0;
    //@Fields STATUS_RETRY_PWD : 需要再输入一次密码
    public static final int STATUS_RETRY_PWD = 1;
    //@Fields STATUS_SAVE_PWD : 成功保存密码
    public static final int STATUS_SAVE_PWD = 2;
    //@Fields STATUS_SUCCESS_PWD : 成功验证密码
    public static final int STATUS_SUCCESS_PWD = 3;
    //@Fields STATUS_FAILED_PWD : 验证密码失败
    public static final int STATUS_FAILED_PWD = 4;
    //@Fields STATUS_ERROR : 输入密码长度不够
    public static final int STATUS_ERROR = 5;

    private int width;
    private int height;
    private int padding = 2;
    private int colorSuccess = Color.BLUE;
    private int colorFailed = Color.RED;
    private int minPwdNumber = MIN_PWD_NUMBER;
    private List<LockViewCircle> lockViewCircles = new ArrayList<>();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mPath = new Path();
    private Path backupsPath = new Path();
    private List<Integer> result = new ArrayList<>();
    private int status = STATUS_NO_PWD;
    private OnLockListener listener;
    private Handler handler = new Handler();

    public LockView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        initStatus();
    }

    public LockView(Context context, AttributeSet attrs){
        super(context, attrs);
        initStatus();
    }

    public LockView(Context context){
        super(context);
        initStatus();
    }

    /**
     * @Description:初始化当前密码的状态
     */
    public void initStatus(){
        if(TextUtils.isEmpty(LockViewUtil.getPwd(getContext()))){
            status = STATUS_NO_PWD;
        }else{
            status = STATUS_SAVE_PWD;
        }
    }

    public int getCurrentStatus(){
        return status;
    }

    /**
     * @Description:初始化参数，若不调用则使用默认值
     * @param padding 圆球之间的间距
     * @param colorSuccess 密码正确时圆球的颜色
     * @param colorFailed 密码错误时圆球的颜色
     * @return LockView
     */
    public LockView initParam(int padding ,int colorSuccess ,int colorFailed ,int minPwdNumber){
        this.padding = padding;
        this.colorSuccess = colorSuccess;
        this.colorFailed = colorFailed;
        this.minPwdNumber = minPwdNumber;
        init();
        return this;
    }

    /**
     * @Description:若第一次调用则创建圆球，否则更新圆球
     */
    private void init(){
        int circleRadius = (width - (COUNT_PER_RAW + 1) * padding) / COUNT_PER_RAW /2;
        if(lockViewCircles.size() == 0){
            for(int i = 0; i < COUNT_PER_RAW * COUNT_PER_RAW; i++){
                createCircles(circleRadius, i);
            }
        }else{
            for(int i = 0; i < COUNT_PER_RAW * COUNT_PER_RAW; i++){
                updateCircles(lockViewCircles.get(i), circleRadius);
            }
        }
    }

    private void createCircles(int radius, int position){
        int centerX = (position % 3 + 1) * padding + (position % 3 * 2 + 1) * radius;
        int centerY = (position / 3 + 1) * padding + (position / 3 * 2 + 1) * radius;
        LockViewCircle lockViewCircle = new LockViewCircle(centerX, centerY, colorSuccess, colorFailed, radius, position);
        lockViewCircles.add(lockViewCircle);
    }

    private void updateCircles(LockViewCircle lockViewCircle, int radius){
        int centerX = (lockViewCircle.getPosition() % 3 + 1) * padding + (lockViewCircle.getPosition() % 3 * 2 + 1) * radius;
        int centerY = (lockViewCircle.getPosition() / 3 + 1) * padding + (lockViewCircle.getPosition() / 3 * 2 + 1) * radius;
        lockViewCircle.setCenterX(centerX);
        lockViewCircle.setCenterY(centerY);
        lockViewCircle.setRadius((LockViewCircle.DEFAULT_CENTER_BOUND)*2);//一个圆的大小
        lockViewCircle.setColorSuccess(colorSuccess);
        lockViewCircle.setColorFailed(colorFailed);
    }

    @Override
    protected void onDraw(Canvas canvas){
        init();
        //绘制圆
        for(int i = 0; i < lockViewCircles.size() ; i++){
            lockViewCircles.get(i).draw(canvas, mPaint);
        }
        if(result.size() != 0){
            //绘制Path
            LockViewCircle temp = lockViewCircles.get(result.get(0));
            mPaint.setColor(temp.getStatus() == LockViewCircle.STATUS_FAILED ? colorFailed : colorSuccess);
            mPaint.setStrokeWidth(LockViewCircle.DEFAULT_CENTER_BOUND);
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                backupsPath.reset();
                for(int i = 0; i < lockViewCircles.size() ; i++){
                    LockViewCircle lockViewCircle = lockViewCircles.get(i);
                    if(event.getX() >= lockViewCircle.getCenterX() - lockViewCircle.getRadius()
                            && event.getX() <= lockViewCircle.getCenterX() + lockViewCircle.getRadius()
                            && event.getY() >= lockViewCircle.getCenterY() - lockViewCircle.getRadius()
                            && event.getY() <= lockViewCircle.getCenterY() + lockViewCircle.getRadius()){
                        lockViewCircle.setStatus(LockViewCircle.STATUS_TOUCH);
                        //将这个点放入Path
                        backupsPath.moveTo(lockViewCircle.getCenterX(), lockViewCircle.getCenterY());
                        //放入结果
                        result.add(lockViewCircle.getPosition());
                        break;
                    }
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                for(int i = 0; i < lockViewCircles.size() ; i++){
                    LockViewCircle lockViewCircle = lockViewCircles.get(i);
                    if(event.getX() >= lockViewCircle.getCenterX() - lockViewCircle.getRadius()
                            && event.getX() <= lockViewCircle.getCenterX() + lockViewCircle.getRadius()
                            && event.getY() >= lockViewCircle.getCenterY() - lockViewCircle.getRadius()
                            && event.getY() <= lockViewCircle.getCenterY() + lockViewCircle.getRadius()){
                        if(!result.contains(lockViewCircle.getPosition())){
                            lockViewCircle.setStatus(LockViewCircle.STATUS_TOUCH);
                            //首先判断是否连线中间也有满足条件的圆
                            LockViewCircle lastLockViewCircle = lockViewCircles.get(result.get(result.size() - 1));
                            int cx = (lastLockViewCircle.getCenterX() + lockViewCircle.getCenterX()) / 2;
                            int cy = (lastLockViewCircle.getCenterY() + lockViewCircle.getCenterY()) / 2;
                            for(int j = 0; j < lockViewCircles.size(); j++){
                                LockViewCircle tempLockViewCircle = lockViewCircles.get(j);
                                if(cx >= tempLockViewCircle.getCenterX() - tempLockViewCircle.getRadius()
                                        && cx <= tempLockViewCircle.getCenterX() + tempLockViewCircle.getRadius()
                                        && cy >= tempLockViewCircle.getCenterY() - tempLockViewCircle.getRadius()
                                        && cy <= tempLockViewCircle.getCenterY() + tempLockViewCircle.getRadius()){
                                    //处理满足条件的圆
                                    backupsPath.lineTo(tempLockViewCircle.getCenterX(), tempLockViewCircle.getCenterY());
                                    //放入结果
                                    tempLockViewCircle.setStatus(LockViewCircle.STATUS_TOUCH);
                                    result.add(tempLockViewCircle.getPosition());
                                }
                            }
                            //处理现在的圆
                            backupsPath.lineTo(lockViewCircle.getCenterX(), lockViewCircle.getCenterY());
                            //放入结果
                            lockViewCircle.setStatus(LockViewCircle.STATUS_TOUCH);
                            result.add(lockViewCircle.getPosition());
                            break;
                        }
                    }
                }
                mPath.reset();
                mPath.addPath(backupsPath);
                mPath.lineTo(event.getX(), event.getY());
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                mPath.reset();
                mPath.addPath(backupsPath);
                invalidate();
                if(result.size() < minPwdNumber){
                    if(listener != null){
                        listener.onError();
                    }
                    if(status == STATUS_RETRY_PWD){
                        LockViewUtil.clearPwd(getContext());
                    }
                    status = STATUS_ERROR;
                    for(int i = 0; i < result.size(); i++){
                        lockViewCircles.get(result.get(i)).setStatus(LockViewCircle.STATUS_FAILED);
                    }
                }else{
                    if(status == STATUS_NO_PWD){ //当前没有密码
                        //保存密码，重新录入
                        LockViewUtil.savePwd(getContext(), result);
                        status = STATUS_RETRY_PWD;
                        if(listener != null){
                            listener.onTypeInOnce(LockViewUtil.listToString(result));
                        }
                    }else if(status == STATUS_RETRY_PWD){ //需要重新绘制密码
                        //判断两次输入是否相等
                        if(LockViewUtil.getPwd(getContext()).equals(LockViewUtil.listToString(result))){
                            status = STATUS_SAVE_PWD;
                            if(listener != null){
                                listener.onTypeInTwice(LockViewUtil.listToString(result), true);
                            }
                            for(int i = 0; i < result.size(); i++){
                                lockViewCircles.get(result.get(i)).setStatus(LockViewCircle.STATUS_SUCCESS);
                            }
                        }else{
                            status = STATUS_NO_PWD;
                            LockViewUtil.clearPwd(getContext());
                            if(listener != null){
                                listener.onTypeInTwice(LockViewUtil.listToString(result), false);
                            }
                            for(int i = 0; i < result.size(); i++){
                                lockViewCircles.get(result.get(i)).setStatus(LockViewCircle.STATUS_FAILED);
                            }
                        }
                    }else if(status == STATUS_SAVE_PWD){ //验证密码
                        //判断密码是否正确
                        if(LockViewUtil.getPwd(getContext()).equals(LockViewUtil.listToString(result))){
                            status = STATUS_SUCCESS_PWD;
                            if(listener != null){
                                listener.onUnLock(LockViewUtil.listToString(result), true);
                            }
                            for(int i = 0; i < result.size(); i++){
                                lockViewCircles.get(result.get(i)).setStatus(LockViewCircle.STATUS_SUCCESS);
                            }
                        }else{
                            status = STATUS_FAILED_PWD;
                            if(listener != null){
                                listener.onUnLock(LockViewUtil.listToString(result), false);
                            }
                            for(int i = 0; i < result.size(); i++){
                                lockViewCircles.get(result.get(i)).setStatus(LockViewCircle.STATUS_FAILED);
                            }
                        }
                    }
                }
                invalidate();
                handler.postDelayed(new Runnable(){

                    @Override
                    public void run(){
                        result.clear();
                        mPath.reset();
                        backupsPath.reset();
                        //     initStatus();
                        // 重置下状态
                        if(status == STATUS_SUCCESS_PWD || status == STATUS_FAILED_PWD){
                            status = STATUS_SAVE_PWD;
                        }else if(status == STATUS_ERROR){
                            initStatus();
                        }
                        for(int i = 0; i < lockViewCircles.size(); i++){
                            lockViewCircles.get(i).setStatus(LockViewCircle.STATUS_DEFAULT);
                        }
                        invalidate();
                    }
                }, DURATION);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        width = View.MeasureSpec.getSize(widthMeasureSpec);
        height = width - getPaddingLeft() - getPaddingRight() + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, height);
    }

    public void setOnLockListener(OnLockListener listener){
        this.listener = listener;
    }

    public interface OnLockListener{
        /**
         * @Description:没有密码时，第一次录入密码触发器
         */
        void onTypeInOnce(String input);
        /**
         * @Description:已经录入第一次密码，录入第二次密码触发器
         */
        void onTypeInTwice(String input ,boolean isSuccess);
        /**
         * @Description:验证密码触发器
         */
        void onUnLock(String input ,boolean isSuccess);

        /**
         * @Description:密码长度不够
         */
        void onError();
    }
}
