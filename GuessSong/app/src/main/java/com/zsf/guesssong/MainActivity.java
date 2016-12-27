package com.zsf.guesssong;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zsf.guesssong.data.Constant;
import com.zsf.guesssong.model.IWordButtonClickListener;
import com.zsf.guesssong.model.Song;
import com.zsf.guesssong.model.WordButton;
import com.zsf.guesssong.util.MyLog;
import com.zsf.guesssong.util.Util;
import com.zsf.guesssong.view.MyGridView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements IWordButtonClickListener {

    private Animation mPanAnim;
    private LinearInterpolator mPanPolator;
    private Animation mBarInAnim;
    private LinearInterpolator mBarInPolator;
    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutPolator;

    private ImageButton mBtnPlay;//播放按键
    private ImageView mViewPan;//盘片的View
    private ImageView mViewBar;//唱片指针的View

    private boolean mIsPlaying = false;//默认初始值为false
    private ArrayList<WordButton> mAllWords;// 文字框容器
    private MyGridView mMyGridView;

    private ArrayList<WordButton> mBtnSelectedWords;//已选框容器
    private LinearLayout mViewWordsContainer;//文字框UI容器

    private Song mCurrentSong;//当前的歌曲
    private int mCurrentStageIndex = -1;//当前关的索引

    private static final int SPASH_TIMES = 6;

    private static final String TAG = "MainActivity";
    /**
     * 答案状态
     * 1--正确
     * 2--错误
     * 3--不完整
     */
    public static final int STATUS_ANSWER_RIGHT = 1;
    public static final int STATUS_ANSWER_WRONG = 2;
    public static final int STATUS_ANSWER_LACK = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
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
        mMyGridView = (MyGridView) findViewById(R.id.grid_view);
        mViewWordsContainer = (LinearLayout) findViewById(R.id.word_select_container);

        mMyGridView.registOnWordButtonClick(this);//注册监听
    }

    /**
     * 初始化数据
     */
    private void initData() {
        initCurrentStageData();
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
        mBarInAnim.setFillAfter(true);
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
                Toast.makeText(getBaseContext(),
                        "点了播放按钮", Toast.LENGTH_SHORT).show();

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
        if (mViewBar != null) {
            if (!mIsPlaying) {
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

    private Song loadStageSongInfo(int stageIndex) {
        Song song = new Song();
        String[] stage = Constant.SONG_INFO[stageIndex];
        song.setSongFileName(stage[Constant.INDEX_FILE_NAME]);
        song.setSongName(stage[Constant.INDEX_SONG_NAME]);
        return song;
    }

    private void initCurrentStageData() {
        //读取当前关卡的歌曲信息
        mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);

        //初始化已选择框
        mBtnSelectedWords = initSelectedWord();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, 60);
        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
            mViewWordsContainer
                    .addView(mBtnSelectedWords.get(i).mViewButton, params);
        }
        //获得数据
        mAllWords = initAllWord();
        //更新数据-MyGridView
        mMyGridView.updateData(mAllWords);

    }

    /**
     * 初始化文字框
     *
     * @return
     */
    private ArrayList<WordButton> initAllWord() {
        //声明容器
        ArrayList<WordButton> data = new ArrayList<>();
        //获得所有待选文字
        String[] words = generateWords();


        for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
            WordButton button = new WordButton();
            button.mWordString = words[i];
            data.add(button);
        }
        return data;
    }

    /**
     * 初始化已选择文字
     *
     * @return
     */
    private ArrayList<WordButton> initSelectedWord() {
        ArrayList<WordButton> data = new ArrayList<>();
        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            View view = Util.getView(MainActivity.this, R.layout.item_gridview);
            final WordButton holder = new WordButton();
            holder.mViewButton = (Button) view.findViewById(R.id.btn_item);
            holder.mViewButton.setTextColor(Color.WHITE);
            holder.mViewButton.setText("");
            holder.mIsVisiable = false;

            holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
            holder.mViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearSelectWord(holder);
                }
            });
            data.add(holder);
        }
        return data;
    }


    /**
     * 重写接口内方法
     *
     * @param wordButton
     */
    @Override
    public void onWordButtonClick(WordButton wordButton) {
//        Toast.makeText(getBaseContext(),wordButton.mIndex+"",Toast.LENGTH_SHORT).show();
        setSelectWord(wordButton);
        //获得答案状态
        int checkResult = checkTheAnswer();
        //检查答案
        if (checkResult == STATUS_ANSWER_RIGHT) {
            //答案正确，过关并获得奖励

        } else if (checkResult == STATUS_ANSWER_WRONG) {
            //答案错误，闪烁文字提示用户
            sparkTheWords();

        } else if (checkResult == STATUS_ANSWER_LACK) {
            //答案不完整
            for (int i = 0; i < mBtnSelectedWords.size(); i++) {
                mBtnSelectedWords.get(i).mViewButton.setTextColor(Color.WHITE);
            }
        }

    }

    private void clearSelectWord(WordButton wordButton) {
        wordButton.mViewButton.setText("");
        wordButton.mWordString = "";
        wordButton.mIsVisiable = false;

        //设置待选框
        setButtonVisible(mAllWords.get(wordButton.mIndex), View.VISIBLE);


    }


    /**
     * 设置答案
     *
     * @param wordButton
     */
    private void setSelectWord(WordButton wordButton) {
        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
            if (mBtnSelectedWords.get(i).mWordString.length() == 0) {
                //设置第一个文字选择框内容及可见性
                mBtnSelectedWords.get(i).mViewButton.setText(wordButton.mWordString);
                //设置文字可见性
                mBtnSelectedWords.get(i).mIsVisiable = true;
                mBtnSelectedWords.get(i).mWordString = wordButton.mWordString;
                //记录索引
                mBtnSelectedWords.get(i).mIndex = wordButton.mIndex;
//                Log.d("MainActivity",getClass().getSimpleName());
                MyLog.d(TAG, mBtnSelectedWords.get(i).mWordString);

                //设置待选框可见性
                setButtonVisible(wordButton, View.INVISIBLE);
                break;
            }
        }
    }

    private void setButtonVisible(WordButton button, int visibility) {
        button.mViewButton.setVisibility(visibility);
        button.mIsVisiable = (visibility == View.VISIBLE) ? true : false;
        //
        MyLog.d(TAG, button.mIsVisiable + "");

    }

    /**
     * 生成所有的待选汉字
     *
     * @return
     */
    private String[] generateWords() {
        Random random = new Random();
        String[] words = new String[MyGridView.COUNTS_WORDS];
        //存入歌名
        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            words[i] = mCurrentSong.getNameCharacter()[i] + "";
        }

        //余下空格放入随机文字
        for (int i = mCurrentSong.getNameLength(); i < MyGridView.COUNTS_WORDS; i++) {
            words[i] = getRandomChar() + "";
        }

        //打乱文字顺序
        /**
         * 首先从所有元素中随机选取一个与第一个元素进行交换，
         * 然后在第二个之后选择一个元素与第二个交换，直到最后一个元素
         * 这样确保每个元素在每个位置的概率都是1/n
         */
        for (int i = MyGridView.COUNTS_WORDS - 1; i >= 0; i--) {
            int index = random.nextInt(i + 1);
            String buf = words[index];
            words[index] = words[i];
            words[i] = buf;
        }

        return words;
    }

    /**
     * 生成随机汉字
     *
     * @return
     */
    private char getRandomChar() {
        String str = "";
        int hightPos;
        int lowPos;

        Random random = new Random();
        hightPos = (176 + Math.abs(random.nextInt(39)));
        lowPos = (161 + Math.abs(random.nextInt(93)));
        byte[] b = new byte[2];
        b[0] = (Integer.valueOf(hightPos)).byteValue();
        b[1] = (Integer.valueOf(lowPos)).byteValue();
        try {
            str = new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str.charAt(0);
    }

    /**
     * 检查答案
     *
     * @return
     */
    private int checkTheAnswer() {
        //先检查长度
        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
            //如果有空，表示答案不完整
            if (mBtnSelectedWords.get(i).mWordString.length() == 0) {
                return STATUS_ANSWER_LACK;
            }
        }
        //答案完整,继续检查正确性
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
            sb.append(mBtnSelectedWords.get(i).mWordString);
        }
        return (sb.toString().equals(mCurrentSong.getSongName()))
                ? STATUS_ANSWER_RIGHT : STATUS_ANSWER_WRONG;
    }

    /**
     * 文字闪烁
     */
    private void sparkTheWords() {
        //定时器相关
        TimerTask task = new TimerTask() {
            boolean mChange = false;//是否改变文字颜色
            int sparkTimes = 0;//闪烁次数

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (++sparkTimes > SPASH_TIMES) {
                            return;
                        }
                        //执行闪烁逻辑，交替显示红色和白色文字
                        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
                            mBtnSelectedWords.get(i).mViewButton.setTextColor(mChange ? Color.RED :
                                    Color.WHITE);
                        }
                        mChange = !mChange;
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 1, 150);
    }
}
