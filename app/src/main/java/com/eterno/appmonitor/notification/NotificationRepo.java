package com.eterno.appmonitor.notification;

import android.app.Application;
import android.util.Log;

import java.util.List;

import androidx.lifecycle.LiveData;

class NotificationRepo {

  private NotificationDao notificationDao;
  private LiveData<List<NotificationItem>> mAllNotifications;

  NotificationRepo(Application application) {
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

  void insert(NotificationItem notificationItem) {
    Log.d("DDDD",
        notificationItem.getTime()+"Notification item insert "+notificationItem.getPackageName());
    NotificationDatabase.databaseWriteExecutor.execute(() -> {
      notificationDao.insertNotification(notificationItem);
    });
  }
}
