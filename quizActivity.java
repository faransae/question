package com.sangsang.beyondtportal;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sangsang.Info.dataInfo;
import com.sangsang.dataManager.DbAdapter;
import com.sangsang.widget.SnappingHorizontalScrollView;

public class quizActivity extends Activity {

    private SnappingHorizontalScrollView sHScrollView;
    private HorizontalScrollView subScrollView;
    private LinearLayout snapHScrollContainer;
    private LinearLayout subHScrollContainer;

    private LinearLayout adContainer;
    private LinearLayout adBGContainer;

    // private TextView titleView;
    private DbAdapter dbAdapter;
    private ArrayList<dataInfo> dataList;
    private ArrayList<dataInfo> detailList;

    private SharedPreferences prefs;
    private int currentIndex = -1;

    private LinearLayout subConatiner;
    private LinearLayout buttonContainer;

    private ImageButton button01;
    private ImageButton button02;
    private ImageButton button03;
    private ImageButton button04;
    private ImageButton button05;
    private ImageButton button06;



    private ImageButton next_button;
    private ImageButton home_button;

    private boolean isResumeFirst;

    private AlertDialog back_alert;

    Button[] btnWord = new Button[7];

    private AdView mAdView;

    private static final int Contents_ACTIVITY = 0;

    private int currentLevel;

    private int storyoff1;
    private int storyoff2;
    private int storyoff3;
    private int storyoff4;
    private int storyoff5;
    private int storyoff6;


    private int endPage;

