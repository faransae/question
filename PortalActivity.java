package com.sangsang.beyondtportal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.sangsang.dataManager.DbAdapter;
import com.sangsang.utils.ExpansionFileManager;
import com.sangsang.utils.IabHelper;
import com.sangsang.utils.IabResult;
import com.sangsang.utils.Inventory;
import com.sangsang.utils.Purchase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class PortalActivity extends Activity implements Animation.AnimationListener, OnClickListener {

    View menu;
    View app;
    boolean menuOut = false;
    AnimParams animParams = new AnimParams();

    class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {


            System.out.println("onClick " + new Date());
            PortalActivity me = PortalActivity.this;
            Context context = me;
            Animation anim;


            float app_position_x = app.getX();


            if (app_position_x > 100) {
                menuOut = true;
            }



            int w = app.getMeasuredWidth();
            int h = app.getMeasuredHeight();
            int left = (int) (app.getMeasuredWidth() * 0.8);

            if (!menuOut) {
                anim = new TranslateAnimation(0, left, 0, 0);
                menu.setVisibility(View.VISIBLE);
                animParams.init(left, 0, left + w, h);
            } else {
                anim = new TranslateAnimation(0, -left, 0, 0);
                animParams.init(0, 0, w, h);
            }

            anim.setDuration(500);
            anim.setAnimationListener(me);
            anim.setFillAfter(true);
            app.startAnimation(anim);
        }
    }


    void layoutApp(boolean menuOut) {
        System.out.println("layout [" + animParams.left + "," + animParams.top + "," + animParams.right + ","
                + animParams.bottom + "]");
        app.layout(animParams.left, animParams.top, animParams.right, animParams.bottom);
        app.clearAnimation();

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        System.out.println("onAnimationEnd");
        menuOut = !menuOut;
        if (!menuOut) {
            menu.setVisibility(View.INVISIBLE);
        }
        layoutApp(menuOut);


    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        System.out.println("onAnimationRepeat");
    }

    @Override
    public void onAnimationStart(Animation animation) {
        System.out.println("onAnimationStart");
    }

    static class AnimParams {
        int left, right, top, bottom;

        void init(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    private DbAdapter dbAdapter;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    public static final String KEY_PREFERENCE_USERINFO = "userinfo_preference";

    private AlertDialog back_alert;
    private AlertDialog down_alert;
    private AlertDialog buy_alert;


    private static String DB_NAME;
    private static String DB_TEMPNAME;
    private static String DB_ZIPNAME;
    private int currentLevel = 0;

    private ProgressDialog mProgressDialog;
    public static TextView wait;
    public static LinearLayout waitContainer;
    public static LinearLayout downloadContainer;

    public static final int COPY_END = 99999;
    public static final int MOVE_ACTIVITY = 99991;
    public static final int COPY_START = 9993;

    private ExpansionFileManager exFileMgr;

    private ImageButton buttonLevel1;
    private ImageButton buttonLevel3;
    private ImageButton buttonLevel4;

    ImageButton BookButton01;
    ImageButton BookButton02;
    ImageButton BookButton03;
    ImageButton BookButton04;

//    private AdView mAdView;


    private String payID;

    private AlertDialog activate_alert;
    protected static final int REQUEST_BUY = 1001;


    IInAppBillingService mService;
    IabHelper mHelper;

    ServiceConnection mServiceConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };


    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            System.gc();
            super.handleMessage(msg);

            switch (msg.what) {

                case COPY_END:
                    Intent intent = new Intent();
                    intent.setClass(PortalActivity.this, quizActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.out_left, R.anim.in_left);
                    // finish();
                case COPY_START:
                    if (handler.hasMessages(MOVE_ACTIVITY)) {
                        handler.removeMessages(MOVE_ACTIVITY);
                    }
                    break;

                case MOVE_ACTIVITY:

                    dbAdapter = new DbAdapter(PortalActivity.this);
                    dbAdapter.close();

                    Intent mIntent = new Intent();
                    mIntent.setClass(PortalActivity.this, quizActivity.class);
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.out_left, R.anim.in_left);
                    // finish();
                    break;

            }
        }
    };

    public void BuyRelease() {
        // Log.d(LogTag, "아이템 구매 종료 (초기화)");
        if (mHelper != null)
            mHelper.dispose();
        mHelper = null;

        if (mServiceConn != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        editor.putInt("level", 0);
        editor.commit();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        editor.putInt("level", 0);
        editor.commit();

        showMenu();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        editor.putInt("level", 0);
        editor.commit();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.portal);


        app = findViewById(R.id.app);
        app.findViewById(R.id.BtnSlide).setOnClickListener(new ClickListener());
        menu = findViewById(R.id.menu);

        wait = (TextView) findViewById(R.id.text_wait);
        waitContainer = (LinearLayout) findViewById(R.id.text_wait_container);
        waitContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // 터치 이벤트 제거
                return true;
            }

            ;
        });
        waitContainer.setVisibility(View.GONE);

        downloadContainer = (LinearLayout) findViewById(R.id.download_view_container);
        downloadContainer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

            }
        });

        prefs = getSharedPreferences(KEY_PREFERENCE_USERINFO, MODE_PRIVATE);
        editor = prefs.edit();
