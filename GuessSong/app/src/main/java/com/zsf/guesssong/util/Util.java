package com.zsf.guesssong.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zsf.guesssong.R;
import com.zsf.guesssong.model.IAlertDialogButtonListener;

/**
 * Created by zsf on 2016/12/26.
 * 工具类直接使用静态方法
 */
public class Util {

    private static AlertDialog mAlertDialog;

    public static View getView(Context context,int layoutId){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(layoutId,null);
        return layout;

    }

    public static void startActivity(Context context,Class desti){
        Intent intent = new Intent();
        intent.setClass(context,desti);
        context.startActivity(intent);

        //关闭当前Activity
        ((Activity)context).finish();
    }

    /**
     * 显示自定义对话框
     * @param context
     * @param message
     * @param listener
     */
    public static void showDialog(final Context context, String message, final IAlertDialogButtonListener listener){

        View dialogView = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AppTheme_Transparent);
        dialogView = getView(context, R.layout.dialog_view);
        ImageButton btnOk = (ImageButton) dialogView.findViewById(R.id.btn_dialog_ok);
        ImageButton btnCancel = (ImageButton) dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView txtMessage = (TextView) dialogView.findViewById(R.id.txt_dialog_message);

        txtMessage.setText(message);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAlertDialog != null){
                    mAlertDialog.cancel();
                }

                //事件回调
                if (listener != null){
                    listener.onClick();
                }

                //播放音效
                MyPlayer.playTone(context,MyPlayer.INDEX_TONE_ENTER);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAlertDialog != null){
                    mAlertDialog.cancel();
                }
                //播放音效
                MyPlayer.playTone(context,MyPlayer.INDEX_TONE_CANCEL);
            }
        });

        //为dialog设置view
        builder.setView(dialogView);
        mAlertDialog = builder.create();
        //显示对话框
        mAlertDialog.show();
    }
}
