package com.sangsang.beyondtportal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sangsang.Info.dataInfo;
import com.sangsang.Info.pageInfo;
import com.sangsang.dataManager.DbAdapter;
import com.sangsang.dataManager.FileManager;
import com.sangsang.dataManager.ViewPagerAdapter;
import com.sangsang.utils.CustomViewPager;
import com.sangsang.utils.UIUdateUtils;
import com.sangsang.utils.ViewFragment;

public class contentsActivity extends Activity {

	private WebView currentWebview;
	private TextView currentTextView;
	private FileManager fileManager;
	private DbAdapter dbAdapter;

	private ArrayList<dataInfo> dataList;
	private SharedPreferences prefs;
	SharedPreferences.Editor editor;

	private RelativeLayout upItemContainer;
	private LinearLayout downItemContainer;

	private boolean isItemShowUp = false;
	private boolean isItemShowDown = false;

	private boolean isDetail = false;
	private boolean isNext = true;

	private int currentId;

	private int currentPosition;
	private AlertDialog alert;
	private AlertDialog commonDialog;

	private Button btn_upMenu;
	private Button btn_downMenu;

	private Button btn_exam;

	private Button btn_pre;
	private Button btn_next;

	private TextView currentNo;
	private TextView currentTotal;

	private TextView currentRank;

	private TextView currentTime;
	private TextView currentTimeBottom;
	int timeValue = 0;

	private boolean isUseDiaog = false;

	private SoundPool sound_pool;
	private int sound_beep;
	private int sound_beep1;

	private int hWidth;
	private int hHeight;

	private final Handler handler = new Handler();

	private CustomViewPager mWebViewViewPager; // 웹뷰 swipe 용도
	private ViewPagerAdapter mViewPagerAdapter;
	private float mViewPagerX;
	float touchX;// 뷰페이저 터치 시작 x 좌표
	private AlertDialog nextDialog;
	private ViewPagerAdapter pagerAdapter;

	protected boolean _active = true;

	private int count = 0;
	ProgressDialog progressDialog;
	private float mLastMoveX = 0;
	private String nId;// 단원이나 모의고사 몇회인지...]

	private int positionA;

	private Bundle extra;
	private Intent intentCurrent;
	private String StoryNumber = "";

	private AlertDialog lastAlert;
	private Button menuBottomBtn;



	private Boolean isPreStroy;
	private Boolean isNextStroy;



	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.contents);

		Intent intent = getIntent();
		dbAdapter = new DbAdapter(this);
		fileManager = new FileManager(this);

		currentPosition = intent.getExtras().getInt("position");
		StoryNumber = intent.getExtras().getString("StoryLevel");


		dataList = dbAdapter.getPageList("STORY", StoryNumber);


		menuBottomBtn = (Button) findViewById(R.id.menu_bottom_btn);
		menuBottomBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onEnd(null);
			}

		});

		initQuizResultDialog();
		initLastResultDialog();
		initDetailDialog();

		currentId = intent.getExtras().getInt("id");

		currentNo = (TextView) findViewById(R.id.current_no);
		currentTotal = (TextView) findViewById(R.id.current_total);
		currentTime = (TextView) findViewById(R.id.current_time);
		upItemContainer = (RelativeLayout) findViewById(R.id.upitem_container);

		mWebViewViewPager = (CustomViewPager) findViewById(R.id.contents_webview_viewpager);
		// mViewPagerAdapter = new ViewPagerAdapter(dataList, this);
		// mWebViewViewPager.setAdapter(mViewPagerAdapter);
		mWebViewViewPager.setOnPageChangeListener(mPageChangerListener);
		mWebViewViewPager.setOnTouchListener(listener);
		mWebViewViewPager.setPadding(0, 0, 0, 0);

		pagerAdapter = new ViewPagerAdapter(dataList, this, handlerForWebview,
				currentId);

		mWebViewViewPager.setAdapter(pagerAdapter);
		mWebViewViewPager.setVerticalScrollBarEnabled(false);
		count = dataList.size();

		currentPosition = intent.getExtras().getInt("position");
		// 이어서 풀기

		prefs = getSharedPreferences(
				PortalActivity.KEY_PREFERENCE_USERINFO, MODE_PRIVATE);
		editor = prefs.edit();


		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			editor.putString("orientation", "vertical");
		} else {
			editor.putString("orientation", "landscape");
		}

		editor.commit();

		mWebViewViewPager.setCurrentItem(currentPosition);
		// mWebViewViewPager.setCurrentItem(46);

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();


		mHandler.sendEmptyMessage(0);

