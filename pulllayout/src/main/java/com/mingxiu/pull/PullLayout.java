package com.mingxiu.pull;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ----------BigGod be here!----------/
 * ***┏┓******┏┓*********
 * *┏━┛┻━━━━━━┛┻━━┓*******
 * *┃             ┃*******
 * *┃     ━━━     ┃*******
 * *┃             ┃*******
 * *┃  ━┳┛   ┗┳━  ┃*******
 * *┃             ┃*******
 * *┃     ━┻━     ┃*******
 * *┃             ┃*******
 * *┗━━━┓     ┏━━━┛*******
 * *****┃     ┃神兽保佑*****
 * *****┃     ┃代码无BUG！***
 * *****┃     ┗━━━━━━━━┓*****
 * *****┃              ┣┓****
 * *****┃              ┏┛****
 * *****┗━┓┓┏━━━━┳┓┏━━━┛*****
 * *******┃┫┫****┃┫┫********
 * *******┗┻┛****┗┻┛*********
 * ━━━━━━神兽出没━━━━━━
 * 版权所有：个人
 * 作者：Created by a.wen.
 * 创建时间：2017/4143
 * Email：13872829574@163.com
 * 内容描述：通用刷新控件
 * 修改人：a.wen
 * 修改时间：${DATA}
 * 修改备注：
 * 修订历史：1.0
 */
public class PullLayout extends FrameLayout {
    private static String TAG = "PullLayout";

    //拉拽监听
    private OnPullListener mPullListener;
    //加载监听
    private OnRefreshListener mRefreshListener;
    //动画插值器
    protected Interpolator interpolator;
    //刷新头的高度
    private int headViewHeight = 300;
    //加载更多的高度
    private int bottomViewHeight = 100;
    //下拉 百分多少
    private float pullRate = 0.6f;

    //刷新头
    private PullHead headView;
    //包含的View;
    private View mTarget;
    //加载更多的尾
    private ProgressBar bottomView;

    //拉拽 距离 Y 轴 px值
    private float pullY;

    //按下去时候的 Y 轴 px值
    private float downY;
    //起来时候的 Y 轴 px值
    private float upY;
    //移动中 Y 轴 px值
    private float moveY;

    //是否正在上拉
    private boolean isPullUping;
    //是否正在下拉
    private boolean isDropDowning;

    //是否 上拉 加载中
    private boolean isPullUpMoreing;
    //是否 下拉 加载中
    private boolean isDropDownRefreshing;

    //能下拉
    private boolean canDropDown = true;
    //能上拉
    private boolean canPullUp = true;
    //是不是出于测试状态
    private boolean isTest;


    private static final int IS_PULLUPING = 1;
    private static final int IS_DROPDOWNING = 2;
    private static final int IS_PULLUP_MOREING = 3;
    private static final int IS_DROPDOWN_REFRESHING = 4;
    private static final int PULLUP_BACK = 5;
    private static final int DROPDOWN_BACK = 6;
    private static final int COMPLETE_PULLUP_BACK = 7;
    private static final int COMPLETE_DROPDOWN_BACK = 8;

    @IntDef(value = {IS_PULLUPING, IS_DROPDOWNING, IS_PULLUP_MOREING, IS_DROPDOWN_REFRESHING, PULLUP_BACK, DROPDOWN_BACK, COMPLETE_PULLUP_BACK, COMPLETE_DROPDOWN_BACK})
    @Retention(RetentionPolicy.SOURCE)
    private @interface TYPE {
    }


    public PullLayout(@NonNull Context context) {
        super(context);
        setHeadView(new PullHead(getContext()));
        setBottomView(new ProgressBar(getContext()));
        if (getChildCount() > 1) {
            Log.d(TAG, "PullLayout: 只能只能包含一个View");
            throw new RuntimeException("只能只能包含一个View");
        }
    }

