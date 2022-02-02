package com.eterno.joshspy.data;

import com.eterno.joshspy.util.AppUtil;

import java.util.Locale;

/**
 * App Item
 * Created by zb on 18/12/2017.
 */

public class SheetAppItem {
  public String mName;
  public String mPackageName;
  public String mEventTime;
  public String mUsageTime;
  public String mStartTime;


  public static SheetAppItem getSheetAppItem(AppItem item, long startTime) {
    SheetAppItem newItem = new SheetAppItem();
    newItem.mName = item.mName;
    newItem.mPackageName = item.mPackageName;
    newItem.mStartTime = AppUtil.formatTimeStamp(startTime);
    newItem.mEventTime = AppUtil.formatTimeStamp(item.mEventTime);
    newItem.mUsageTime = AppUtil.formatMilliSeconds(item.mUsageTime);

    return newItem;
  }

}
