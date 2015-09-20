package com.sangsang.beyondtportal;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;

import com.sangsang.utils.ExpansionFileManager;

public class introActivity extends Activity {

	public static final String KEY_PREFERENCE_USERINFO = "userinfo_preference";
	private ProgressBar circleProgress;

	public static final int COPY_END = 99999;
	public static final int MOVE_ACTIVITY = 99991;
	public static final int COPY_START = 9993;

	SharedPreferences.Editor editor;
	
	private ExpansionFileManager exFileMgr;
	

	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			System.gc();
			super.handleMessage(msg);

			switch (msg.what) {

			case COPY_END:			
				Intent intent = new Intent();
				intent.setClass(introActivity.this, PortalActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.out_left, R.anim.in_left);
				finish();
				break;
				
			case COPY_START:
				if (handler.hasMessages(MOVE_ACTIVITY)) {
					handler.removeMessages(MOVE_ACTIVITY);
				}
				break;

			case MOVE_ACTIVITY:				
				Intent mIntent = new Intent();
				mIntent.setClass(introActivity.this, PortalActivity.class);
				startActivity(mIntent);
				overridePendingTransition(R.anim.out_left, R.anim.in_left);
				finish();
				break;
			}
		}
	};


	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);		
		setContentView(R.layout.splash);

		circleProgress = (ProgressBar) findViewById(R.id.circle_progress);

		SharedPreferences prefs = getSharedPreferences(KEY_PREFERENCE_USERINFO,
				MODE_PRIVATE);
		editor = prefs.edit();
		editor.putInt("level", 0);

		exFileMgr = new ExpansionFileManager(this);
		exFileMgr.validFileCheck(handler);

		handler.sendEmptyMessageDelayed(MOVE_ACTIVITY, 2000);
	}
	

	

	
	
}

