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

import com.eterno.joshspy.data.DataManager;
import com.eterno.joshspy.R;
import com.eterno.joshspy.data.AppItem;
import com.eterno.joshspy.data.SheetAppItem;
import com.eterno.joshspy.data.SheetEventItem;
import com.eterno.joshspy.helper.SendAppDataToSheet;
import com.eterno.joshspy.helper.SendDataToSheet;
import com.eterno.joshspy.helper.SendFGServiceDataToSheet;
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


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_back_up_data);
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
      Log.e(TAG, "first launch, backup from " + AppUtil.formatTimeStamp(cal.getTimeInMillis()));
    }
    new MyAsyncTask().execute(2);
  }


  private void getAndPostAppSpecificData(String mPackageName, SheetAppItem sheetAppItem) {
    List<AppItem> appItems = DataManager.getInstance().getTargetAppTimelineRange(this,
        mPackageName, lastUpdatedTime, timeNow);
    List<SheetEventItem> sNewList = new ArrayList<>();

    long prevFgTime = 0l;
    Log.e(TAG, "Processing " + mPackageName + "  " + (getPackageManager() == null));


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
    Log.d(TAG, "  " + mPackageName + "   " + json);
    new SendDataToSheet().execute(json);

  }

  @SuppressLint("StaticFieldLeak")
  class MyAsyncTask extends AsyncTask<Integer, Void, List<AppItem>> {

    @Override
    protected void onPreExecute() {
      HashMap<String, Integer> fgAppItems = DataManager.getInstance()
          .getAppsFGServiceRange(getApplicationContext(), lastUpdatedTime, timeNow);
      Log.e(TAG, "Foreground service  No of apps =  " + fgAppItems.size());

      for (String item : fgAppItems.keySet()) {
        Log.e(TAG , "Get events for Foreground service  " + item);
        if (validatePkgName(item)) {
          getAndPostFGAppSpecificData(item);
        }
      }
      Log.e(TAG, "Foreground service  completed");
    }

    @Override
    protected List<AppItem> doInBackground(Integer... integers) {
      return DataManager.getInstance().getAppsRange(getApplicationContext(), 2,
          lastUpdatedTime, timeNow);
    }

    @Override
    protected void onPostExecute(List<AppItem> appItems) {
      Log.e(TAG, "No of app events =  " + appItems.size());
      List<SheetEventItem> fgServiceNewList = new ArrayList<>();
      for (AppItem item : appItems) {
        if (item.mUsageTime <= 0) {
          continue;
        }
        if (validatePkgName(item.mPackageName)) {
          setNotiCountForApp(item.mPackageName);
        }
      }

      for (AppItem item : appItems) {
        if (item.mUsageTime <= 0) {
          continue;
        }
        if (validatePkgName(item.mPackageName)) {

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
        new SendAppDataToSheet().execute(json);
      }
      Log.e(TAG, "Backup completed");
      setLastUpdated(timeNow);
      goToMainActivity();
    }
  }

  private void setNotiCountForApp(String pkg) {
    AsyncTask.execute(() -> {
      int count = notificationRepo.getTotalNotificationsForApp(pkg);
      notiCountList.put(pkg, count);
    });
  }

  private boolean validatePkgName(String mPackageName) {
    switch (mPackageName) {
      case "in.mohalla.video":
      case "in.mohalla.video.lite":
      case "com.funnypuri.client":
      case "com.next.innovation.takatak":
      case "com.next.innovation.takatak.lite":
      case "video.tiki":
      case "com.instagram.android":
      case "com.facebook.katana":
      case "com.eterno.shortvideos":
      case "com.eterno":
        return true;
      default: return false;

    }
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
    new SendFGServiceDataToSheet().execute(FGSjson);
  }
}