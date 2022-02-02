package com.eterno.joshspy.data;

import com.eterno.joshspy.util.AppUtil;

/**
 * App Item
 * Created by AJAY
 */

public class SheetEventItem {
  public String mName;
  public String mPackageName;
  public String mEventTime;
  public String mUsageTime;
  public String mStartTime;


  public static SheetEventItem getSheetAppItem(AppItem item, long startTime) {
    if(startTime <= 0L || startTime > item.mEventTime){
      startTime = item.mEventTime;
    }
    SheetEventItem newItem = new SheetEventItem();
    newItem.mName = item.mName;
    newItem.mPackageName = item.mPackageName;
    newItem.mStartTime = AppUtil.formatTimeStamp(startTime);
    newItem.mEventTime = AppUtil.formatTimeStamp(item.mEventTime);
    newItem.mUsageTime = AppUtil.formatMilliSeconds(item.mUsageTime);

    return newItem;
  }
}