    // LinearLayout linear;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        prefs = getSharedPreferences(PortalActivity.KEY_PREFERENCE_USERINFO,
                MODE_PRIVATE);
        currentLevel = prefs.getInt("level", 99);

        if (currentLevel == 1) {
            setContentView(R.layout.quiz);
        } else {
            setContentView(R.layout.quiz2);
        }



        sHScrollView = (SnappingHorizontalScrollView) findViewById(R.id.snap_scrollview);
        sHScrollView.setHorizontalScrollBarEnabled(false);

        subScrollView = (HorizontalScrollView) findViewById(R.id.sub_scrollview);
        subScrollView.setHorizontalScrollBarEnabled(false);

        sHScrollView.setItemWidth(this.getWindowManager().getDefaultDisplay()
                .getWidth());

        String aaa = ""
                + this.getWindowManager().getDefaultDisplay().getWidth();
        Log.i("test", aaa);

        sHScrollView.setMaxItem(9);
        sHScrollView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                boolean bValue = sHScrollView.onTouchReceive(v, event);

                if (event.getAction() == MotionEvent.ACTION_MOVE) {

                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    setSubItemByIndex(sHScrollView.getActiveItemIndex());
                }

                return bValue;
            }
        });


 //       adBGContainer = (LinearLayout) findViewById(R.id.ad_view_bg);
  //      adBGContainer.setVisibility(View.GONE);

        buttonContainer = (LinearLayout) findViewById(R.id.button_container);
        buttonContainer.setVisibility(View.GONE);
        button01 = (ImageButton) findViewById(R.id.button_01);
        button02 = (ImageButton) findViewById(R.id.button_02);
        button03 = (ImageButton) findViewById(R.id.button_03);
        button04 = (ImageButton) findViewById(R.id.button_04);
        button05 = (ImageButton) findViewById(R.id.button_05);
        button06 = (ImageButton) findViewById(R.id.button_06);


        home_button = (ImageButton) findViewById(R.id.button_home);
        next_button = (ImageButton) findViewById(R.id.button_next);

        button01.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                buttonContainer.setVisibility(View.GONE);
                sHScrollView.scrollTo(800 * 2, 0);
                sHScrollView.setActiveItemIndex(2);
                setSubItemByIndex(2);
            }
        });
        button02.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                buttonContainer.setVisibility(View.GONE);
                sHScrollView.scrollTo(800 * 3, 0);
                sHScrollView.setActiveItemIndex(3);
                setSubItemByIndex(3);
            }
        });
        button03.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                buttonContainer.setVisibility(View.GONE);
                sHScrollView.scrollTo(800 * 4, 0);
                sHScrollView.setActiveItemIndex(4);
                setSubItemByIndex(4);
            }
        });
        button04.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                buttonContainer.setVisibility(View.GONE);
                sHScrollView.scrollTo(800 * 5, 0);
                sHScrollView.setActiveItemIndex(5);
                setSubItemByIndex(5);
            }
        });
        button05.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                buttonContainer.setVisibility(View.GONE);
                sHScrollView.scrollTo(800 * 6, 0);
                sHScrollView.setActiveItemIndex(6);
                setSubItemByIndex(6);
            }
        });
        button06.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                buttonContainer.setVisibility(View.GONE);
                sHScrollView.scrollTo(800 * 7, 0);
                sHScrollView.setActiveItemIndex(7);
                setSubItemByIndex(7);
            }
        });



        home_button.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                buttonContainer.setVisibility(View.VISIBLE);

                home_button.setVisibility(View.GONE);

                sHScrollView.scrollTo(800 * 1, 0);
                sHScrollView.setActiveItemIndex(1);

                subHScrollContainer.removeAllViews();
                subScrollView.scrollTo(0, 0);
            }
        });

        next_button.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                buttonContainer.setVisibility(View.VISIBLE);

                sHScrollView.scrollTo(800 * 1, 0);
                sHScrollView.setActiveItemIndex(1);

                subHScrollContainer.removeAllViews();
                subScrollView.scrollTo(0, 0);

                next_button.setVisibility(View.GONE);
            }
        });

        snapHScrollContainer = (LinearLayout) findViewById(R.id.snapHScrollContainer);

        addHotItem("chapter_01");
        addHotItem("chapter_02");
        addHotItem("chapter_03");
        addHotItem("chapter_04");
        addHotItem("chapter_05");
        addHotItem("chapter_06");
        addHotItem("chapter_07");
        addHotItem("chapter_08");
        addHotItem("chapter_09");


        subConatiner = (LinearLayout) findViewById(R.id.sub_container);
        subHScrollContainer = (LinearLayout) findViewById(R.id.subHScrollContainer);

        currentIndex = 0;

        initBackDialog();

        if (currentLevel == 1) {

            mAdView = (AdView) findViewById(R.id.ad_view);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(
                    AdRequest.DEVICE_ID_EMULATOR).build();

            mAdView.loadAd(adRequest);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        isResumeFirst = true;

        setSubItemByIndex(sHScrollView.getActiveItemIndex());
    }

    private void setSubItemByIndex(int index) {

        Log.d("subitem Index :", String.valueOf(index));

        if (currentIndex != index || isResumeFirst) {

            int subQuizIndex = 0;

            subHScrollContainer.removeAllViews();
            subScrollView.scrollTo(0, 0);

            String current = "" + currentLevel;

            switch (index) {
                case 0:


                    buttonContainer.setVisibility(View.GONE);
                    home_button.setVisibility(View.GONE);
                    next_button.setVisibility(View.VISIBLE);
                    break;
                case 1:


                    next_button.setVisibility(View.GONE);
                    buttonContainer.setVisibility(View.VISIBLE);
                    home_button.setVisibility(View.GONE);
                    break;
                case 2:

                    dbAdapter = new DbAdapter(this);
                    detailList = dbAdapter.getPageList("STORY", "1");
                    dbAdapter.close();
                    for (int j = 0; j < detailList.size(); j++) {
                        addHotSubItem(1, subQuizIndex);
                        subQuizIndex++;
                    }
                    buttonContainer.setVisibility(View.GONE);
                    home_button.setVisibility(View.VISIBLE);
                    break;
                case 3:

                    dbAdapter = new DbAdapter(this);
                    detailList = dbAdapter.getPageList("STORY", "2");
                    dbAdapter.close();
                    for (int j = 0; j < detailList.size(); j++) {
                        addHotSubItem(2, subQuizIndex);
                        subQuizIndex++;
                    }
                    home_button.setVisibility(View.VISIBLE);
                    break;
                case 4:



                    dbAdapter = new DbAdapter(this);
                    detailList = dbAdapter.getPageList("STORY", "3");
                    dbAdapter.close();
                    for (int j = 0; j < detailList.size(); j++) {
                        addHotSubItem(3, subQuizIndex);
                        subQuizIndex++;
                    }
                    home_button.setVisibility(View.VISIBLE);
                    break;
                case 5:


                    dbAdapter = new DbAdapter(this);
                    detailList = dbAdapter.getPageList("STORY", "4");
                    dbAdapter.close();
                    for (int j = 0; j < detailList.size(); j++) {
                        addHotSubItem(4, subQuizIndex);
                        subQuizIndex++;
                    }
                    home_button.setVisibility(View.VISIBLE);
                    break;
                case 6:


                    dbAdapter = new DbAdapter(this);
                    detailList = dbAdapter.getPageList("STORY", "5");
                    dbAdapter.close();
                    for (int j = 0; j < detailList.size(); j++) {
                        addHotSubItem(5, subQuizIndex);
                        subQuizIndex++;
                    }
                    home_button.setVisibility(View.VISIBLE);
                    break;

                case 7:


                    dbAdapter = new DbAdapter(this);
                    detailList = dbAdapter.getPageList("STORY", "6");
                    dbAdapter.close();
                    for (int j = 0; j < detailList.size(); j++) {
                        addHotSubItem(6, subQuizIndex);
                        subQuizIndex++;
                    }
                    home_button.setVisibility(View.VISIBLE);
                    break;

                case 8:

                    break;
            }

        }
        currentIndex = index;
        // startAnimation

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int px = 0;
        int userDpi = this.getApplicationContext().getResources()
                .getDisplayMetrics().densityDpi;

        px = 120 * outMetrics.densityDpi / 160;
/*
        switch (userDpi) {
            case DisplayMetrics.DENSITY_HIGH:
                px = 100 * outMetrics.densityDpi / 160;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                px = 200 * outMetrics.densityDpi / 160;
                break;
            case DisplayMetrics.DENSITY_LOW:
                px = 120 * outMetrics.densityDpi / 160;
                break;
            default:
                break;
        }
  */
        Animation ani = new TranslateAnimation(0, 0, px, 0);
        ani.setDuration(500);
        ani.setFillAfter(true);

        subConatiner.startAnimation(ani);

        // subConatiner.setVisibility(View.GONE);

        isResumeFirst = false;

    }

    void addHotItem(String imageName) {

        String filePath = Environment.getExternalStorageDirectory()
                + "/BeyondTPortal/surprise" + currentLevel + "/" + imageName
                + ".png";
        ;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
        // 패스는 Environment.getExternalStorageDirectory()+"/"+나머지 패스입니다.

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setGravity(Gravity.TOP);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(this
                .getWindowManager().getDefaultDisplay().getWidth(), this
                .getWindowManager().getDefaultDisplay().getHeight()));
        linearLayout.setBackgroundDrawable(ob);

        snapHScrollContainer.addView(linearLayout);
    }

    OnClickListener goHome = new OnClickListener() {
        // @Override
        public void onClick(View v) {
            sHScrollView.scrollTo(800, 0);
            sHScrollView.setActiveItemIndex(1);

            subHScrollContainer.removeAllViews();
            subScrollView.scrollTo(0, 0);
        }
    };

    OnClickListener btnClicked = new OnClickListener() {
        // @Override
        public void onClick(View v) {
            String tag = v.getTag().toString();
            int index = Integer.parseInt(tag);
            setSubItemByIndex(index + 2);

            currentIndex = index + 2;
            sHScrollView.scrollTo(800 * currentIndex, 0);
            sHScrollView.setActiveItemIndex(currentIndex);
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case Contents_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    int storyNumber = intent.getExtras().getInt("data");
                    int direction = intent.getExtras().getInt("direction");

                    if (direction == 1) {

                        storyNumber++;
                        sHScrollView.scrollTo(800 * storyNumber, 0);
                        sHScrollView.setActiveItemIndex(storyNumber);

                        Intent intent1 = new Intent();
                        intent1.putExtra("position", 0);
                        String story = "" + storyNumber;
                        intent1.putExtra("StoryLevel", story);
                        intent1.setClass(quizActivity.this, contentsActivity.class);
                        startActivityForResult(intent1, Contents_ACTIVITY);

                        //                     sHScrollView.scrollTo(800, 0);
                        //                     sHScrollView.setActiveItemIndex(1);

                    } else if (direction == 0) {

                        storyNumber--;
                        sHScrollView.scrollTo(800 * storyNumber, 0);
                        sHScrollView.setActiveItemIndex(storyNumber);


                        dbAdapter = new DbAdapter(this);
                        storyoff1 = dbAdapter.getStoryOff1("CurrentLevel", "1");
                        storyoff2 = dbAdapter.getStoryOff2("CurrentLevel", "2");
                        storyoff3 = dbAdapter.getStoryOff3("CurrentLevel", "3");
                        storyoff4 = dbAdapter.getStoryOff4("CurrentLevel", "4");
                        storyoff5 = dbAdapter.getStoryOff5("CurrentLevel", "5");
                        storyoff6 = dbAdapter.getStoryOff6("CurrentLevel", "6");
                        dbAdapter.close();




                        switch (storyNumber) {
                            case 1:
                                endPage = storyoff1-1;
                                break;
                            case 2:
                                endPage = storyoff2-1;
                                break;
                            case 3:
                                endPage = storyoff3-1;
                                break;
                            case 4:
                                endPage = storyoff4-1;
                                break;
                            case 5:
                                endPage = storyoff5-1;
                                break;
                            case 6:
                                endPage = storyoff6-1;
                                break;
                        }


                        Intent intent1 = new Intent();
                        intent1.putExtra("position", endPage);
                        String story = "" + storyNumber;
                        intent1.putExtra("StoryLevel", story);
                        intent1.setClass(quizActivity.this, contentsActivity.class);
                        startActivityForResult(intent1, Contents_ACTIVITY);

                        //                       sHScrollView.scrollTo(800, 0);
                        //                       sHScrollView.setActiveItemIndex(1);
                    }
                }
        }
    }

    void addHotSubItem(int pIndex, final int subIndex) {

        final String StoryLevel = "" + pIndex;

        FrameLayout fLayout = new FrameLayout(this);
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.FILL_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        fLayout.setLayoutParams(fp);

        // LinearLayout
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout subItemLayout = new LinearLayout(this);
        subItemLayout.setPadding(5, 0, 5, 0);
        subItemLayout.setLayoutParams(lp);
        subItemLayout.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("position", subIndex);
                intent.putExtra("StoryLevel", StoryLevel);
                intent.setClass(quizActivity.this, contentsActivity.class);
                startActivityForResult(intent, Contents_ACTIVITY);
            }
        });

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int xpx = 0;
        int ypx = 0;

        int userDpi = this.getApplicationContext().getResources()
                .getDisplayMetrics().densityDpi;

        xpx = 55 * outMetrics.densityDpi / 160;
        ypx = 100 * outMetrics.densityDpi / 160;