//		currentNo.setText(String.format("%03d",
//				Integer.parseInt(intent.getExtras().getString("Current"))));



		if (!(dataList.size() == 0)) {

			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Progress Dialog");
			progressDialog.setIcon(android.R.drawable.ic_dialog_info);
			progressDialog.setMessage("잠시만 기다려 주세요.");
	//		progressDialog.show();

			pageInfo qInfo = (pageInfo) dataList.get(currentPosition);
			addCardItem(qInfo);

			String imagePath = "";
			File sdcard = Environment.getExternalStorageDirectory();
			File iamgespath = new File(sdcard.getAbsolutePath()
					+ File.separator + "historytest" + File.separator
					+ "images");
			if (!iamgespath.exists()) {
				iamgespath.mkdirs();
			}
			dbAdapter.close();

		}

		currentTotal.setText(String.format("%03d", dataList.size()));


		isPreStroy = false;
		isNextStroy = false;






		backPressCloseHandler = new BackPressCloseHandler(this);



	}





	private Handler handlerForWebview = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			switch (msg.what) {
			case ViewFragment.ANSWER_CHECK:
				// checkAnswer(String.valueOf(msg.obj));
				break;
			case ViewFragment.LOAD_COMPLETE:
				if (progressDialog.isShowing())
					progressDialog.dismiss();
				break;
			}
		}
	};



	private boolean isMenuVisible = false;

	private OnTouchListener listener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			int action = event.getAction();
			switch (action & MotionEventCompat.ACTION_MASK) {
			case MotionEvent.ACTION_MOVE:
				mLastMoveX = event.getXPrecision();
				break;
			}
			return false;
		}
	};

	// 1초마다 시간 갱신해서 표시하는 핸들러
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

			timeValue++;
			int time = timeValue;

			String hour = "0";
			String minute = "00";
			String second = "00";

			int hourInt = 0;
			int minuteInt = 0;

			if (time >= 3600) {
				hourInt = time / 3600;
				time = time - hourInt * 3600;
			} else if (time > 60) {
				minuteInt = time / 60;
				time = time - minuteInt * 60;

				if (time == 60) {
					hourInt++;
					time = 0;
				}
			}

			int secondInt = time;

			if (secondInt < 10) {
				second = "0" + secondInt;
			} else if (secondInt == 60) {
				second = "00";
				minuteInt++;
			} else {
				second = "" + secondInt;
			}

			hour = "" + hourInt;

			if (minuteInt < 10) {
				minute = "0" + minuteInt;
			} else if (minuteInt == 60) {
				minute = "00";
			} else {
				minute = "" + minuteInt;
			}

			String timeString = hour + ":" + minute + ":" + second;

			currentTime.setText("" + timeString);

			mHandler.sendEmptyMessageDelayed(0, 1000);
		}
	};

	public void onPre(int position) {
		currentPosition = mWebViewViewPager.getCurrentItem();

		if (position == -1 && swipeStartPosition == 0
				&& !directionHistory.containsKey("left")) {
			sendTouchUpAction(mWebViewViewPager);
			goPreStory();

			editor.putBoolean("isNext", true);
			editor.putBoolean("isPre", true);
			editor.commit();


		} else {
			// dataList.get(currentPosition);
		}

		Log.i("test", "onPre : " + currentPosition);

	//	showCorrectAnswer(currentPosition);
	}

	private int swipeStartPosition = 0;

	@SuppressLint("NewApi")
	public void onNext(int position, boolean moveNextPage) {

		int currPosition = 0;
		if (position <= 0)
			currPosition = 0;
		else
			currPosition = position - 1;

		pageInfo qCurrentInfo = (pageInfo) dataList.get(currPosition);

		// currentPosition++;
		currentPosition = mWebViewViewPager.getCurrentItem();

		// 마지막 문제인지 확인
		if ((!moveNextPage && swipeStartPosition + 1 == count && currentPosition + 1 == count)
				&& !directionHistory.containsKey("right")) {
			pageInfo info = (pageInfo) dataList.get(mWebViewViewPager
					.getCurrentItem());

			sendTouchUpAction(mWebViewViewPager);
			currentPosition = count - 1;

			goNextStory();
			editor.putBoolean("isNext", true);
			editor.putBoolean("isPre", true);
			editor.commit();


		} else {
			// 다음 문제로 이동

			pageInfo qInfo = (pageInfo) dataList.get(currentPosition + 1);

			// 페이지 변경

			positionA = position;

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					mWebViewViewPager.setCurrentItem(positionA, true);
				}
			}, 500);

		}

		Log.i("test", "onNext : " + currentPosition);
	}

	private void sendTouchUpAction(ViewPager target) {

		try {
			long downTime = SystemClock.uptimeMillis();
			long eventTime = SystemClock.uptimeMillis() + 100;
			MotionEvent up_event = MotionEvent.obtain(downTime, eventTime,
					MotionEvent.ACTION_UP, 0, 0, 0);

			target.dispatchTouchEvent(up_event);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			if (!mWebViewViewPager.beginFakeDrag())
				mWebViewViewPager.beginFakeDrag();

			if (mWebViewViewPager.beginFakeDrag())
				mWebViewViewPager.endFakeDrag();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private int moveNextPage() {
		int movePageNo = mWebViewViewPager.getCurrentItem() + 1;
		if (count <= movePageNo)
			return count;
		else
			return movePageNo;
	}

	private void showCorrectAnswer(int position) {// , String pre_selection) {
		// //이은정 수정
		pageInfo info = (pageInfo) dataList.get(position);

		int nCnt = position + 1;
		if (position == 0)
			nCnt = 1;
		else
			nCnt = 2;

		// 정답 표시 ViewPagerAdapter에서도 표시함.
		// 이어 풀기를 할 때 처음 보이는 문제와 이전 문제에 정답 표시
		for (int i = position; i > position - nCnt; i--) {
			if (position <= mWebViewViewPager.getCurrentItem() + 1) {
				RelativeLayout v = (RelativeLayout) mWebViewViewPager
						.findViewWithTag(i);
				if (v == null)
					return;
				WebView web = (WebView) v.getChildAt(0);
				info = (pageInfo) dataList.get(i);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		playUpMenuHide();
	}

	private void moveResultActivity(int type1, String type2) {

	}

	private final int UNIT_ALL_SOVLED_LAST = 0; // 단원 모든 문제를 풀고 난 후 다음 문제를 보려는
												// 경우
	private final int UNIT_ALL_NOT_SOVLED_LAST = 1; // 단원 마지막 문제를 풀지 않고 다음 문제를
													// 보려는 경우
	private final int UNIT_LAST_SOLVED_LAST = 2; // 단원 마지막 문제를 푼 경우
	private final int FIRST = 3;
	private final int ANSWER_CHECK_AFTER = 4;
	private final int MOCK_INCORRECT_LAST_SOLVED = 5;

	private final int MOCK_LAST_SOLVED = 6;

	private boolean isCorrect = false;

	private void initDetailDialog() {
		// dialog
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(
				contentsActivity.this);
		alt_bld.setMessage("퀴즈를 풀어야 보실 수 있습니다.").setCancelable(false)
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
	}

	private void initQuizResultDialog() {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(
				contentsActivity.this);
		alt_bld.setCancelable(false)
				.setPositiveButton("다음문제",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								if (currentPosition < Integer
										.valueOf((String) currentTotal
												.getText())) {

									onNext(moveNextPage(), true);
								} else {
									commonDialog.setMessage("문제를 다 풀었습니다.");
									commonDialog.show();
								}
							}
						})
				.setNeutralButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.setNegativeButton("해설보기",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		alert = alt_bld.create();
	}

	private void initLastResultDialog() {

		AlertDialog.Builder alt_bld2 = new AlertDialog.Builder(
				contentsActivity.this);

		int num = Integer.parseInt(StoryNumber);

		if (num == 6) {
			alt_bld2.setCancelable(false).setPositiveButton("마지막 이야기 입니다.",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							finish();

						}
					});
		} else {
			alt_bld2.setCancelable(false)
					.setPositiveButton("그만 보기",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									dialog.dismiss();
									finish();

								}
							})
					.setNegativeButton("다음 이야기",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									goNextStory();
									dialog.cancel();
								}
							});
		}
		lastAlert = alt_bld2.create();
	}

	private void goNextStory() {

		int num = Integer.parseInt(StoryNumber);

		Intent BstartA = new Intent();
		BstartA.putExtra("data", num);
		BstartA.putExtra("direction", 1);

		if (num == 6) {
			this.finish();
		} else {
			this.setResult(RESULT_OK, BstartA);
			this.finish();
		}

	}

	private void goPreStory() {
		int num = Integer.parseInt(StoryNumber);

		Intent BstartA = new Intent();
		BstartA.putExtra("data", num);
		BstartA.putExtra("direction", 0);

		if (num == 1) {
			this.finish();
		} else {
			this.setResult(RESULT_OK, BstartA);
			this.finish();
		}

	}

	public void setExImagesVisibie(String gubun, int index) {
	}



	public void playUpMenuShow() {
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		int px = 60 * outMetrics.densityDpi / 160;

		Animation animation = new TranslateAnimation(0, 0, 0, 0);
		animation.setDuration(500);
		animation.setFillAfter(true);
		upItemContainer.startAnimation(animation);

		isItemShowUp = true;
	}

	public void playUpMenuHide() {

		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		int px = 60 * outMetrics.densityDpi / 160;

		Animation animation = new TranslateAnimation(0, 0, 0, -px);
		animation.setDuration(500);
		animation.setFillAfter(true);
		upItemContainer.startAnimation(animation);

		isItemShowUp = false;
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void addCardItem(pageInfo qInfo) {
		/*
		 * if (currentId == 0) {
		 * currentNo.setText(String.valueOf(qInfo.UnitNo())); } else if
		 * (currentId == 1) { currentNo.setText(qInfo.MockNo()); } else if
		 * (currentId == 3) { currentNo.setText(String.valueOf(qInfo.UnitNo()));
		 * }
		 */
		currentNo.setText(String.format("%03d",
				mWebViewViewPager.getCurrentItem() + 1));
		currentNo.setTextSize(30.0f);

		currentTextView = new TextView(this);
		currentTextView.setLayoutParams(new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		if (currentId == 0) {
			currentTextView.setTextColor(Color.parseColor("#804098"));

		} else if (currentId == 3) {
			currentTextView.setTextColor(Color.parseColor("#804098"));

		}

		currentTextView.setTextSize(30.0f);
		currentTextView.setPadding(25, 22, 20, 20);

	}

	public void onEnd(View v) {
		AlertDialog dialog;
		dialog = new AlertDialog.Builder(this)
				.setMessage("그만 보시겠습니까?")
				.setPositiveButton("예", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setNegativeButton("아니요",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {
								dialog.cancel();
							}
						}).show();
	}


	private BackPressCloseHandler backPressCloseHandler;


	@Override
	public boolean onKeyDown(int KeyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {

			if (KeyCode == KeyEvent.KEYCODE_BACK) {

				// 여기에 뒤로 버튼을 눌렀을때 해야할 행동을 지정한다
				if (isDetail) {
					return false;
				} else {

					if (isItemShowUp) {
						playUpMenuHide();

					} else {
			/*			AlertDialog dialog;
						dialog = new AlertDialog.Builder(this)
								.setMessage("그만 보시겠습니까?")
								.setPositiveButton("예",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												finish();
											}
										})
								.setNegativeButton("아니요",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.cancel();
											}
										}).show();
			*/

						backPressCloseHandler.onBackPressed();



					}
					return true;
				}
				// 여기서 리턴값이 중요한데; 리턴값이 true 이냐 false 이냐에 따라 행동이 달라진다.
				// true 일경우 back 버튼의 기본동작인 종료를 실행하게 된다.
				// 하지만 false 일 경우 back 버튼의 기본동작을 하지 않는다.
				// back 버튼을 눌렀을때 종료되지 않게 하고 싶다면 여기서 false 를 리턴하면 된다.
				// back 버튼의 기본동작을 막으면 어플리케이션을 종료할 방법이 없기때문에
				// 따로 종료하는 방법을 마련해야한다.
			}
		}
		return super.onKeyDown(KeyCode, event);
	}


	public class BackPressCloseHandler {

		private long backKeyPressedTime = 0;
		private Toast toast;

		private Activity activity;

		public BackPressCloseHandler(Activity context) {
			this.activity = context;
		}

		public void onBackPressed() {
			if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
				backKeyPressedTime = System.currentTimeMillis();
				showGuide();
				return;
			}
			if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
				activity.finish();
				toast.cancel();
			}
		}

		public void showGuide() {
			toast = Toast.makeText(activity,
					"\'뒤로\'버튼을 한번 더 누르시면 그만 봅니다.", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private static final String LOG_TAG = contentsActivity.class
			.getSimpleName();
	private int mViewPagerCurrentPosition = 0;
	private int swipeDirectionCount = 0;
	private HashMap<String, String> directionHistory = new HashMap<String, String>();
	// swipe 동작 감지
	private ViewPager.OnPageChangeListener mPageChangerListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			mViewPagerCurrentPosition = position;
			// 현재 번호 표시
			// setUnitNoByCurrentPosition(position);

			UIUdateUtils.updateCurrentNo(currentId, currentNo, position);
			// showCorrectAnswer(position);

			showCorrectAnswer(position);// , "0"); //이은정 수정
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			if ((mViewPagerCurrentPosition == position + 1 && positionOffset > 0.5f)
					|| (mViewPagerCurrentPosition == 0 && positionOffset == 0f)) {
				// dialog 가 있으면 다시 표시 안함.

				isPreStroy = prefs.getBoolean("isPre", false);
				if(!isPreStroy) {

					if (commonDialog == null || commonDialog.isShowing() == false
							&& nextDialog != null && !nextDialog.isShowing()) {
						Log.i("Direction", "Right : " + positionOffset);
						++swipeDirectionCount;
						if (swipeDirectionCount < 2) {
							directionHistory.put("right", "");
							onPre(mWebViewViewPager.getCurrentItem() - 1);
							// swipeDirectionCount = 0;
						}
					}
				} else {
					editor.putBoolean("isPre", false);
					editor.putBoolean("isNext", false);
					editor.commit();
				}
			} else if (mViewPagerCurrentPosition == position
					&& positionOffset < 0.5f
					&& positionOffset != 0
					|| (mViewPagerCurrentPosition == dataList.size() - 1 && positionOffset == 0f)) {

				isNextStroy = prefs.getBoolean("isNext", false);
				if(!isNextStroy) {

					if (commonDialog == null || !commonDialog.isShowing()
							&& nextDialog != null && !nextDialog.isShowing()) {
						Log.i("Direction", "Left : " + positionOffset);
						++swipeDirectionCount;
						if (swipeDirectionCount < 2) {
							directionHistory.put("left", "");
							// 다음 문제를 볼수 있는 확인 후 볼 수 없는 경우 현재 문제 유지
							onNext(mWebViewViewPager.getCurrentItem() + 1, false);
							// swipeDirectionCount = 0;
						}
					}
				}else{
					editor.putBoolean("isNext", false);
					editor.putBoolean("isPre", false);
					editor.commit();
				}
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {

			if (state == ViewPager.SCROLL_STATE_IDLE)
				swipeDirectionCount = 0;
			else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
				directionHistory.clear();
				// swipe 시작할 때 보이는 문제의 위치
				swipeStartPosition = mWebViewViewPager.getCurrentItem();
			}
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*
		 * if (event.getAction() == MotionEvent.ACTION_DOWN) { _active = false;
		 * }
		 */
		Log.i("test", "onTouchEvent");
		return false;
	}

	// 이은정 추가
	// 메모리 부족으로 인한 다운 현상이 발생하지 않도록 가비지 콜렉션하도록 알림
	@Override
	protected void onDestroy() {
		super.onDestroy();

		dataList = null;
		System.gc();
	}
/*
	private void showCommonDialog(int type) {

		AlertDialog.Builder alt_bld2 = new AlertDialog.Builder(
				contentsActivity.this);

		if (type != FIRST) {
			alt_bld2.setCancelable(false)
					.setMessage("마지막 페이지입니다.")
					.setPositiveButton("그만보기",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							})
					.setNeutralButton("확인",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
		//	lastAlert.show();
		//	goNextStory();

		} else if (type == FIRST) {
			alt_bld2.setCancelable(false)
					.setMessage("첫번째 페이지입니다.")
					.setNegativeButton("확인",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
		}

		commonDialog = alt_bld2.create();
		commonDialog.show();
	}   */
}