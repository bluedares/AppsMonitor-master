package com.eterno.joshspy.app;

import android.app.Application;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import com.eterno.joshspy.data.DataManager;
import com.eterno.joshspy.AppConst;
import com.eterno.joshspy.BuildConfig;
import com.eterno.joshspy.data.AppItem;
import com.eterno.joshspy.db.DbHistoryExecutor;
import com.eterno.joshspy.db.DbIgnoreExecutor;
import com.eterno.joshspy.service.AppService;
import com.eterno.joshspy.util.AppUtil;
import com.eterno.joshspy.util.CrashHandler;
import com.eterno.joshspy.util.PreferenceManager;

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
