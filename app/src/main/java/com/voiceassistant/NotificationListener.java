package com.voiceassistant;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.*;

public class NotificationListener extends NotificationListenerService {

    private static List<String> notifications = new ArrayList<>();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String app  = sbn.getPackageName();
        String title = "";
        String text  = "";

        if (sbn.getNotification().extras != null) {
            CharSequence t =
                sbn.getNotification().extras.getCharSequence("android.title");
            CharSequence b =
                sbn.getNotification().extras.getCharSequence("android.text");
            if (t != null) title = t.toString();
            if (b != null) text  = b.toString();
        }

        String friendly = getFriendlyAppName(app);
        String entry = friendly + " from " + title + " — " + text;

        // Keep last 10 notifications
        notifications.add(0, entry);
        if (notifications.size() > 10) {
            notifications.remove(notifications.size() - 1);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}

    public static List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    private String getFriendlyAppName(String pkg) {
        if (pkg.contains("whatsapp"))   return "WhatsApp";
        if (pkg.contains("gmail"))      return "Gmail";
        if (pkg.contains("instagram"))  return "Instagram";
        if (pkg.contains("youtube"))    return "YouTube";
        if (pkg.contains("telegram"))   return "Telegram";
        if (pkg.contains("twitter"))    return "Twitter";
        if (pkg.contains("facebook"))   return "Facebook";
        if (pkg.contains("messaging"))  return "Messages";
        return pkg;
    }
}
