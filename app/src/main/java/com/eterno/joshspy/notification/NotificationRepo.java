package com.eterno.joshspy.notification;

import android.app.Application;
import android.util.Log;

import java.util.List;

import androidx.lifecycle.LiveData;

public class NotificationRepo {

  private NotificationDao notificationDao;
  private LiveData<List<NotificationItem>> mAllNotifications;

  public NotificationRepo(Application application) {
    if (application != null) {
      NotificationDatabase db = NotificationDatabase.getDatabase(application);
      notificationDao = db.notificationDao();
      mAllNotifications = notificationDao.getNotifications();
    }
  }

  LiveData<List<NotificationItem>> getmAllNotifications() {
    return mAllNotifications;
  }

  LiveData<List<NotificationItem>> getPkgAllNotifications(String pkg) {
    return notificationDao.getPkgNotifications(pkg);
  }

  public int getTotalNotificationsForApp(String pkg) {
    return notificationDao.getTotalNotificationsForApp(pkg);
  }

  void insert(NotificationItem notificationItem) {
    NotificationDatabase.databaseWriteExecutor.execute(() -> {
      notificationDao.insertNotification(notificationItem);
    });
  }
}
