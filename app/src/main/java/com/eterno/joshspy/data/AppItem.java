package com.eterno.joshspy.data;

import java.util.Locale;

/**
 * App Item
 * Created by zb on 18/12/2017.
 */

public class AppItem {
    public String mName;
    public String mPackageName;
    public long mEventTime = 0L;
    public long mUsageTime = 0L;
    public int mEventType;
    public int mCount = 0;
    public long mMobile = 0L;
    public long mWifi = 0L;
    public boolean mCanOpen;
    private boolean mIsSystem;

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "name:%s package_name:%s time:%d total:%d type:%d system:%b count:%d",
                mName, mPackageName, mEventTime, mUsageTime, mEventType, mIsSystem, mCount);
    }

    public AppItem copy() {
        AppItem newItem = new AppItem();
        newItem.mName = this.mName;
        newItem.mPackageName = this.mPackageName;
        newItem.mEventTime = this.mEventTime;
        newItem.mUsageTime = this.mUsageTime;
        newItem.mEventType = this.mEventType;
        newItem.mIsSystem = this.mIsSystem;
        newItem.mCount = this.mCount;
        return newItem;
    }
}
