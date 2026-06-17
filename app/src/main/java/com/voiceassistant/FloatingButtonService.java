package com.voiceassistant;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.core.app.NotificationCompat;

public class FloatingButtonService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private SpeechManager speechManager;
    private static final String CHANNEL_ID = "voice_assistant_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification());
        speechManager = new SpeechManager(this);
        createFloatingButton();
    }

    private void createFloatingButton() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        floatingView = new View(this) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

                // Outer glow
                paint.setColor(Color.argb(60, 123, 47, 255));
                canvas.drawCircle(getWidth()/2f, getHeight()/2f,
                    getWidth()/2f, paint);

                // Main circle
                paint.setColor(Color.argb(220, 26, 26, 46));
                canvas.drawCircle(getWidth()/2f, getHeight()/2f,
                    getWidth()/2f - 8, paint);

                // Border
                paint.setColor(Color.argb(255, 123, 47, 255));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(4f);
                canvas.drawCircle(getWidth()/2f, getHeight()/2f,
                    getWidth()/2f - 8, paint);

                // Mic icon dot
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(getWidth()/2f, getHeight()/2f,
                    12, paint);
            }
        };

        floatingView.setOnClickListener(v -> {
            speechManager.startListening();
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            120, 120,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.x = 20;
        params.y = 200;

        // Make button draggable
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            int initialX, initialY;
            float initialTouchX, initialTouchY;
            long pressStartTime;
            boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        pressStartTime = System.currentTimeMillis();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - initialTouchX;
                        float dy = event.getRawY() - initialTouchY;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isDragging = true;
                            params.x = initialX - (int) dx;
                            params.y = initialY + (int) dy;
                            windowManager.updateViewLayout(floatingView, params);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging &&
                            System.currentTimeMillis() - pressStartTime < 300) {
                            speechManager.startListening();
                        }
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(floatingView, params);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription(getString(R.string.channel_desc));
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Assistant Active")
            .setContentText("Tap the floating button or say your wake word")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
        if (speechManager != null) speechManager.destroy();
    }
}
