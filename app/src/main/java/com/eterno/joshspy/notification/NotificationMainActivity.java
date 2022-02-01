package com.eterno.joshspy.notification;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.eterno.joshspy.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
public class NotificationMainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private static final String NOTIFICATION_TITLE = "Notification Title";

    private RecyclerView notificationList;
    private TextView title;
    private ImageChangeBroadcastReceiver imageChangeBroadcastReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;
    private NotificationAdapter notificationAdapter;
    private String appPkgName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getArguments();
        setContentView(R.layout.activity_notification_main);

        notificationAdapter = new NotificationAdapter(this, (appPkgName == null));
        notificationList
                = (RecyclerView) this.findViewById(R.id.notification_list);

        title = ((TextView) findViewById(R.id.notification_title));
        if(appPkgName != null ){
            ((TextView) findViewById(R.id.notification_package)).setText(appPkgName);
            ((TextView) findViewById(R.id.notification_package)).setVisibility(View.VISIBLE);
        }
        notificationList.setLayoutManager(new LinearLayoutManager(this));
        notificationList.setAdapter(notificationAdapter);
        setNotificationsObserver();
        // If the user did not turn the notification listener service on we prompt him to do so
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        // Finally we register a receiver to tell the MainActivity when a notification has been received
        imageChangeBroadcastReceiver = new ImageChangeBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationListenerExampleService.APP_PACKAGE_NAME);
        registerReceiver(imageChangeBroadcastReceiver,intentFilter);
    }

    private void getArguments() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appPkgName = extras.getString("package_name");
        }
    }

    private void setNotificationsObserver() {
        NotificationRepo notificationRepo = new NotificationRepo(this.getApplication());
         if(appPkgName != null && !appPkgName.equals("")) {
            notificationRepo.getPkgAllNotifications(appPkgName).observe(this, notificationItems -> {
                if (notificationItems == null || notificationItems.size() == 0) {
                    Toast.makeText(this, "No Notifications to show", Toast.LENGTH_SHORT).show();
                    return;
                }
                notificationAdapter.setNotificationList(notificationItems);

                title.setText(getResources().getString(R.string.notification_list_title,
                    String.valueOf(notificationAdapter.getItemCount())));
            });
        } else {
            notificationRepo.getmAllNotifications().observe(this, notificationItems -> {
                notificationAdapter.setNotificationList(notificationItems);
                title.setText(getResources().getString(R.string.notification_list_title,
                    String.valueOf(notificationAdapter.getItemCount())));
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(imageChangeBroadcastReceiver);
    }

//    /**
//     * Change Intercepted Notification Image
//     * Changes the MainActivity image based on which notification was intercepted
//     * @param notificationCode The intercepted notification code
//     */
//    private void changeInterceptedNotificationImage(int notificationCode){
//        switch(notificationCode){
//            case NotificationListenerExampleService.InterceptedNotificationCode.FACEBOOK_CODE:
//                interceptedNotificationImageView.setImageResource(R.drawable.facebook_logo);
//                break;
//            case NotificationListenerExampleService.InterceptedNotificationCode.INSTAGRAM_CODE:
//                interceptedNotificationImageView.setImageResource(R.drawable.instagram_logo);
//                break;
//            case NotificationListenerExampleService.InterceptedNotificationCode.WHATSAPP_CODE:
//                interceptedNotificationImageView.setImageResource(R.drawable.whatsapp_logo);
//                break;
//            case NotificationListenerExampleService.InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE:
//                interceptedNotificationImageView.setImageResource(R.drawable.other_notification_logo);
//                break;
//        }
//    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if enabled, false otherwise.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Image Change Broadcast Receiver.
     * We use this Broadcast Receiver to notify the Main Activity when
     * a new notification has arrived, so it can properly change the
     * notification image
     * */
    public class ImageChangeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                NotificationItem notification =
                    (NotificationItem) intent.getSerializableExtra(NotificationListenerExampleService.NOTIFICATION_DATA);
                if (notification != null) {
                  //  notificationAdapter.addNotification(notification);
                    Toast.makeText(context, "receivedNotification : " + notification.getPackageName(),
                        Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Notification NULL", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e) {
                Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //changeInterceptedNotificationImage(receivedNotificationCode);
        }
    }


    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }
}
