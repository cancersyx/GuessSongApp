package com.zsf.guesssong.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.zsf.guesssong.R;
import com.zsf.guesssong.model.IWordButtonClickListener;
import com.zsf.guesssong.model.WordButton;
import com.zsf.guesssong.util.Util;

import java.util.ArrayList;

/**
 *
 */
public class MyGridView extends GridView {

    private ArrayList<WordButton> mArrayList = new ArrayList<>();
    private MyGridAdapter mAdapter;
    private Context mContext;
    public final static int COUNTS_WORDS = 24;
    private Animation mScaleAnimation;
    private IWordButtonClickListener mWordButtonClickListener;


    public MyGridView(Context context) {
        super(context);
    }

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAdapter = new MyGridAdapter();
        this.setAdapter(mAdapter);//
        initView();
    }

    public MyGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView() {


    }


    public void updateData(ArrayList<WordButton> list) {
        mArrayList = list;
        setAdapter(mAdapter);//刷新
    }


    private class MyGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final WordButton holder;

            if (convertView == null) {
                convertView = Util.getView(mContext, R.layout.item_gridview);
                holder = mArrayList.get(position);
                //加载动画
                mScaleAnimation = AnimationUtils.loadAnimation(mContext, R.anim.scale);
                //设置动画延迟时间
                mScaleAnimation.setStartOffset(position * 100);
                holder.mIndex = position;
                holder.mViewButton = (Button) convertView.findViewById(R.id.btn_item);
                holder.mViewButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mWordButtonClickListener.onWordButtonClick(holder);
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (WordButton) convertView.getTag();
            }
            holder.mViewButton.setText(holder.mWordString);
            //动画的播放
            convertView.startAnimation(mScaleAnimation);
            return convertView;
        }
    }

    /**
     * 注册监听接口
     * @param listener
     */
    public void registOnWordButtonClick(IWordButtonClickListener listener){
        mWordButtonClickListener = listener;
    }

}