    public PullLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setHeadView(new PullHead(getContext()));
        setBottomView(new ProgressBar(getContext()));
        if (getChildCount() > 1) {
            Log.d(TAG, "PullLayout: 只能只能包含一个View");
            throw new RuntimeException("只能只能包含一个View");
        }
    }

    public PullLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setHeadView(new PullHead(getContext()));
        setBottomView(new ProgressBar(getContext()));
        if (getChildCount() > 1) {
            Log.d(TAG, "PullLayout: 只能只能包含一个View");
            throw new RuntimeException("只能只能包含一个View");
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setmTarget(getChildAt(0));
        if (getmTarget() == null) {
            return;
        }
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.TOP;
        layoutParams.setMargins(0, -getHeadViewHeight(), 0, 0);
        getHeadView().setLayoutParams(layoutParams);
        addView(getHeadView());

        getBottomView().setVisibility(GONE);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        getBottomView().setLayoutParams(params);
        addView(getBottomView());

        setInterpolator(new OvershootInterpolator(1));

    }

    public void log(String TAG, String content) {
        if (isTest()) {
            Log.d(TAG, content);
        }
    }


    @Override//事件拦截
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                log(TAG, "onInterceptTouchEvent: ACTION_DOWN" + getPullY());
                setDownY(ev.getY());
                if (isDropDownRefreshing() || isPullUpMoreing()) {                                 //如果正在刷新或者加载中 事件拦截掉
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                log(TAG, "onInterceptTouchEvent: ACTION_UP" + getPullY());
                break;
            case MotionEvent.ACTION_MOVE:
                log(TAG, "onInterceptTouchEvent: ACTION_MOVE" + getPullY());
                setMoveY(ev.getY());
                setPullY(getMoveY() - getDownY());

                if (getPullY() > 0) {//下拉
                    if (isCanDropDown() && !canChildScrollDown() && !isPullUpMoreing()) {
                        //设置正在下拉
                        return true;
                    }
                } else if (getPullY() < 0) {//上拉
                    if (isCanPullUp() && !canChildScrollUp() && !isDropDowning()) {
                        //设置正在上拉
                        return true;
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override//事件消费
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                log(TAG, "onTouchEvent: ACTION_DOWN" + getPullY());
                break;
            case MotionEvent.ACTION_MOVE:
                log(TAG, "onTouchEvent: ACTION_MOVE" + getPullY());
                setMoveY(e.getY());
                setPullY(getMoveY() - getDownY());

                if (getPullY() > 0) {//下拉
                    if (isCanDropDown() && !canChildScrollDown() && !isPullUpMoreing()) {
                        //设置正在下拉
                        handleAnimation(IS_DROPDOWNING);
                    }
                } else {
                    if (isDropDowning()) {
                        //设置正在下拉
                        handleAnimation(IS_DROPDOWNING);
                    }
                }

                if (getPullY() < 0) {//上拉
                    if (isCanPullUp() && !canChildScrollUp() && !isDropDowning()) {
                        //设置正在上拉
                        handleAnimation(IS_PULLUPING);
                    }
                } else {
                    if (isPullUping()) {
                        //设置正在上拉
                        handleAnimation(IS_PULLUPING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                log(TAG, "onTouchEvent: ACTION_UP" + getPullY());
                setUpY(e.getY());
                setPullY(getUpY() - getDownY());
                if (isDropDowning()) {
                    if (getPullY() > getHeadViewHeight() * getPullRate()) {                           //刷新
                        handleAnimation(IS_DROPDOWN_REFRESHING);
                    } else {                                                                          //下拉距离不够回弹
                        handleAnimation(DROPDOWN_BACK);
                    }
                }

                if (isPullUping()) {
                    if (Math.abs(getPullY()) > getBottomViewHeight() * getPullRate()) {               //加载 更多
                        handleAnimation(IS_PULLUP_MOREING);
                    } else {                                                                          //上拉距离不够回弹
                        handleAnimation(PULLUP_BACK);
                    }
                }
                break;

        }
        return true;

    }

    long currentTimeMillis;

    void handleAnimation(@TYPE int type) {
        setPullUpMoreing(false);
        setPullUping(false);
        setDropDowning(false);
        setDropDownRefreshing(false);
        switch (type) {
            case IS_DROPDOWN_REFRESHING: //正在下拉刷新
                Log.d(TAG, "正在下拉刷新... ");
                setDropDownRefreshing(true);
                loadTargetAnimator(getPullY() > getHeadViewHeight() ? getHeadViewHeight() : getPullY(), getHeadViewHeight());
                getHeadView().startLoad();
                if (mRefreshListener != null) {
                    mRefreshListener.onRefresh();
                }
                break;
            case IS_PULLUP_MOREING:      //正在上拉加载
                Log.d(TAG, "正在上拉加载...");
                setPullUpMoreing(true);
                getBottomView().setVisibility(VISIBLE);
                if (mRefreshListener != null) {
                    mRefreshListener.onMore();
                }
                break;
            case IS_DROPDOWNING:         //正在下拉中
                if (System.currentTimeMillis() - currentTimeMillis > 1000 * 3) {
                    Log.d(TAG, "正在下拉中... ");
                    currentTimeMillis = System.currentTimeMillis();
                }
                setDropDowning(true);
                if (getPullY() > getHeadViewHeight()) {
                    setPullY(getHeadViewHeight());
                } else if (getPullY() <= 0) {
                    setPullY(0);
                }
                getHeadView().startPull(getPullY());
                getHeadView().setTranslationY(getPullY());
                getmTarget().setTranslationY(getPullY());
                if (mPullListener != null) {
                    mPullListener.onDropDown(getPullY());
                }
                break;
            case IS_PULLUPING:           //正在上拉中
                if (System.currentTimeMillis() - currentTimeMillis > 1000 * 3) {
                    Log.d(TAG, "正在上拉中... ");
                    currentTimeMillis = System.currentTimeMillis();
                }
                setPullUping(true);
                getBottomView().setVisibility(VISIBLE);
                if (mPullListener != null) {
                    mPullListener.onPullUp(getPullY());
                }
                break;
            case PULLUP_BACK:            //上拉距离不够回弹
                Log.d(TAG, "上拉距离不够 回弹 ");
                getBottomView().setVisibility(GONE);
                break;
            case DROPDOWN_BACK:          //下拉距离不够回弹
                Log.d(TAG, "下拉距离不够 回弹 ");
                getHeadView().setVisibility(GONE);
                if (getPullY() <= 0) {
                    getmTarget().setTranslationY(0);
                } else {
                    loadTargetAnimator(getPullY(), 0);
                }
                break;
            case COMPLETE_PULLUP_BACK:            //上拉加载完毕
                Log.d(TAG, "上拉加载完毕 ");
                getBottomView().setVisibility(GONE);
                break;
            case COMPLETE_DROPDOWN_BACK:            //下拉刷新完毕
                Log.d(TAG, "下拉刷新完毕 ");
                loadTargetAnimator((long) (1000 * 0.5), getHeadViewHeight(), 0).addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        getHeadView().stopPull();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                break;

        }

    }


    private ObjectAnimator loadTargetAnimator(float... value) {
        return loadTargetAnimator(0, value);
    }

    private ObjectAnimator loadTargetAnimator(long time, float... value) {
        ObjectAnimator mAnimator = ObjectAnimator.ofFloat(getmTarget(), "translationY", value);
        mAnimator.setDuration(100);
        mAnimator.setInterpolator(getInterpolator());
        mAnimator.setRepeatCount(0);
        mAnimator.setStartDelay(time);
        mAnimator.start();
        return mAnimator;
    }


    public void completeAll() {
        if (isDropDownRefreshing()) {
            handleAnimation(COMPLETE_DROPDOWN_BACK);
        }
        if (isPullUpMoreing()) {
            handleAnimation(COMPLETE_PULLUP_BACK);
        }
    }


    //动画插值器
    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    //拉拽时候的监听
    public void setOnPullListener(OnPullListener onPullListener) {
        this.mPullListener = onPullListener;
    }

    //刷新  加载监听
    public void setOnRefreshListener(OnRefreshListener mOnRefreshListener) {
        this.mRefreshListener = mOnRefreshListener;
    }

    //设置下拉 百分之开始 刷新
    public void setPullRate(float pullRate) {
        this.pullRate = pullRate;
    }

    public float getPullRate() {
        return pullRate;
    }

    //能否下拉刷新
    public void setCanPull(boolean b) {
        this.canDropDown = b;
    }

    public boolean isCanDropDown() {
        return canDropDown;
    }

    //能否加载更多
    public void setCanMore(boolean b) {
        this.canPullUp = b;
    }

    public boolean isCanPullUp() {
        return canPullUp;
    }

    //获得头部View的对象
    public PullHead getHeadView() {
        return headView;
    }

    //设置头部View 的 高度
    public void setHeadViewHeight(int headViewHeight) {
        this.headViewHeight = headViewHeight;
        requestLayout();
    }

    public int getHeadViewHeight() {
        return headViewHeight;
    }

    //获得底部View的对象
    public ProgressBar getBottomView() {
        return bottomView;
    }

    //设置底部View 的 高度
    public void setBottomViewHeight(int bottomViewHeight) {
        this.bottomViewHeight = bottomViewHeight;
    }

    public int getBottomViewHeight() {
        return bottomViewHeight;
    }


    public boolean canChildScrollDown() {
        if (mTarget == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    public boolean canChildScrollUp() {
        if (mTarget == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                if (absListView.getChildCount() > 0) {
                    int lastChildBottom = absListView.getChildAt(absListView.getChildCount() - 1).getBottom();
                    return absListView.getLastVisiblePosition() == absListView.getAdapter().getCount() - 1 && lastChildBottom <= absListView.getMeasuredHeight();
                } else {
                    return false;
                }

            } else {
                return ViewCompat.canScrollVertically(mTarget, 1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, 1);
        }
    }

    private Interpolator getInterpolator() {
        return interpolator;
    }

    private void setHeadView(PullHead headView) {
        this.headView = headView;
    }

    private void setBottomView(ProgressBar bottomView) {
        this.bottomView = bottomView;
    }

    private float getPullY() {
        return pullY;
    }

    private void setPullY(float pullY) {
        this.pullY = pullY;
    }

    private float getDownY() {
        return downY;
    }

    private void setDownY(float downY) {
        this.downY = downY;
    }

    private float getUpY() {
        return upY;
    }

    private void setUpY(float upY) {
        this.upY = upY;
    }

    private float getMoveY() {
        return moveY;
    }

    private void setMoveY(float moveY) {
        this.moveY = moveY;
    }

    private boolean isPullUping() {
        return isPullUping;
    }

    private void setPullUping(boolean pullUping) {
        isPullUping = pullUping;
    }

    private boolean isDropDowning() {
        return isDropDowning;
    }

    private void setDropDowning(boolean dropDowning) {
        isDropDowning = dropDowning;
    }

    private boolean isPullUpMoreing() {
        return isPullUpMoreing;
    }

    private void setPullUpMoreing(boolean pullUpMoreing) {
        isPullUpMoreing = pullUpMoreing;
    }

    private boolean isDropDownRefreshing() {
        return isDropDownRefreshing;
    }

    private void setDropDownRefreshing(boolean dropDownRefreshing) {
        isDropDownRefreshing = dropDownRefreshing;
    }

    private View getmTarget() {
        return mTarget;
    }

    private void setmTarget(View mTarget) {
        this.mTarget = mTarget;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    public interface OnRefreshListener {
        void onRefresh();

        void onMore();
    }


    public interface OnPullListener {

        void onPullUp(float pullValue);

        void onDropDown(float pullValue);

    }

    private class PullHead extends FrameLayout {

        private ImageView beeImg, rocketImg;//蜜蜂和火箭的图片

        private ObjectAnimator mAnimator;


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getHeadViewHeight(), MeasureSpec.EXACTLY));
        }

        public PullHead(Context context) {
            super(context);
            beeImg = new ImageView(context);
            rocketImg = new ImageView(context);

            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            rocketImg.setLayoutParams(params);
            beeImg.setLayoutParams(params);

            beeImg.setBackgroundResource(R.mipmap.pxwk_pull_bee);
            rocketImg.setBackgroundResource(R.mipmap.pxwk_pull_rocket);

            addView(rocketImg);
            addView(beeImg);
        }

        /**
         * 正在下拉... 处理头部
         *
         * @param pullHeight
         */
        public void startPull(float pullHeight) {
            if (getVisibility() != VISIBLE) {
                setVisibility(VISIBLE);
            }
            rocketImg.setRotation((pullHeight / getHeight()) * 360);
            beeImg.setScaleX(pullHeight / getHeight());
            beeImg.setScaleY(pullHeight / getHeight());
            setTranslationY(pullHeight);
        }

        /**
         * 正在
         */
        public void startLoad() {
            if (mAnimator == null) {
                mAnimator = ObjectAnimator.ofFloat(rocketImg, "rotation", 0f, 720f);
                mAnimator.setDuration(3 * 1000);
                mAnimator.setInterpolator(new LinearInterpolator());
                mAnimator.setRepeatCount(ValueAnimator.INFINITE);
            }
            if (!mAnimator.isRunning()) {
                beeImg.setScaleX(1);
                beeImg.setScaleY(1);
                setTranslationY(getHeight());
                mAnimator.start();
                if (getVisibility() != VISIBLE) {
                    setVisibility(VISIBLE);
                }
            }

        }

        public void stopPull() {
            if (getVisibility() != GONE) {
                setVisibility(GONE);
            }
            setTranslationY(-getHeight());
            if (mAnimator != null && mAnimator.isRunning()) {
                mAnimator.cancel();
            }
        }


        public ImageView getBeeImg() {
            return beeImg;
        }

        public void setBeeImg(ImageView beeImg) {
            this.beeImg = beeImg;
        }

        public ImageView getRocketImg() {
            return rocketImg;
        }

        public void setRocketImg(ImageView rocketImg) {
            this.rocketImg = rocketImg;
        }

    }
}
