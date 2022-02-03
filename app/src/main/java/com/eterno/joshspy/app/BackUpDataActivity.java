package com.eterno.joshspy.app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.eterno.joshspy.data.DataManager;
import com.eterno.joshspy.R;
import com.eterno.joshspy.data.AppItem;
import com.eterno.joshspy.data.SheetAppItem;
import com.eterno.joshspy.data.SheetEventItem;
import com.eterno.joshspy.helper.SendDataToSheet;
import com.eterno.joshspy.notification.NotificationRepo;
import com.eterno.joshspy.ui.MainActivity;
import com.eterno.joshspy.util.AppUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class BackUpDataActivity extends AppCompatActivity {

  long lastUpdatedTime = 0L;
  long timeNow = System.currentTimeMillis();
  private ArrayList<SheetAppItem> sheetAppItems = new ArrayList<SheetAppItem>();
  private NotificationRepo notificationRepo;

  private static final String LAST_UPDATED_TIME = "last_updated_time";
  private static final String TAG = "BackUpDataActivity";
  HashMap<String, Integer> notiCountList = new HashMap<>();
  HashMap<String, Integer> fgServiceCount = new HashMap<>();
  private SharedPreferences sharedPref;

  private TextView mProgress;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_back_up_data);
    mProgress = findViewById(R.id.progress);
    sharedPref = this.getPreferences(Context.MODE_PRIVATE);
    sharedPref.getLong(LAST_UPDATED_TIME, 0L);
    notificationRepo = new NotificationRepo(this.getApplication());
    lastUpdatedTime = getLastUpdated();
    if (lastUpdatedTime == 0L) {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH, 1);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      Log.d(TAG, "first launch, backup from " + AppUtil.formatTimeStamp(cal.getTimeInMillis()));
      lastUpdatedTime = cal.getTimeInMillis();
    }
    new MyAsyncTask().execute(2);
  }


  private void getAndPostAppSpecificData(String mPackageName, SheetAppItem sheetAppItem) {
    List<AppItem> appItems = DataManager.getInstance().getTargetAppTimelineRange(this,
        mPackageName, lastUpdatedTime, timeNow);
    if (appItems == null || appItems.size() == 0) {
      return;
    }
    List<SheetEventItem> sNewList = new ArrayList<>();

    long prevFgTime = 0l;
    setProgress("AE - " + appItems.get(0).mName);
    Log.d(TAG, "Processing " + mPackageName + "  " + (getPackageManager() == null));


    for (AppItem item : appItems) {

      if (item.mEventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
        prevFgTime = item.mEventTime;
      } else if (item.mEventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
        sNewList.add(SheetEventItem.getSheetAppItem(item, prevFgTime));
      }
    }
    sheetAppItem.mTimesOpened = sNewList.size();
    sheetAppItems.add(sheetAppItem);

    Gson gson = new Gson();
    String json = gson.toJson(sNewList);
    Log.d(TAG, "send data size - " + mPackageName + "   " +sNewList.size());
    Log.d(TAG, "  " + mPackageName + "   " + json);
    try {
      new SendDataToSheet("OPEN").execute(json, mPackageName);
    } catch (Exception e) {
      Log.e(TAG, "errror open" + e.getMessage());
    }
  }

  private void setProgress(String progress) {
    mProgress.post(() -> {
      String str = mProgress.getText().toString();
      mProgress.setText(str + "\n" + progress);
    });
  }

  @SuppressLint("StaticFieldLeak")
  class MyAsyncTask extends AsyncTask<Integer, Void, List<AppItem>> {

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected List<AppItem> doInBackground(Integer... integers) {
      setProgress("**********    Fetching Foreground services    **********");
      HashMap<String, Integer> fgAppItems = DataManager.getInstance()
          .getAppsFGServiceRange(getApplicationContext(), lastUpdatedTime, timeNow);
      Log.d(TAG, "Foreground service  No of apps =  " + fgAppItems.size());

      //setProgress("Fetching FG services No of apps = " + fgAppItems.size());
      for (String item : fgAppItems.keySet()) {
        Log.d(TAG, "Get events for Foreground service  " + item);
        if (AppUtil.validatePkgName(item)) {
          setProgress("FG - " + item);
          getAndPostFGAppSpecificData(item);
        }
      }
      setProgress("Fetching FG services Completed");
      Log.d(TAG, "Foreground service  completed");
      return DataManager.getInstance().getAppsRange(getApplicationContext(), 2,
          lastUpdatedTime, timeNow);
    }

    @Override
    protected void onPostExecute(List<AppItem> appItems) {

      setProgress("**********     Fetching App  Data      **********");
      if(appItems == null || appItems.size() == 0){
        goToMainActivity();
        Log.e(TAG , "size 0 return to main ");
        return;
      }
      Log.d(TAG, "No of app events =  " + appItems.size());
     // setProgress("Fetching App  Events  " + appItems.size()+"");
      for (AppItem item : appItems) {
        if (item.mUsageTime <= 0) {
          continue;
        }
        if (AppUtil.validatePkgName(item.mPackageName)) {
          setNotiCountForApp(item.mPackageName);
        }
      }

      for (AppItem item : appItems) {
        if (item.mUsageTime <= 0) {
          continue;
        }
        if (AppUtil.validatePkgName(item.mPackageName)) {

          int mNotiCount = 0;
          if (notiCountList.containsKey(item.mPackageName)) {
            mNotiCount = notiCountList.get(item.mPackageName);
          }
          int mFgServiceCunt = 0;
          if (fgServiceCount.containsKey(item.mPackageName)) {
            mFgServiceCunt = fgServiceCount.get(item.mPackageName);
          }

          SheetAppItem sheetAppItem = new SheetAppItem(
              item.mName,
              item.mPackageName,
              0,
              item.mUsageTime,
              mFgServiceCunt,
              mNotiCount,
              item.mMobile,
              item.mWifi
          );
          getAndPostAppSpecificData(item.mPackageName, sheetAppItem);
        }
      }


      if (sheetAppItems.size() > 0) {
        Gson gson = new Gson();
        String json = gson.toJson(sheetAppItems);
        Log.d(TAG, "DDDD Updating sheet app items   " + json);
        try {
          setProgress("********   Sending Total App usage data   ********");
          new SendDataToSheet("USAGE").execute(json);
        } catch (Exception e) {
          Log.e(TAG, "error usage" + e.getMessage());
        }
      } else {
        setLastUpdated(timeNow);
        goToMainActivity();
      }
      Log.d(TAG, "Backup completed");
      setLastUpdated(timeNow);

    }
  }

  private void setNotiCountForApp(String pkg) {
    AsyncTask.execute(() -> {
      int count = notificationRepo.getTotalNotificationsForApp(pkg);
      notiCountList.put(pkg, count);
    });
  }


  private void goToMainActivity() {
    Intent i = new Intent(BackUpDataActivity.this, MainActivity.class);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(i);
  }


  private long getLastUpdated() {
    return sharedPref.getLong(LAST_UPDATED_TIME, 0L);
  }

  private void setLastUpdated(Long time) {
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putLong(LAST_UPDATED_TIME, time);
    editor.apply();
  }

  private void getAndPostFGAppSpecificData(String mPackageName) {
    List<AppItem> appItems = DataManager.getInstance().getTargetAppFGTimelineRange(this,
        mPackageName, lastUpdatedTime, timeNow);
    if (appItems == null || appItems.size() == 0) {
      return;
    }
    List<SheetEventItem> sNewList = new ArrayList<>();

    long prevFgTime = 0l;
    for (AppItem item : appItems) {
      if (item.mEventType == UsageEvents.Event.FOREGROUND_SERVICE_START) {
        prevFgTime = item.mEventTime;
      } else if (item.mEventType == UsageEvents.Event.FOREGROUND_SERVICE_STOP) {
        sNewList.add(SheetEventItem.getSheetAppItem(item, prevFgTime));
      }
    }
    fgServiceCount.put(mPackageName, sNewList.size());
    Gson gson = new Gson();
    String FGSjson = gson.toJson(sNewList);
    try {
      Log.e(TAG, "Send Foreground data" + FGSjson);
      new SendDataToSheet("SERVICE").execute(FGSjson);
    } catch (Exception e) {
      Log.e(TAG, "error usage" + e.getMessage());
    }
  }
}