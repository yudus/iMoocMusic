package com.imooc.guessmusic.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.imooc.guessmusic.R;
import com.imooc.guessmusic.data.Const;
import com.imooc.guessmusic.model.IWordButtonClickListener;
import com.imooc.guessmusic.model.Song;
import com.imooc.guessmusic.model.WordButton;
import com.imooc.guessmusic.myui.MyGridView;
import com.imooc.guessmusic.util.MyLog;
import com.imooc.guessmusic.util.Util;

public class MainActivity extends Activity 
	implements IWordButtonClickListener {

	public static final String TAG = "MainActivity";
	
	
	public static final int STATUS_ANSWER_RIGHT = 1;
	public static final int STATUS_ANSWER_WRONG = 2;
	public static final int STATUS_ANSWER_LACK = 3;
	
	public static final int SPASH_TIMES = 6;
	
	// 唱片相关动画
	private Animation mPanAnim;
	private LinearInterpolator mPanLin;
	
	private Animation mBarInAnim;
	private LinearInterpolator mBarInLin;
	
	private Animation mBarOutAnim;
	private LinearInterpolator mBarOutLin;
	
	// 唱片控件
	private ImageView mViewPan;
	// 拨杆控件
	private ImageView mViewPanBar;
	
	// Play 按键事件
	private ImageButton mBtnPlayStart;
	
	//过关界面
	private View mPassView;
	
	// 当前动画是否正在运行
	private boolean mIsRunning = false;
	
	// 文字框容器
	private ArrayList<WordButton> mAllWords;
	
	private ArrayList<WordButton> mBtnSelectWords;
	
	private MyGridView mMyGridView;
	
	// 已选择文字框UI容器
	private LinearLayout mViewWordsContainer;
	
	// 当前的歌曲
	private Song mCurrentSong;
	
	// 当前关的索引
	private int mCurrentStageIndex = -1;
	
	//当前金币数量
	private int mCurrentCoins = Const.TOTAL_COINS;
	
	//金币view
	private TextView mViewCurrentCoins;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// 初始化控件
		mViewPan = (ImageView)findViewById(R.id.imageView1);
		mViewPanBar = (ImageView)findViewById(R.id.imageView2);
		
		mMyGridView = (MyGridView)findViewById(R.id.gridview);
		
		mViewCurrentCoins = (TextView)findViewById(R.id.txt_bar_coins);
		mViewCurrentCoins.setText(mCurrentCoins + "");
		
		
		// 注册监听
		mMyGridView.registOnWordButtonClick(this);
		
		mViewWordsContainer = (LinearLayout)findViewById(R.id.word_select_container);
		
		// 初始化动画
		mPanAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
		mPanLin = new LinearInterpolator();
		mPanAnim.setInterpolator(mPanLin);
		mPanAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	// 开启拨杆退出动画
            	mViewPanBar.setAnimation(mBarOutAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
		
		mBarInAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_45);
		mBarInLin = new LinearInterpolator();
		mBarInAnim.setFillAfter(true);
		mBarInAnim.setInterpolator(mBarInLin);
		mBarInAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	// 开始唱片动画
            	mViewPan.startAnimation(mPanAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
		
		mBarOutAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_d_45);
		mBarOutLin = new LinearInterpolator();
		mBarOutAnim.setFillAfter(true);
		mBarOutAnim.setInterpolator(mBarOutLin);
		mBarOutAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	// 整套动画播放完毕
            	mIsRunning = false;
            	mBtnPlayStart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
		
		mBtnPlayStart = (ImageButton)findViewById(R.id.btn_play_start);
		mBtnPlayStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				handlePlayButton();
			}
		});
		
		// 初始化游戏数据
		initCurrentStageData();
		handleDeleteWord();
		handleTipAnswer();
	}
	
	@Override
	public void onWordButtonClick(WordButton wordButton) {
//		Toast.makeText(this, wordButton.mIndex + "", Toast.LENGTH_SHORT).show();
		setSelectWord( wordButton );
		
		//获得答案状态
		int checkResult = checkTheAnswer();
		
		//检查答案
		if( checkResult == STATUS_ANSWER_RIGHT ){
			//获得相应奖励，过关
			handlePassEvent();
			
		} else if ( checkResult == STATUS_ANSWER_WRONG ){
			//进行错误提示：闪烁文字提示用户
			sparkTheWords();
			
		} else if ( checkResult == STATUS_ANSWER_LACK ){
			//答案缺失
			for( int i = 0; i < mBtnSelectWords.size(); ++i ){
				mBtnSelectWords.get(i).mViewButton.setTextColor(Color.WHITE);
			}
			
		}
	}
	
	/**
	 * 处理过关事件
	 */
	private void handlePassEvent(){
		mPassView = (LinearLayout)this.findViewById(R.id.pass_view);
		mPassView.setVisibility(View.VISIBLE);
		
	}
	
	private void clearTheAnswer( WordButton wordButton ){
		wordButton.mViewButton.setText("");
		wordButton.mWordString = "";
		wordButton.mIsVisible = false;
		
		//设置待选框的可见性
		setButtonVisiable( mAllWords.get(wordButton.mIndex), View.VISIBLE );
	}
	
	/**
	 * 设置待选文字框是否可见
	 * @param button
	 * @param visibility
	 */
	private void setButtonVisiable( WordButton button, int visibility ){
		button.mViewButton.setVisibility( visibility );
		button.mIsVisible = (visibility == View.VISIBLE) ? true : false;
		
		//Log
		MyLog.d(TAG,button.mIsVisible+"");
	}
	
	
	
	/**
	 * 设置答案
	 * @param wordButton
	 */
	private void setSelectWord( WordButton wordButton ){
		for( int i = 0; i < mBtnSelectWords.size(); ++i ){
			if( mBtnSelectWords.get(i).mWordString.length() == 0 ){
				//设置答案文字框内容及可见性
				mBtnSelectWords.get( i ).mViewButton.setText( wordButton.mWordString);
				mBtnSelectWords.get( i ).mIsVisible = true;
				mBtnSelectWords.get(i).mWordString = wordButton.mWordString;
				
				//记录索引
				mBtnSelectWords.get(i).mIndex = wordButton.mIndex;
				
				//Log....
				MyLog.d(TAG, mBtnSelectWords.get(i).mIndex+"");
				//设置带选框可见性
				setButtonVisiable( wordButton, View.INVISIBLE);
				break;
			}
		}
	}
	
    /**
     * 处理圆盘中间的播放按钮，就是开始播放音乐
     */
	private void handlePlayButton() {
		if (mViewPanBar != null) {
			if (!mIsRunning) {
				mIsRunning = true;
				
				// 开始拨杆进入动画
				mViewPanBar.startAnimation(mBarInAnim);
				mBtnPlayStart.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	@Override
    public void onPause() {
        mViewPan.clearAnimation();
        
        super.onPause();
    }
	
	private Song loadStageSongInfo(int stageIndex) {
		Song song = new Song();
		
		String[] stage = Const.SONG_INFO[stageIndex];
		song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
		song.setSongName(stage[Const.INDEX_SONG_NAME]);
		
		return song;
	}
	
	private void initCurrentStageData() {
		// 读取当前关的歌曲信息
		mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);
		// 初始化已选择框
		mBtnSelectWords = initWordSelect();
		
		LayoutParams params = new LayoutParams(140, 140);
		
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			mViewWordsContainer.addView(
					mBtnSelectWords.get(i).mViewButton,
					params);
		}
		
		// 获得数据
		mAllWords = initAllWord();
		// 更新数据- MyGridView
		mMyGridView.updateData(mAllWords);
	}
	
	/**
	 * 初始化待选文字框
	 */
	private ArrayList<WordButton> initAllWord() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();
		
		// 获得所有待选文字
	    String[] words = generateWords();
		
		for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
			WordButton button = new WordButton();
			
			button.mWordString = words[i];
			
			data.add(button);
		}
		
		return data;
	}
	
	/**
	 * 初始化已选择文字框
	 * 
	 * @return
	 */
	private ArrayList<WordButton> initWordSelect() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();
		
		for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
			View view = Util.getView(MainActivity.this, R.layout.self_ui_gridview_item);
			
			final WordButton holder = new WordButton();
			
			holder.mViewButton = (Button)view.findViewById(R.id.item_btn);
			holder.mViewButton.setTextColor(Color.WHITE);
			holder.mViewButton.setText("");
			holder.mIsVisible = false;
			
			holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
			
			holder.mViewButton.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					clearTheAnswer( holder );
				}
			});
			
			data.add(holder);
		}
		
		return data;
	}
	
	//生成所有文字
	private String[] generateWords(){
		
		Random random = new Random();
		
		String[] words = new String[MyGridView.COUNTS_WORDS];
		
		//存入歌名
		for( int i = 0; i < mCurrentSong.getNameLength(); ++i ){
			words[i] = mCurrentSong.getNameCharacters()[i] + "";
			
		}
		
		//获取随机文字，存到数组当中，做for循环
		for( int i =  mCurrentSong.getNameLength(); i < MyGridView.COUNTS_WORDS; ++i ){
			words[i] = getRandomChar() + "";
		}
		
		//打乱顺序
		for( int i = MyGridView.COUNTS_WORDS-1; i >= 0; i-- ){
			int index = random.nextInt( i+1 );
			//交换
			String buf = words[index];
			words[index] = words[i];
			words[i] = buf;
			
		}
		
		return words;
	}
	
	
	//生成随机汉字
	private char getRandomChar(){
		String str = "";
		int hightPos;
		int lowPos;
		
		Random random = new Random();
		
		hightPos = (176 + Math.abs( random.nextInt(39) ) );
		lowPos = ( 161 + Math.abs( random.nextInt(39 )));
		
		byte[] b = new byte[2];
		b[0] = Integer.valueOf(hightPos).byteValue();
		b[1] = Integer.valueOf(lowPos).byteValue();
		
		try {
			str = new String( b, "GBK" );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return str.charAt(0);
		
	}
	
	private int checkTheAnswer(){
		for( int i = 0; i < mBtnSelectWords.size(); ++i ){
			//如果有空的说明答案还不完整
			if( mBtnSelectWords.get(i).mWordString.length() == 0 ){
				return STATUS_ANSWER_LACK;
			}
		}
		
		//答案完整，继续检查正确性
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < mBtnSelectWords.size(); ++i ){
			sb.append( mBtnSelectWords.get(i).mWordString );
		}
		
		return (sb.toString().equals(mCurrentSong.getmSongName())) ? 
				STATUS_ANSWER_RIGHT : STATUS_ANSWER_WRONG;
	}
	
	/**
	 * 文字闪烁
	 */
	private void sparkTheWords(){
		//定时器相关
		TimerTask task = new TimerTask(){

			boolean mChange = false;
			int mSpardTimes = 0;
			
			@Override
			public void run() {
				runOnUiThread( new Runnable(){
					public void run(){
						//显示闪烁的次数
						if( ++mSpardTimes > SPASH_TIMES){
							return;
						}
						
						//执行闪烁逻辑：交替显示红色和白色文字
						for( int i = 0; i < mBtnSelectWords.size(); ++i ){
							mBtnSelectWords.get(i).mViewButton.setTextColor(mChange ? Color.RED : Color.WHITE);
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
	 * 自动选择一个答案
	 */
	private void tipAnswer(){
		boolean tipWord = false;
		for( int i = 0; i < mBtnSelectWords.size(); ++i ){
			if( mBtnSelectWords.get(i).mWordString.length() == 0 ){
				//根据当前答案框条件选择对应的文字并填入
				onWordButtonClick(findIsAnswerWord(i));
				tipWord = true;
				//减少金币数量
				if( handleCoins(-getTipCoins()) ){
					//当金币数量不够的时候，显示对话框
					return;
				}
				break;
			}
		}
		
		//没有找到可以填充的答案
		if( !tipWord ){
			//闪烁文字提示用户
			sparkTheWords();
		}
	}
	

	
	/**
	 * 删除文字
	 */
	private void deleteOneWord(){
		//减少金币
		if( !handleCoins(-getDeleteWordCoins())){
			//金币不够，显示对话框
			return;
		}
		
		//将这个索引对应的WordButton设置为不可见
		setButtonVisiable( findNotAnswerWord(), View.INVISIBLE);
		
	}
	
	/**
	 * 找到一个不是答案的文字，并且是当前可见的
	 * @return
	 */
	private WordButton findNotAnswerWord(){
		Random random = new Random();
		WordButton buf = null;
		while( true ){
			int index = random.nextInt(MyGridView.COUNTS_WORDS);
			
			buf = mAllWords.get(index);
			
			if( buf.mIsVisible && !isTheAnswerWord(buf) ){
				return buf;
			}
		}
	}
	 
	/**
	 * 找到一个答案文字
	 * @param index 当前需要填入答案框的索引
	 * @return
	 */
	private WordButton findIsAnswerWord( int index ){
		WordButton buf = null;
		
		for( int i = 0; i < MyGridView.COUNTS_WORDS; ++i ){
			buf  = mAllWords.get(i);
			if( buf.mWordString.equals(""+mCurrentSong.getNameCharacters()[index])){
				return buf;
			}
		}
		
		return null;
	}
	
	/**
	 * 判断某个文字是否为答案
	 * 
	 * @param word
	 * @return
	 */
	private boolean isTheAnswerWord( WordButton word ){
		boolean result = false;
		for( int i = 0; i < mCurrentSong.getNameLength(); ++i ){
			if( word.mWordString.equals(mCurrentSong.getNameCharacters()[i])){
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 增加或减少指定金币数量
	 * @param data 正的代表增加，负的代表减少
	 * @return true	增加/减少成功，false是失败
	 */
	private boolean handleCoins( int data ){
		//判断当前总的金币数量是否可被减少
		if( mCurrentCoins + data >= 0 ){	//代表可以减少
			mCurrentCoins += data;
			//更新右上角的coins
			mViewCurrentCoins.setText( mCurrentCoins+"" );
			return true;
		}else{	//金币不够时
			
			return false;
		}
	}
	
	private int getDeleteWordCoins(){
		return this.getResources().getInteger(R.integer.pay_delete_word);
	}
	
	private int getTipCoins(){
		return this.getResources().getInteger(R.integer.pay_tip_answer);
	}
	
	/**
	 * 处理删除待选文字事件
	 */
	private void handleDeleteWord(){
		ImageButton button = (ImageButton)findViewById(R.id.btn_delete_word);
		
		button.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				deleteOneWord();
			}
		});
	}
	
	/**
	 * 处理提示事件
	 */
	private void handleTipAnswer(){
		ImageButton button = (ImageButton)findViewById(R.id.btn_tip_answer);
		
		button.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tipAnswer();
				
			}
		});
	}
}
