package com.zsf.guesssong.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by zsf on 2016/12/26.
 * 工具类直接使用静态方法
 */
public class Util {

    public static View getView(Context context,int layoutId){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(layoutId,null);
        return layout;

    }
}
