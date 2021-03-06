package de.lisemeitnerschule.liseapp;

import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Pascal on 23.3.15.
 */
public class Utilities {
    private static int primaryDark;


    public static long generateTimeStamp() {
        return System.currentTimeMillis() / 1000L;
    }
    public static void setDefaultStaticStatusBarColor(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if(primaryDark == 0)
                primaryDark = activity.getResources().getColor(R.color.LiseBlueDarker);
            window.setStatusBarColor(primaryDark);
        }
    }
    public static void setStaticStatusBarColor(int id,Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(id));
        }
    }
    public static void removeStaticStatusBarColor(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        }
    }
}