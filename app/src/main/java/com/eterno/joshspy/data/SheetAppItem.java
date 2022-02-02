package com.eterno.joshspy.data;

import com.eterno.joshspy.util.AppUtil;

/**
 * Created by AJAY
 */

public class SheetAppItem {
  public String mName;
  public String mPackageName;
  public int mTimesOpened;
  public long mUsageTime;
  public int mFGServiceCount;
  public int mNotiCount;
  public long mMobile;
  public long mWifi;

  public SheetAppItem(String mName, String mPackageName, int mTimesOpened,
                      long mUsageTime, int mFGServiceCount, int mNotiCount, long mMobile,
                      long mWifi) {
    this.mName = mName;
    this.mPackageName = mPackageName;
    this.mTimesOpened = mTimesOpened;
    this.mUsageTime =  mUsageTime;
    this.mFGServiceCount = mFGServiceCount;
    this.mNotiCount = mNotiCount;
    this.mMobile = mMobile;
    this.mWifi = mWifi;
  }

}
