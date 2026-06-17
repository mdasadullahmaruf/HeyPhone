package com.voiceassistant;

import android.content.*;
import android.net.Uri;
import android.provider.Settings;
import java.util.*;

public class CommandExecutor {

    private Context context;
    private SpeechManager speech;

    private static final Map<String, String> APP_PACKAGES = new HashMap<>();
    static {
        APP_PACKAGES.put("google",      "com.google.android.googlequicksearchbox");
        APP_PACKAGES.put("youtube",     "com.google.android.youtube");
        APP_PACKAGES.put("chrome",      "com.android.chrome");
        APP_PACKAGES.put("gmail",       "com.google.android.gm");
        APP_PACKAGES.put("maps",        "com.google.android.apps.maps");
        APP_PACKAGES.put("camera",      "com.android.camera2");
        APP_PACKAGES.put("settings",    "com.android.settings");
        APP_PACKAGES.put("instagram",   "com.instagram.android");
        APP_PACKAGES.put("whatsapp",    "com.whatsapp");
        APP_PACKAGES.put("facebook",    "com.facebook.katana");
        APP_PACKAGES.put("twitter",     "com.twitter.android");
        APP_PACKAGES.put("spotify",     "com.spotify.music");
        APP_PACKAGES.put("netflix",     "com.netflix.mediaclient");
        APP_PACKAGES.put("photos",      "com.google.android.apps.photos");
        APP_PACKAGES.put("calculator",  "com.android.calculator2");
        APP_PACKAGES.put("calendar",    "com.google.android.calendar");
        APP_PACKAGES.put("clock",       "com.android.deskclock");
        APP_PACKAGES.put("contacts",    "com.android.contacts");
        APP_PACKAGES.put("messages",    "com.google.android.apps.messaging");
        APP_PACKAGES.put("phone",       "com.android.dialer");
        APP_PACKAGES.put("files",       "com.google.android.documentsui");
        APP_PACKAGES.put("drive",       "com.google.android.apps.docs");
        APP_PACKAGES.put("telegram",    "org.telegram.messenger");
        APP_PACKAGES.put("snapchat",    "com.snapchat.android");
        APP_PACKAGES.put("tiktok",      "com.zhiliaoapp.musically");
        APP_PACKAGES.put("play store",  "com.android.vending");
        APP_PACKAGES.put("playstore",   "com.android.vending");
    }

    public CommandExecutor(Context context, SpeechManager speech) {
        this.context = context;
        this.speech  = speech;
    }

    public void execute(String command) {

        // Read notifications
        if (command.contains("notification") || command.contains("what did i miss")) {
            readNotifications();

        // Read screen
        } else if (command.contains("what is on") || command.contains("read screen")
                || command.contains("what's on my screen")) {
            readScreen();

        // Open app
        } else if (command.startsWith("open ") || command.startsWith("launch ")) {
            String appName = command.replace("open ", "").replace("launch ", "").trim();
            openApp(appName);

        // Go back
        } else if (command.equals("go back") || command.equals("back")) {
            VoiceAccessibilityService.performBack();
            speech.speak("Going back");

        // Go home
        } else if (command.equals("go home") || command.equals("home")) {
            VoiceAccessibilityService.performHome();
            speech.speak("Going to home screen");

        // Scroll down
        } else if (command.contains("scroll down")) {
            VoiceAccessibilityService.performScrollDown();
            speech.speak("Scrolling down");

        // Scroll up
        } else if (command.contains("scroll up")) {
            VoiceAccessibilityService.performScrollUp();
            speech.speak("Scrolling up");

        // Tap something
        } else if (command.startsWith("tap ") || command.startsWith("click ")) {
            String target = command.replace("tap ", "").replace("click ", "").trim();
            tapElement(target);

        // Call someone
        } else if (command.startsWith("call ")) {
            String name = command.replace("call ", "").trim();
            makeCall(name);

        // Search in YouTube
        } else if (command.contains("youtube") && command.contains("search")) {
            String query = command.replace("open youtube and search", "")
                                  .replace("youtube search", "")
                                  .replace("search on youtube", "")
                                  .trim();
            searchYouTube(query);

        // Google search
        } else if (command.startsWith("search ")) {
            String query = command.replace("search ", "").trim();
            googleSearch(query);

        // Unknown
        } else {
            speech.speak("I did not understand that. Please try again.");
        }
    }

    private void openApp(String appName) {
        String pkg = APP_PACKAGES.get(appName.toLowerCase());
        if (pkg != null) {
            Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(pkg);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                speech.speak("Opening " + appName);
            } else {
                speech.speak(appName + " is not installed on your phone");
            }
        } else {
            speech.speak("I don't know how to open " + appName);
        }
    }

    private void searchYouTube(String query) {
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        intent.putExtra("query", query);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        speech.speak("Searching YouTube for " + query);
    }

    private void googleSearch(String query) {
        Uri uri = Uri.parse("https://www.google.com/search?q=" +
            Uri.encode(query));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        speech.speak("Searching Google for " + query);
    }

    private void makeCall(String name) {
        Uri uri = Uri.parse("tel:" + name);
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        speech.speak("Calling " + name);
    }

    private void tapElement(String target) {
        boolean found = VoiceAccessibilityService.tapNodeByText(target);
        if (found) {
            speech.speak("Tapped " + target);
        } else {
            speech.speak("I could not find " + target + " on screen");
        }
    }

    private void readNotifications() {
        List<String> notes = NotificationListener.getNotifications();
        if (notes.isEmpty()) {
            speech.speak("You have no notifications right now");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("You have ").append(notes.size()).append(" notifications. ");
            for (String n : notes) sb.append(n).append(". ");
            speech.speak(sb.toString());
        }
    }

    private void readScreen() {
        String screenText = VoiceAccessibilityService.readScreenContent();
        if (screenText == null || screenText.isEmpty()) {
            speech.speak("I cannot read the current screen");
        } else {
            speech.speak("On your screen I can see: " + screenText);
        }
    }
}
