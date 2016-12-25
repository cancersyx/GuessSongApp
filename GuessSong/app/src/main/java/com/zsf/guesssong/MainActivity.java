package com.zsf.guesssong;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

    private Animation mPanAnim;
    private LinearInterpolator mPanPolator;
    private Animation mBarInAnim;
    private LinearInterpolator mBarInPolator;
    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutPolator;

    private ImageButton mBtnPlay;//播放按键
    private ImageView mViewPan;//盘片的View
    private ImageView mViewBar;//唱片指针的View

    private boolean mIsPlaying  = false;//默认初始值为false
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initAnim();
        initEvent();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mBtnPlay = (ImageButton) findViewById(R.id.btn_play_start);
        mViewPan = (ImageView) findViewById(R.id.imageView_pan);
        mViewBar = (ImageView) findViewById(R.id.imageView_bar);
    }

    /**
     * 初始化动画
     */
    private void initAnim() {
        mPanAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        mPanPolator = new LinearInterpolator();
        mPanAnim.setInterpolator(mPanPolator);
        panAnimListener();

        mBarInAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_45);
        mBarInPolator = new LinearInterpolator();
//        mBarInAnim.setFillAfter(true);
        mBarInAnim.setInterpolator(mBarInPolator);
        barInAnimListener();

        mBarOutAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_d_45);
        mBarOutPolator = new LinearInterpolator();
        mBarOutAnim.setFillAfter(true);//
        mBarOutAnim.setInterpolator(mBarOutPolator);
        barOutAnimListener();
    }

    private void initEvent() {
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlayButton();
                Toast.makeText(getBaseContext(),"点了播放按钮",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 唱盘动画监听
     */
    private void panAnimListener() {
        mPanAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewBar.startAnimation(mBarOutAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 播杆落下动画监听
     */
    private void barInAnimListener() {
        mBarInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPan.startAnimation(mPanAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 播杆移出动画监听
     */
    private void barOutAnimListener() {
        mBarOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsPlaying = false;
                mBtnPlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }



    /**
     * 处理play相关动画
     */
    private void handlePlayButton() {
        if (mViewBar != null){
            if (!mIsPlaying){
                mIsPlaying = true;
                //开始播杆进入动画
                mViewBar.startAnimation(mBarInAnim);
                mBtnPlay.setVisibility(View.INVISIBLE);
            }
        }

    }

    /**
     * 暂停处理
     */
    @Override
    protected void onPause() {
        mViewPan.clearAnimation();
        super.onPause();
    }
}
