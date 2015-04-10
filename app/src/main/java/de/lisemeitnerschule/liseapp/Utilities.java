package de.lisemeitnerschule.liseapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Pascal on 23.3.15.
 */
public class Utilities {
    public static long generateTimeStamp() {
        return System.currentTimeMillis() / 1000L;
    }

    public static void updateColor(int id,Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(id));
        }
    }
}