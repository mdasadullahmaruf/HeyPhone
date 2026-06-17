package com.voiceassistant;

import android.app.*;
import android.content.*;
import android.os.*;
import android.speech.*;
import androidx.core.app.NotificationCompat;
import java.util.*;

public class WakeWordService extends Service {

    private SpeechRecognizer recognizer;
    private static final String WAKE_WORD = "hey phone";
    private static final String CHANNEL_ID = "wake_word_channel";
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isListening = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(2, buildNotification());
        startWakeWordListening();
    }

    private void startWakeWordListening() {
        handler.post(() -> {
            if (isListening) return;
            isListening = true;

            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches =
                        results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null) {
                        for (String match : matches) {
                            if (match.toLowerCase().contains(WAKE_WORD)) {
                                onWakeWordDetected();
                                break;
                            }
                        }
                    }
                    isListening = false;
                    // Restart listening after 500ms
                    handler.postDelayed(() ->
                        startWakeWordListening(), 500);
                }

                @Override
                public void onError(int error) {
                    isListening = false;
                    // Restart after error
                    handler.postDelayed(() ->
                        startWakeWordListening(), 1000);
                }

                @Override public void onReadyForSpeech(Bundle p) {}
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float v) {}
                @Override public void onBufferReceived(byte[] b) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onPartialResults(Bundle b) {}
                @Override public void onEvent(int t, Bundle b) {}
            });

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            recognizer.startListening(intent);
        });
    }

    private void onWakeWordDetected() {
        // Trigger the floating button's speech manager
        Intent i = new Intent("com.voiceassistant.WAKE_WORD_DETECTED");
        sendBroadcast(i);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID, "Wake Word Listener",
            NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Listening for Hey Phone")
            .setContentText("Say 'Hey Phone' to activate")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recognizer != null) recognizer.destroy();
    }
}
