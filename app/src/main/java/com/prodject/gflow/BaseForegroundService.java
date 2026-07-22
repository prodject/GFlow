package com.prodject.gflow;

import android.app.*;
import android.content.*;
import android.os.*;

public abstract class BaseForegroundService extends Service {
    protected abstract String title();
    protected abstract int notificationId();

    @Override public void onCreate() {
        super.onCreate();
        String channel = "gflow";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(channel, "GFlow", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, channel) : new Notification.Builder(this);
        startForeground(notificationId(), b.setContentTitle(title()).setContentText("GFlow работает в фоне").setSmallIcon(com.prodject.gflow.R.drawable.ic_gflow_logo).build());
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public android.os.IBinder onBind(Intent intent) { return null; }
}
