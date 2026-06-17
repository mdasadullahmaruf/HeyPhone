package com.voiceassistant;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOverlay      = findViewById(R.id.btnOverlay);
        Button btnAccessibility = findViewById(R.id.btnAccessibility);
        Button btnNotification  = findViewById(R.id.btnNotification);
        Button btnMic           = findViewById(R.id.btnMic);
        Button btnStart         = findViewById(R.id.btnStart);

        // Overlay permission
        btnOverlay.setOnClickListener(v -> {
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
            );
            startActivity(intent);
        });

        // Accessibility permission
        btnAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        // Notification listener permission
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(
                Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            );
            startActivity(intent);
        });

        // Microphone permission
        btnMic.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                Toast.makeText(this,
                    "Microphone already granted", Toast.LENGTH_SHORT).show();
            }
        });

        // Start assistant
        btnStart.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this,
                    "Please grant overlay permission first",
                    Toast.LENGTH_SHORT).show();
                return;
            }
            Intent service = new Intent(this, FloatingButtonService.class);
            startForegroundService(service);
            Intent wake = new Intent(this, WakeWordService.class);
            startForegroundService(wake);
            Toast.makeText(this,
                "Assistant started! You can close this screen.",
                Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