/*
        mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(
                AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);

        interstitial = new InterstitialAd(PortalActivity.this);
        // Insert the Ad Unit ID
        interstitial.setAdUnitId("ca-app-pub-7800501464776549/2986105713");
        interstitial.loadAd(adRequest);

        // Set the AdListener.
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
//                Toast.makeText(PortalActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
//                Toast.makeText(PortalActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();


            }
        });
*/

        BookButton01 = (ImageButton) findViewById(R.id.book_01);
        BookButton01.setOnClickListener(this);
        BookButton02 = (ImageButton) findViewById(R.id.book_02);
        BookButton02.setOnClickListener(this);
        BookButton03 = (ImageButton) findViewById(R.id.book_03);
        BookButton03.setOnClickListener(this);
        BookButton04 = (ImageButton) findViewById(R.id.book_04);
        BookButton04.setOnClickListener(this);


        buttonLevel1 = (ImageButton) findViewById(R.id.button_level1);
        buttonLevel1.setOnClickListener(this);
        buttonLevel3 = (ImageButton) findViewById(R.id.button_level3);
        buttonLevel3.setOnClickListener(this);
        buttonLevel4 = (ImageButton) findViewById(R.id.button_level4);
        buttonLevel4.setOnClickListener(this);


        initBackDialog();
        initBuyDialog();
        startDownloadDialog();

        showMenu();
    }


    private InterstitialAd interstitial;


    public void displayInterstitial() {
        // If Ads are loaded, show Interstitial else show nothing.
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }


    public void InAppInit_U() {

        bindService(new Intent(
                        "com.android.vending.billing.InAppBillingService.BIND"),
                mServiceConn, Context.BIND_AUTO_CREATE);

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8"
                + "AMIIBCgKCAQEA3Uo9DrvB7J3sbRZg6s9kOIxybOAR1s6PZwe+3ENmseqlq"
                + "hXSc+0CEc6Tt82yB2o7xe2tsuMydUZWcN8N3iwQ7eDZQMti7Rnm0saSC97"
                + "Uj7cCE1TqLoq6kmA5fBkRqdJ6F06mCZfKRWWwKo7sIYd8gl+MaTqd3ldg6"
                + "7CWDstCRnIxciu6ZW+wWGHe2XOHDRGyP1jjal1yz7if/Q+CD/YVS/S8MVI"
                + "9XzUY6HDZEQqUOGIdi67kjc8s9rSA/3qutEl0JRf8NtshJEHRAHkC2a93P"
                + "4zXYTU5d7p34Q9fj9+u3zPTzB2aalgky5fOCDn0+wmuyBFq+fe0/uaUAts"
                + "iu6XBRQIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // 구매오류처리 ( 토스트하나 띄우고 결제팝업 종료시키면 되겠습니다 )
                    return;
                }

                if (mHelper == null)
                    return;

                mHelper.queryInventoryAsync(mGotInventoryListener);

            }
        });
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            // TODO
            if (result.isFailure()) {
                Log.d("InApp", "구매 한적 있는지 확인 실패" + result.getMessage());
                return; // handle error
            }
            Purchase checkPurchase = inv.getPurchase(payID);
            Log.i("InApp", payID);
            if (checkPurchase == null) {
                Log.d("InApp", "구매 한적이 없습니다.");
            } else {
                Log.d("InApp", "구매 한적이 있습니다.");
            }

            // 구매한 적이있으면 일단은 구매 정보를 지운다 어짜피 서버에 기록되어있으니깐
            if (checkPurchase != null && verifyDeveloperPayload(checkPurchase)) {

                AlertDialog.Builder alt_bld = new AlertDialog.Builder(
                        PortalActivity.this);
                alt_bld.setMessage("이미 구매한 문제집입니다.\n다시 다운받으시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("예",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        // Action for 'Yes' Button
                                        dialog.cancel();
                                        download();
                                        // down_alert.show();
                                    }
                                })
                        .setNegativeButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        // Action for 'NO' Button
                                        dialog.cancel();


                                    }
                                });
                activate_alert = alt_bld.create();
                activate_alert.setTitle("문제집 구매 확인");
                activate_alert.setIcon(R.drawable.icon);
                activate_alert.show();

                return;
            }
        }
    };

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        if (p == null)
            return false;
        String payload = p.getDeveloperPayload();
        return true;
    }

    public void InAppBuyItem_U(final String strItemId) {
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                    strItemId, "inapp", "test");
            PendingIntent pendingIntent = buyIntentBundle
                    .getParcelable("BUY_INTENT");

            if (pendingIntent != null) {

                mHelper.launchPurchaseFlow(this, strItemId, REQUEST_BUY,
                        mPurchaseFinishedListener, "test");
                // 위에 두줄 결제호출이 2가지가 있는데 위에것을 사용하면 결과가 onActivityResult 메서드로 가고,
                // 밑에것을 사용하면 OnIabPurchaseFinishedListener 메서드로 갑니다. (참고하세요!)
            } else {
                // 결제가 막혔다면
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

           download();
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {

            // if we were disposed of in the meantime, quit.
            if (mHelper == null)
                return;

            if (result.isSuccess()) {

            } else {

            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BUY) {

            if (resultCode == RESULT_OK) {
                if (!mHelper
                        .handleActivityResult(requestCode, resultCode, data)) {
                    super.onActivityResult(requestCode, resultCode, data);

                } else {

                }
            } else {


            }

        }
    }

    public void showMenu() {

        String dbfilePath = "";
        File sdcard = Environment.getExternalStorageDirectory();
        File dbpath = new File(sdcard.getAbsolutePath() + File.separator
                + "BeyondTPortal" + File.separator + "databases");
        File dbFile;


        int buttonCount = 0;

        for (int i = 1; i < 6; i++) {

            dbfilePath = dbpath.getAbsolutePath() + File.separator + "surprise"
                    + i + ".sqlite3";
            dbFile = new File(dbfilePath);

            String fileName = "";

            if (!dbFile.exists()) {
                //               fileName = String.format("level%d_lock.png", i);
            } else {
                buttonCount++;
                switch (i) {
                    case 1:
     //                   buttonLevel1.setVisibility(View.GONE);
                        buttonLevel1.setImageResource(R.drawable.button_read);
                        break;
                    case 2:
                        //                       buttonLevel1.setEnabled(false);
                        break;
                    case 3:
   //                     buttonLevel3.setVisibility(View.GONE);
                        buttonLevel3.setImageResource(R.drawable.button_read);
                        break;
                    case 4:
    //                    buttonLevel4.setVisibility(View.GONE);
                        buttonLevel4.setImageResource(R.drawable.button_read);
                        break;
                }

                fileName = String.format("book%d.png", i);
                String filePath = Environment.getExternalStorageDirectory()
                        + "/BeyondTPortal/images/" + fileName;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);

                switch (buttonCount) {
                    case 1:
                        BookButton01.setImageDrawable(ob);
                        BookButton01.setTag(i);
                        break;
                    case 2:
                        BookButton02.setImageDrawable(ob);
                        BookButton02.setTag(i);
                        break;
                    case 3:
                        BookButton03.setImageDrawable(ob);
                        BookButton03.setTag(i);
                        break;
                    case 4:
                        BookButton04.setImageDrawable(ob);
                        BookButton04.setTag(i);
                        break;
                }
            }
        }
    }

    private void initBackDialog() {
        // TOOD : 다운로드 유무 확인 Dialog
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(
                PortalActivity.this);
        alt_bld.setMessage("프로그램을 종료하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'Yes' Button
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("아니요",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Action for 'NO' Button
                                dialog.cancel();
                            }
                        });
        back_alert = alt_bld.create();
        back_alert.setTitle("신비한 만화 서프라이즈");
        back_alert.setIcon(R.drawable.icon);

    }

    private void startDownloadDialog() {
        // TOOD : 다운로드 유무 확인 Dialog
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(
                PortalActivity.this);
        alt_bld.setMessage("파일을 다운로드 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'Yes' Button
                        download();
                    }
                })
                .setNegativeButton("아니요",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Action for 'NO' Button
                                dialog.cancel();
                                showMenu();
                            }
                        });
        down_alert = alt_bld.create();
        down_alert.setTitle("신비한 만화 서프라이즈");
        down_alert.setIcon(R.drawable.icon);
    }

    private void initBuyDialog() {
        // TOOD : 구매 유무 확인 Dialog
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(
                PortalActivity.this);
        alt_bld.setMessage("구매 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'Yes' Button
                        InAppBuyItem_U(payID);
                    }
                })
                .setNegativeButton("아니요",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Action for 'NO' Button
                                dialog.cancel();
                            }
                        });
        buy_alert = alt_bld.create();
        buy_alert.setTitle("여행 그 이상");
        buy_alert.setIcon(R.drawable.icon);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (menuOut) {

                PortalActivity me = PortalActivity.this;
                Context context = me;
                Animation anim;

                int w = app.getMeasuredWidth();
                int h = app.getMeasuredHeight();
                int left = (int) (app.getMeasuredWidth() * 0.8);

                if (!menuOut) {
                    // anim = AnimationUtils.loadAnimation(context, R.anim.push_right_out_80);
                    anim = new TranslateAnimation(0, left, 0, 0);
                    menu.setVisibility(View.VISIBLE);
                    animParams.init(left, 0, left + w, h);
                } else {
                    // anim = AnimationUtils.loadAnimation(context, R.anim.push_left_in_80);
                    anim = new TranslateAnimation(0, -left, 0, 0);
                    animParams.init(0, 0, w, h);
                }

                anim.setDuration(500);
                anim.setAnimationListener(me);
                anim.setFillAfter(true);
                app.startAnimation(anim);

            } else {
                back_alert.show();
            }

        }
        return true;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {


            case R.id.button_level1:



                DB_NAME = "surprise1.sqlite3";
                DB_TEMPNAME = "surprise1.mp3";
                DB_ZIPNAME = "surprise1.zip";
                currentLevel = 1;

                editor.putInt("level", currentLevel);
                editor.commit();

                isDB();





                break;

            case R.id.button_level3:
                DB_NAME = "surprise3.sqlite3";
                DB_TEMPNAME = "surprise3.mp3";
                DB_ZIPNAME = "surprise3.zip";
                currentLevel = 3;

                editor.putInt("level", currentLevel);
                editor.commit();

                isDB();
                break;

            case R.id.button_level4:
   /*             DB_NAME = "surprise4.sqlite3";
                DB_TEMPNAME = "surprise4.mp3";
                DB_ZIPNAME = "surprise4.zip";
                currentLevel = 4;

                editor.putInt("level", currentLevel);
                editor.commit();

                isDB();  */
                break;


            default:
                String level = v.getTag().toString();
                currentLevel = Integer.parseInt(level);
                editor.putInt("level", currentLevel);
                editor.commit();

                String nameTemp = "surprise" + currentLevel + ".sqlite3";
                DB_NAME = nameTemp;
                String tempTemp = "surprise" + currentLevel + ".mp3";
                DB_TEMPNAME = tempTemp;
                String zipTemp = "surprise" + currentLevel + ".zip";
                DB_ZIPNAME = zipTemp;

                isDB();

                break;
        }

    }

    private void download() {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("다운로드 중입니다...\n잠시만 기다려 주세요 ...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);

        wait.setVisibility(View.VISIBLE);

        // execute this when the downloader must be fired
        final DownloadTask downloadTask = new DownloadTask(this);

        switch (currentLevel) {
            case 1:
                downloadTask
                        .execute("http://52.68.64.252/historytest/surprise1.mp3");
                break;
            case 3:
                downloadTask
                        .execute("http://52.68.64.252/historytest/surprise3.mp3");
                break;
            case 4:
                downloadTask
                        .execute("http://52.68.64.252/historytest/surprise4.mp3");
                break;
            case 5:
                downloadTask
                        .execute("http://52.68.64.252/historytest/surprise5.mp5");
                break;
            case 6:
                downloadTask
                        .execute("http://52.68.64.252/historytest/surprise6.mp3");
                break;
            case 7:
                downloadTask
                        .execute("http://52.68.64.252/historytest/surprise7.mp3");
                break;
            case 8:
                downloadTask
                        .execute("http://52.68.64.252/historytest/surprise8.mp3");
                break;
        }
    }

    private void isDB() {

        String dbfilePath = "";
        File sdcard = Environment.getExternalStorageDirectory();
        File dbpath = new File(sdcard.getAbsolutePath() + File.separator
                + "BeyondTPortal" + File.separator + "databases");
        dbfilePath = dbpath.getAbsolutePath() + File.separator + DB_NAME;
        File dbFile = new File(dbfilePath);

        if (!dbFile.exists()) {

            if (currentLevel != 1) {
     /*           payID = "story" + currentLevel;
                InAppInit_U();
                InAppBuyItem_U(payID);
                buy_alert.show();     */
                down_alert.show();
            } else {
                down_alert.show();
            }


        } else {

/*
            if (currentLevel == 1) {
                if (interstitial.isLoaded()) {
                    interstitial.show();
                } else {

                }
            }
*/
            dbAdapter = new DbAdapter(this);
            dbAdapter.close();

            Intent intent = new Intent();
            intent.setClass(PortalActivity.this, quizActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.out_left, R.anim.in_left);
        }

    }

    public void validFileCheck() {

        waitContainer.setVisibility(View.VISIBLE);

        exFileMgr = new ExpansionFileManager(this);
        exFileMgr.validFileCheck(handler);

    }

    // usually, subclasses of AsyncTask are declared inside the activity class.
    // that way, you can easily modify the UI thread from here
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @SuppressLint("SdCardPath")
        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP "
                            + connection.getResponseCode() + " "
                            + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                switch (currentLevel) {
                    case 1:
                        output = new FileOutputStream("/sdcard/surprise1.mp3");
                        break;
                    case 3:
                        output = new FileOutputStream("/sdcard/surprise3.mp3");
                        break;
                    case 4:
                        output = new FileOutputStream("/sdcard/surprise4.mp3");
                        break;
                    case 5:
                        output = new FileOutputStream("/sdcard/surprise5.mp3");
                        break;
                    case 6:
                        output = new FileOutputStream("/sdcard/surprise6.mp3");
                        break;

                }

                byte data[] = new byte[4096];
                long total = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);

        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result,
                        Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(context, "서프라이즈를 설치하고 있습니다.  잠시만 기다려 주세요.",
                        Toast.LENGTH_SHORT).show();

                editor.putInt("level", currentLevel);
                editor.commit();


                if (currentLevel != 1) {
                    BuyRelease();
                }

                validFileCheck();
            }
        }

    }

}
