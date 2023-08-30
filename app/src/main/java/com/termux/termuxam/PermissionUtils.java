package com.termux.termuxam;

import static com.termux.termuxam.Am.LOG_TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.os.ServiceManager;
import android.util.Log;

import com.android.internal.app.IAppOpsService;

import java.lang.reflect.InvocationTargetException;

public class PermissionUtils {

    @SuppressWarnings({"JavaReflectionMemberAccess", "SameParameterValue", "deprecation"})
    @SuppressLint("PrivateApi")
    static boolean checkPermission(String op) {
        try {
            // We do not need Context to use checkOpNoThrow/unsafeCheckOpNoThrow so it can be null
            IBinder binder = ServiceManager.getService("appops");
            IAppOpsService service = IAppOpsService.Stub.asInterface(binder);
            AppOpsManager appops = (AppOpsManager) Class.forName("android.app.AppOpsManager")
                    .getDeclaredConstructor(Context.class, IAppOpsService.class)
                    .newInstance(null, service);

            if (appops == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int allowed;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    allowed = appops.unsafeCheckOpNoThrow(op, Process.myUid(), FakeContext.PACKAGE_NAME);
                } else {
                    allowed = appops.checkOpNoThrow(op, Process.myUid(), FakeContext.PACKAGE_NAME);
                }

                return (allowed == AppOpsManager.MODE_ALLOWED);
            } else {
                return false;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if {@link Manifest.permission#SYSTEM_ALERT_WINDOW} permission has been granted.
     *
     * @return Returns {@code true} if permission is granted, otherwise {@code false}.
     */
    public static boolean checkDisplayOverOtherAppsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            return checkPermission(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW);
        else
            return true;
    }

    /**
     * Check if running on sdk 29 (android 10) or higher and {@link Manifest.permission#SYSTEM_ALERT_WINDOW}
     * permission has been granted or not.
     *
     * @param logGranted If it should be logged that permission has been granted.
     * @param logNotGranted If it should be logged that permission has not been granted.
     * @return Returns {@code true} if permission is granted, otherwise {@code false}.
     */
    public static boolean validateDisplayOverOtherAppsPermissionForPostAndroid10(boolean logGranted, boolean logNotGranted) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true;

        if (!checkDisplayOverOtherAppsPermission()) {
            if (logNotGranted)
                Log.w(LOG_TAG, "The " + FakeContext.PACKAGE_NAME + " package does not have" +
                        " the Display over other apps (SYSTEM_ALERT_WINDOW) permission");
            return false;
        } else {
            if (logGranted)
                Log.d(LOG_TAG, "The " + FakeContext.PACKAGE_NAME + " package already has" +
                        " the Display over other apps (SYSTEM_ALERT_WINDOW) permission");
            return true;
        }
    }

    public static String getMissingDisplayOverOtherAppsPermissionError() {
        return "The " + FakeContext.PACKAGE_NAME + " app requires the" +
                " \"Display over other apps\" permission to start activities and" +
                " services from background on Android >= 10. Grants it from Android" +
                " Settings -> Apps -> %1$s -> Advanced -> Draw over other apps.\n" +
                "The permission name may be different on different devices, like" +
                " on Xiaomi, its called \"Display pop-up windows while running" +
                " in the background\".\n" +
                "Check https://dontkillmyapp.com for device specific issues.";
    }

}
