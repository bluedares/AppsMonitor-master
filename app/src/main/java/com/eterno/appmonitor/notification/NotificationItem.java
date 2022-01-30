package com.eterno.appmonitor.notification;

import java.io.Serializable;

public class NotificationItem implements Serializable {
  private int id;
  private String packageName;
  private String tag;
  private String key;
  private String groupKey;
  private boolean isPosted;
  private boolean isRemoved;

  public NotificationItem(int id, String packageName, String tag, String key, String groupKey) {
    this.id = id;
    this.packageName = packageName;
    this.tag = tag;
    this.key = key;
    this.groupKey = groupKey;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getGroupKey() {
    return groupKey;
  }

  public void setGroupKey(String groupKey) {
    this.groupKey = groupKey;
  }

  public boolean isPosted() {
    return isPosted;
  }

  public void setPosted(boolean posted) {
    isPosted = posted;
  }

  public boolean isRemoved() {
    return isRemoved;
  }

  public void setRemoved(boolean removed) {
    isRemoved = removed;
  }
}
