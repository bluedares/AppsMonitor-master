package com.eterno.appmonitor.notification;

import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface NotificationDao {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  void insertNotification(NotificationItem word);

  @Query("DELETE FROM notification_table")
  void deleteAllNotifications();

  @Query("SELECT * FROM notification_table ORDER BY pkey DESC ")
  LiveData<List<NotificationItem>> getNotifications();

  @Query("SELECT * FROM notification_table WHERE packageName = :pkgName ORDER BY pkey DESC")
  LiveData<List<NotificationItem>> getPkgNotifications(String pkgName);
}