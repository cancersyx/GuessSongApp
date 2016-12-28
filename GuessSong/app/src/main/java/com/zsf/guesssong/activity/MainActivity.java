package com.zsf.guesssong.activity;

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
import android.widget.TextView;
import android.widget.Toast;

import com.zsf.guesssong.R;
import com.zsf.guesssong.data.Constant;
import com.zsf.guesssong.model.IAlertDialogButtonListener;
import com.zsf.guesssong.model.IWordButtonClickListener;
import com.zsf.guesssong.model.Song;
import com.zsf.guesssong.model.WordButton;
import com.zsf.guesssong.util.MyLog;
import com.zsf.guesssong.util.MyPlayer;
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

    private View mPassView;//过关界面

    //当前金币的数量
    private int mCurrentCoins = Constant.TOTAL_COINS;
    //金币View
    private TextView mViewCurrentCoins;

    private TextView mCurrentStagePassView;//当前关的索引
    private TextView mCurrentSongNamePassView;

    private TextView mCurrentStageView;//关索引，不是过关的那个

    private static final int ID_DIALOG_DELETE_WORD = 1;
    private static final int ID_DIALOG_TIP_ANSWER = 2;
    private static final int ID_DIALOG_LACK_COINS = 3;


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
        mViewCurrentCoins = (TextView) findViewById(R.id.txt_bar_icons);

        mMyGridView.registOnWordButtonClick(this);//注册监听
    }

    /**
     * 初始化数据
     */
    private void initData() {
        initCurrentStageData();
        mViewCurrentCoins.setText(mCurrentCoins + "");

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

        //处理去掉一个错误答案事件
        handleDeleteEvent();
        //处理提示事件
        handleTipEvent();

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

                //开始播放音乐
                MyPlayer.playSong(MainActivity.this,mCurrentSong.getSongFileName());
            }
        }


    }

    /**
     * 暂停处理
     */
    @Override
    protected void onPause() {
        mViewPan.clearAnimation();

        //停止播放音乐
        MyPlayer.stopSong(MainActivity.this);
        super.onPause();
    }

    private Song loadStageSongInfo(int stageIndex) {
        Song song = new Song();
        String[] stage = Constant.SONG_INFO[stageIndex];
        song.setSongFileName(stage[Constant.INDEX_FILE_NAME]);
        song.setSongName(stage[Constant.INDEX_SONG_NAME]);
        return song;
    }

    /**
     * 加载当前关的数据
     */
    private void initCurrentStageData() {
        //读取当前关卡的歌曲信息
        mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);

        //初始化已选择框
        mBtnSelectedWords = initSelectedWord();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, 60);
        //清空原来的答案
        mViewWordsContainer.removeAllViews();
        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
            mViewWordsContainer
                    .addView(mBtnSelectedWords.get(i).mViewButton, params);
        }
        //当前关的索引
        mCurrentStageView = (TextView) findViewById(R.id.txt_current_stage);
        if (mCurrentStageView != null) {
            mCurrentStageView.setText((mCurrentStageIndex + 1) + "");
        }


        //获得数据
        mAllWords = initAllWord();
        //更新数据-MyGridView
        mMyGridView.updateData(mAllWords);

        //进入就播放音乐
       handlePlayButton();

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
            handlePassEvent();
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

    /**
     * 处理过关界面及事件
     */
    private void handlePassEvent() {
        //显示过关界面
        mPassView = this.findViewById(R.id.pass_view);
        mPassView.setVisibility(View.VISIBLE);
        //停止未完成的动画
        mViewPan.clearAnimation();

        //停止正在播放的音乐
        MyPlayer.stopSong(MainActivity.this);

        //播放音效
        MyPlayer.playTone(MainActivity.this,MyPlayer.INDEX_TONE_COIN);

        //当前关的索引
        mCurrentStagePassView = (TextView) findViewById(R.id.text_current_stage_pass);
        if (mCurrentStagePassView != null) {
            mCurrentStagePassView.setText((mCurrentStageIndex + 1) + "");
        }

        //显示歌曲名称
        mCurrentSongNamePassView = (TextView) findViewById(R.id.text_current_song_name_pass);
        if (mCurrentSongNamePassView != null) {
            mCurrentSongNamePassView.setText(mCurrentSong.getSongName());
        }

        //下一关按键处理
        ImageButton btnPass = (ImageButton) findViewById(R.id.btn_next);
        btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (judgeAppPassed()) {
                    //进入到通关界面
                    Util.startActivity(MainActivity.this, AllPassActivity.class);
                } else {
                    //开始新一关
                    mPassView.setVisibility(View.GONE);

                    //加载关卡数据
                    initCurrentStageData();
                }
            }
        });

    }

    /**
     * 判断是否通关
     *
     * @return
     */
    private boolean judgeAppPassed() {
        return mCurrentStageIndex == Constant.SONG_INFO.length - 1;
    }

    /**
     * 自动选择一个答案
     */
    private void tipAnswer() {
        boolean tipWord = false;
        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
            if (mBtnSelectedWords.get(i).mWordString.length() == 0) {
                //根据当前的答案框条件选择对应的文字填入
                onWordButtonClick(findAnswerWord(i));
                tipWord = true;
                //减少金币数量
                if (!handleCoins(-getTipAnwserCoins())) {
                    //金币数量不够，弹出对话框
                    showConfirmDialog(ID_DIALOG_LACK_COINS);
                    return;
                }
                break;
            }
        }

        //没有找到可以填充的答案
        if (!tipWord) {
            //闪烁文字提示用户
            sparkTheWords();
        }


    }


    /**
     * 删除文字
     */
    private void deleteOneWord() {
        //减少金币
        if (!handleCoins(-getDeleteWordCoins())) {
            //金币不够，显示提示对话框
            return;
        }

        //索引对应的wordbutton设置为不可见
        setButtonVisible(findNotAnswerWord(), View.INVISIBLE);
    }

    /**
     * 找到一个不是答案的文件，并且当前是可见的
     *
     * @return
     */
    private WordButton findNotAnswerWord() {
        Random random = new Random();
        WordButton buf = null;
        while (true) {
            int index = random.nextInt(MyGridView.COUNTS_WORDS);
            buf = mAllWords.get(index);
            //判断取出来的是否为我们的正确答案
            if (buf.mIsVisiable && !isTheAnswerWord(buf)) {
                return buf;
            }
        }
    }

    /**
     * 找到答案的一个字
     *
     * @return
     */
    private WordButton findAnswerWord(int index) {
        WordButton buf = null;
        for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
            buf = mAllWords.get(i);
            if (buf.mWordString.equals("" + mCurrentSong.getNameCharacter()[index])) {
                return buf;
            }
        }
        return null;
    }

    /**
     * 判断某个文字是否为答案
     *
     * @param wordButton
     * @return
     */
    private boolean isTheAnswerWord(WordButton wordButton) {
        boolean result = false;
        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            if (wordButton.mWordString.equals("" + mCurrentSong.getNameCharacter()[i])) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 增加或减少指定数量的金币
     *
     * @param data
     * @return true 增加/减少成功，false 失败
     */
    private boolean handleCoins(int data) {
        //判断当前金币是否可以减少
        if (mCurrentCoins + data >= 0) {
            mCurrentCoins += data;
            mViewCurrentCoins.setText(mCurrentCoins + "");
            return true;
        } else {
            //金币不够
            return false;
        }
    }

    /**
     * 从配置文件中读取删除操作需要的金币
     *
     * @return
     */
    private int getDeleteWordCoins() {
        return this.getResources().getInteger(R.integer.pay_delete_word);
    }

    /**
     * 从配置文件中读取提示需要耗费的金币
     *
     * @return
     */
    private int getTipAnwserCoins() {
        return this.getResources().getInteger(R.integer.pay_tip_answer);
    }

    /**
     * 处理删除待选文字事件
     */
    private void handleDeleteEvent() {
        ImageButton button = (ImageButton) findViewById(R.id.btn_delete_word);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog(ID_DIALOG_DELETE_WORD);
            }
        });
    }

    /**
     * 处理提示按键事件
     */
    private void handleTipEvent() {
        ImageButton button = (ImageButton) findViewById(R.id.btn_tip_word);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tipAnswer();
                showConfirmDialog(ID_DIALOG_TIP_ANSWER);
            }
        });
    }


    //自定义AlertDialog事件响应
    //删除错误答案
    private IAlertDialogButtonListener mBtnDeleteWordListener = new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            deleteOneWord();

        }
    };

    //答案提示
    private IAlertDialogButtonListener mBtnOkTipAnswerListener = new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            tipAnswer();
        }
    };

    //金币不足
    private IAlertDialogButtonListener mBtnOkLackCoinsListener = new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件

        }
    };

    /**
     * 显示对话框
     *
     * @param id
     */
    private void showConfirmDialog(int id) {
        switch (id) {
            case ID_DIALOG_DELETE_WORD:
                Util.showDialog(MainActivity.this, "确认花掉" + getDeleteWordCoins() + "个金币去掉一个错误答案", mBtnDeleteWordListener);
                break;
            case ID_DIALOG_TIP_ANSWER:
                Util.showDialog(MainActivity.this, "确认花掉" + getTipAnwserCoins() + "个金币获得文字提示？", mBtnOkTipAnswerListener);

                break;
            case ID_DIALOG_LACK_COINS:
                Util.showDialog(MainActivity.this, "金币不足，去商店补充吧！", mBtnOkLackCoinsListener);
                break;
        }

    }

}
