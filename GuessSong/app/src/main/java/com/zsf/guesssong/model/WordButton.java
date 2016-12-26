package com.zsf.guesssong.model;

import android.widget.Button;

/**
 *
 */
public class WordButton {

    public int mIndex;//索引
    public boolean mIsVisiable;//是否可见
    public String mWordString;
    public Button mViewButton;

    public WordButton(){
        mIsVisiable = true;
        mWordString = "";
    }

}
