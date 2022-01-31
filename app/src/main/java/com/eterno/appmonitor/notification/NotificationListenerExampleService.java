package com.eterno.appmonitor.notification;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.eterno.appmonitor.notification.NotificationRepo;
import com.eterno.appmonitor.util.AppUtil;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.RequiresApi;

/**
 * MIT License
 *
 *  Copyright (c) 2016 Fábio Alves Martins Pereira (Chagall)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class NotificationListenerExampleService extends NotificationListenerService {
    private NotificationRepo mRepository = new NotificationRepo(AppUtil.getApplication());

    /*
        These are the package names of the apps. for which we want to
        listen the notifications
     */
    private static final class ApplicationPackageNames {
        public static final String FACEBOOK_PACK_NAME = "com.facebook.katana";
        public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
        public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
        public static final int FACEBOOK_CODE = 1;
        public static final int WHATSAPP_CODE = 2;
        public static final int INSTAGRAM_CODE = 3;
        public static final int OTHER_NOTIFICATIONS_CODE = 4; // We ignore all notification with code == 4
    }

    public static StatusBarNotification notificationReceived;

    public static final String APP_PACKAGE_NAME = "com.eterno.appmonitor";
    public static final String NOTIFICATION_DATA = "NotificationData";
    public static final String TAG = "NotificationAlert";

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        Log.d(TAG, "onNotificationPosted : " + sbn.getId() + " pkg: " + sbn.getPackageName());
        Log.d(TAG, "onNotificationPosted : " + sbn.toString());


        String date =
            java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        NotificationItem notificationItem = new NotificationItem(sbn.getId(),
            sbn.getPackageName(), sbn.getTag(), sbn.getKey(), sbn.getGroupKey(), date);
        notificationItem.setPosted(true);
        Intent intent = new  Intent(APP_PACKAGE_NAME);
        intent.putExtra(NOTIFICATION_DATA, notificationItem);
        sendBroadcast(intent);
        mRepository.insert(notificationItem);

//        int notificationCode = matchNotificationCode(sbn);
//
//        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE){
//            Intent intent = new  Intent(APP_PACKAGE_NAME);
//            intent.putExtra("Notification Code", notificationCode);
//            sendBroadcast(intent);
//        }
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Log.d(TAG, "onNotificationRemoved : " + sbn.getId() + " pkg: " + sbn.getPackageName());
//        NotificationItem notificationItem = new NotificationItem(sbn.getId(),
//            sbn.getPackageName(), sbn.getTag(), sbn.getKey(), sbn.getGroupKey());
//        notificationItem.setRemoved(true);
//        Intent intent = new  Intent(APP_PACKAGE_NAME);
//        intent.putExtra(NOTIFICATION_DATA, notificationItem);
//        sendBroadcast(intent);

//        int notificationCode = matchNotificationCode(sbn);
//
//        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {
//
//            StatusBarNotification[] activeNotifications = this.getActiveNotifications();
//
//            if(activeNotifications != null && activeNotifications.length > 0) {
//                for (int i = 0; i < activeNotifications.length; i++) {
//
//                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
//                        Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
//                        intent.putExtra("Notification Code", notificationCode);
//                        sendBroadcast(intent);
//                        break;
//                    }
//                }
//            }
//        }
    }

//    private int matchNotificationCode(StatusBarNotification sbn) {
//        String packageName = sbn.getPackageName();
//        if(packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
//                || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)){
//            return(InterceptedNotificationCode.FACEBOOK_CODE);
//        }
//        else if(packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)){
//            return(InterceptedNotificationCode.INSTAGRAM_CODE);
//        }
//        else if(packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)){
//            return(InterceptedNotificationCode.WHATSAPP_CODE);
//        }
//        else{
//            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
//        }
//    }
}
