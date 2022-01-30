package com.eterno.appmonitor.app;

import android.app.Application;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import com.eterno.appmonitor.AppConst;
import com.eterno.appmonitor.BuildConfig;
import com.eterno.appmonitor.data.AppItem;
import com.eterno.appmonitor.data.DataManager;
import com.eterno.appmonitor.db.DbHistoryExecutor;
import com.eterno.appmonitor.db.DbIgnoreExecutor;
import com.eterno.appmonitor.service.AppService;
import com.eterno.appmonitor.util.AppUtil;
import com.eterno.appmonitor.util.CrashHandler;
import com.eterno.appmonitor.util.PreferenceManager;

/**
 * My Application
 * Created by zb on 18/12/2017.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.init(this);
        AppUtil.setApplication(this);
        getApplicationContext().startService(new Intent(getApplicationContext(), AppService.class));
        DbIgnoreExecutor.init(getApplicationContext());
        DbHistoryExecutor.init(getApplicationContext());
        DataManager.init();
        addDefaultIgnoreAppsToDB();
        if (AppConst.CRASH_TO_FILE) CrashHandler.getInstance().init();
    }

    private void addDefaultIgnoreAppsToDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> mDefaults = new ArrayList<>();
                mDefaults.add("com.android.settings");
                mDefaults.add(BuildConfig.APPLICATION_ID);
                for (String packageName : mDefaults) {
                    AppItem item = new AppItem();
                    item.mPackageName = packageName;
                    item.mEventTime = System.currentTimeMillis();
                    DbIgnoreExecutor.getInstance().insertItem(item);
                }
            }
        }).run();
    }
}
