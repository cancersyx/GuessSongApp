package com.zsf.guesssong.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.zsf.guesssong.R;

/**
 * Created by zsf
 * 通过界面
 */
public class AllPassActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_pass_view);

        //隐藏youshang右上角的金币
        FrameLayout view = (FrameLayout) findViewById(R.id.layout_bar_coin);
        view.setVisibility(View.INVISIBLE);
    }
}