/*
        switch (userDpi) {

            case DisplayMetrics.DENSITY_HIGH:
                xpx = 50 * outMetrics.densityDpi / 160;
                ypx = 100 * outMetrics.densityDpi / 160;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                xpx = 50 * outMetrics.densityDpi / 160;
                ypx = 100 * outMetrics.densityDpi / 160;
                break;
            case DisplayMetrics.DENSITY_LOW:
                xpx = 50 * outMetrics.densityDpi / 160;
                ypx = 100 * outMetrics.densityDpi / 160;
                break;
            default:
                break;
        }
*/
        lp = new LinearLayout.LayoutParams(xpx, ypx);

        // Image

        if (subIndex != 0) {

            ImageView imgView = new ImageView(this);

            if (pIndex == 1) {

                int tempIndex = subIndex + 101;//storyoff1;

                String fileName = String.format("a%03d.png", tempIndex);

                String filePath = Environment.getExternalStorageDirectory()
                        + "/BeyondTPortal/surprise" + currentLevel + "/"
                        + fileName;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                imgView.setImageDrawable(ob);

            } else if (pIndex == 2) {

                int tempIndex = subIndex + 201;//storyoff2;

                String fileName = String.format("a%03d.png", tempIndex);

                String filePath = Environment.getExternalStorageDirectory()
                        + "/BeyondTPortal/surprise" + currentLevel + "/"
                        + fileName;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                imgView.setImageDrawable(ob);

            } else if (pIndex == 3) {

                int tempIndex = subIndex + 301;//storyoff3;

                String fileName = String.format("a%03d.png", tempIndex);

                String filePath = Environment.getExternalStorageDirectory()
                        + "/BeyondTPortal/surprise" + currentLevel + "/"
                        + fileName;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                imgView.setImageDrawable(ob);

            } else if (pIndex == 4) {

                int tempIndex = subIndex + 401;//storyoff4;

                String fileName = String.format("a%03d.png", tempIndex);

                String filePath = Environment.getExternalStorageDirectory()
                        + "/BeyondTPortal/surprise" + currentLevel + "/"
                        + fileName;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                imgView.setImageDrawable(ob);

            } else if (pIndex == 5) {
                int tempIndex = subIndex + 501;//storyoff5;

                String fileName = String.format("a%03d.png", tempIndex);

                String filePath = Environment.getExternalStorageDirectory()
                        + "/BeyondTPortal/surprise" + currentLevel + "/"
                        + fileName;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                imgView.setImageDrawable(ob);

            } else if (pIndex == 6) {

                int tempIndex = subIndex + 601;//storyoff6;

                String fileName = String.format("a%03d.png", tempIndex);

                String filePath = Environment.getExternalStorageDirectory()
                        + "/BeyondTPortal/surprise" + currentLevel + "/"
                        + fileName;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                imgView.setImageDrawable(ob);

            }

            imgView.setLayoutParams(lp);

            lp = new LinearLayout.LayoutParams(xpx, ypx);
            fLayout.addView(imgView);
            subItemLayout.addView(fLayout);

            subHScrollContainer.addView(subItemLayout);
        }
    }

    public LinearLayout subHScrollContainer() {
        return this.subHScrollContainer;
    }

    public void onHome(View v) {

//		MQTrainingActivity.mHistoryList.clear();
//		MQTrainingActivity.tabHost.setCurrentTab(0);
    }

    private void initBackDialog() {
        // TOOD : 다운로드 유무 확인 Dialog
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(quizActivity.this);
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // back_alert.show();
            finish();
        }
        return true;
    }
}
