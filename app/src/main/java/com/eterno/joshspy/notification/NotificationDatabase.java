package com.eterno.joshspy.notification;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {NotificationItem.class}, version = 2, exportSchema = false)
public abstract class NotificationDatabase extends RoomDatabase {

  public abstract NotificationDao notificationDao();

  private static volatile NotificationDatabase INSTANCE;
  private static final int NUMBER_OF_THREADS = 4;
  static final ExecutorService databaseWriteExecutor =
      Executors.newFixedThreadPool(NUMBER_OF_THREADS);

  static NotificationDatabase getDatabase(final Context context) {
    if (INSTANCE == null) {
      synchronized (NotificationDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
              NotificationDatabase.class, "word_database")
              .allowMainThreadQueries()
              .build();
        }
      }
    }
    return INSTANCE;
  }
}