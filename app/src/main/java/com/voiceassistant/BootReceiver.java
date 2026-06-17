package com.voiceassistant;

import android.content.*;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent floating = new Intent(context, FloatingButtonService.class);
            context.startForegroundService(floating);
            Intent wake = new Intent(context, WakeWordService.class);
            context.startForegroundService(wake);
        }
    }
}
