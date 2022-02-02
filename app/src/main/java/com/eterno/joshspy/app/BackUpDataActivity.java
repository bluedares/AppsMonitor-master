package com.eterno.joshspy.app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.eterno.joshspy.R;
import com.eterno.joshspy.data.AppItem;
import com.eterno.joshspy.data.DataManager;
import com.eterno.joshspy.data.SheetAppItem;
import com.eterno.joshspy.helper.SendDataToSheet;
import com.eterno.joshspy.service.AppService;
import com.eterno.joshspy.ui.MainActivity;
import com.eterno.joshspy.util.AppUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BackUpDataActivity extends AppCompatActivity {

  long lastUpdatedTime;
  long timeNow = System.currentTimeMillis();
  private boolean isFirstBackup = true;

  private static final String LAST_UPDATED_TIME = "last_updated_time";
  private static final String TAG = "BackUpDataActivity";

  private boolean isAllPermissionsGiven = false;
  private SharedPreferences sharedPref;

  private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_back_up_data);
    sharedPref = this.getPreferences(Context.MODE_PRIVATE);
    lastUpdatedTime = getLastUpdated();
    if(lastUpdatedTime == 0L) {
      isFirstBackup = true;
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH, 1);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      Log.e(TAG, "first launch, backup from "+AppUtil.formatTimeStamp(cal.getTimeInMillis()));
    }

    new MyAsyncTask().execute(2);
    }


  private void getAndPostAppSpecificData(String mPackageName) {
    List<AppItem> appItems = DataManager.getInstance().getTargetAppTimelineRange(this,
        mPackageName, lastUpdatedTime, timeNow);
    List<SheetAppItem> sNewList = new ArrayList<>();
    long prevFgTime = 0l;
    Log.e(TAG, "Processing "+mPackageName);
    for (AppItem item : appItems) {
      if (item.mEventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
        prevFgTime = item.mEventTime;
      }
      if (item.mEventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
        sNewList.add(SheetAppItem.getSheetAppItem(item, prevFgTime));
      }
    }

    SendDataToSheet sendDataToSheet = new SendDataToSheet();
    Gson gson = new Gson();
    String json = gson.toJson(sNewList);
    Log.d(TAG, "  "+mPackageName+"   "+json);
    sendDataToSheet.execute(json);
  }

  @SuppressLint("StaticFieldLeak")
  class MyAsyncTask extends AsyncTask<Integer, Void, List<AppItem>> {

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected List<AppItem> doInBackground(Integer... integers) {
      return DataManager.getInstance().getAppsRange(getApplicationContext(), 2,
         lastUpdatedTime, timeNow);
    }

    @Override
    protected void onPostExecute(List<AppItem> appItems) {
      Log.e(TAG, "No of apps =  "+appItems.size());
      for (AppItem item : appItems) {
        if (item.mUsageTime <= 0) continue;
        item.mCanOpen = getPackageManager().getLaunchIntentForPackage(item.mPackageName) != null;
        if(!isFirstBackup || validatePkgName(item.mPackageName)){
          getAndPostAppSpecificData(item.mPackageName);
        } else{
          Log.d(TAG," skip package "+item.mPackageName);
        }
      }
      Log.e(TAG,"Backup completed" );;
      setLastUpdated(timeNow);
      goToMainActivity();
    }
  }

  private boolean validatePkgName(String mPackageName) {
    switch (mPackageName){
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


  private boolean isNotificationServiceEnabled(){
    String pkgName = getPackageName();
    final String flat = Settings.Secure.getString(getContentResolver(),
        ENABLED_NOTIFICATION_LISTENERS);
    if (!TextUtils.isEmpty(flat)) {
      final String[] names = flat.split(":");
      for (int i = 0; i < names.length; i++) {
        final ComponentName cn = ComponentName.unflattenFromString(names[i]);
        if (cn != null) {
          if (TextUtils.equals(pkgName, cn.getPackageName())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean hasAllPermissions(){
    return (isJoshAppEnabled() && isNotificationServiceEnabled());
  }

  private boolean isJoshAppEnabled(){
    return (DataManager.getInstance().hasPermission(getApplicationContext()));
  }

  private long getLastUpdated() {
    return sharedPref.getLong(LAST_UPDATED_TIME, 0L);
  }

  private void setLastUpdated(Long time) {
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putLong(LAST_UPDATED_TIME, time);
    editor.apply();
  }


}