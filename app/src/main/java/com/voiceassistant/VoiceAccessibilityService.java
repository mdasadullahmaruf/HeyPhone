package com.voiceassistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;

public class VoiceAccessibilityService extends AccessibilityService {

    private static VoiceAccessibilityService instance;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // We use this passively to keep screen state updated
    }

    @Override
    public void onInterrupt() {}

    // Go back
    public static void performBack() {
        if (instance != null)
            instance.performGlobalAction(GLOBAL_ACTION_BACK);
    }

    // Go home
    public static void performHome() {
        if (instance != null)
            instance.performGlobalAction(GLOBAL_ACTION_HOME);
    }

    // Scroll down
    public static void performScrollDown() {
        if (instance == null) return;
        Path path = new Path();
        path.moveTo(540, 1400);
        path.lineTo(540, 600);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
        instance.dispatchGesture(builder.build(), null, null);
    }

    // Scroll up
    public static void performScrollUp() {
        if (instance == null) return;
        Path path = new Path();
        path.moveTo(540, 600);
        path.lineTo(540, 1400);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
        instance.dispatchGesture(builder.build(), null, null);
    }

    // Tap element by text
    public static boolean tapNodeByText(String text) {
        if (instance == null) return false;
        AccessibilityNodeInfo root =
            instance.getRootInActiveWindow();
        if (root == null) return false;
        List<AccessibilityNodeInfo> nodes =
            root.findAccessibilityNodeInfosByText(text);
        if (nodes != null && !nodes.isEmpty()) {
            AccessibilityNodeInfo node = nodes.get(0);
            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);
            float cx = bounds.centerX();
            float cy = bounds.centerY();
            Path path = new Path();
            path.moveTo(cx, cy);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(
                new GestureDescription.StrokeDescription(path, 0, 50));
            instance.dispatchGesture(builder.build(), null, null);
            return true;
        }
        return false;
    }

    // Read screen content
    public static String readScreenContent() {
        if (instance == null) return null;
        AccessibilityNodeInfo root = instance.getRootInActiveWindow();
        if (root == null) return null;
        StringBuilder sb = new StringBuilder();
        collectText(root, sb);
        return sb.toString().trim();
    }

    private static void collectText(AccessibilityNodeInfo node,
                                    StringBuilder sb) {
        if (node == null) return;
        if (node.getText() != null && node.getText().length() > 0) {
            sb.append(node.getText()).append(" ");
        }
        if (node.getContentDescription() != null) {
            sb.append(node.getContentDescription()).append(" ");
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectText(node.getChild(i), sb);
        }
    }
}
