package com.termux.termuxam;

import static com.termux.termuxam.Am.LOG_TAG;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * - https://github.com/Genymobile/scrcpy/blob/v2.1.1/server/src/main/java/com/genymobile/scrcpy/Workarounds.java
 */
public class Workarounds {

    private static Class<?> activityThreadClass;
    private static Object activityThread;

    private Workarounds() {
        // not instantiable
    }

    public static void applyMaybeAudio(boolean audio) {
        boolean mustFillAppInfo = false;
        boolean mustFillBaseContext = false;
        boolean mustFillAppContext = false;


        if (Build.BRAND.equalsIgnoreCase("meizu")) {
            // Workarounds must be applied for Meizu phones:
            //  - <https://github.com/Genymobile/scrcpy/issues/240>
            //  - <https://github.com/Genymobile/scrcpy/issues/365>
            //  - <https://github.com/Genymobile/scrcpy/issues/2656>
            //
            // But only apply when strictly necessary, since workarounds can cause other issues:
            //  - <https://github.com/Genymobile/scrcpy/issues/940>
            //  - <https://github.com/Genymobile/scrcpy/issues/994>
            mustFillAppInfo = true;
        } else if (Build.BRAND.equalsIgnoreCase("honor")) {
            // More workarounds must be applied for Honor devices:
            //  - <https://github.com/Genymobile/scrcpy/issues/4015>
            //
            // The system context must not be set for all devices, because it would cause other problems:
            //  - <https://github.com/Genymobile/scrcpy/issues/4015#issuecomment-1595382142>
            //  - <https://github.com/Genymobile/scrcpy/issues/3805#issuecomment-1596148031>
            mustFillAppInfo = true;
            mustFillBaseContext = true;
            mustFillAppContext = true;
        }

        if (audio && Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            // Before Android 11, audio is not supported.
            // Since Android 12, we can properly set a context on the AudioRecord.
            // Only on Android 11 we must fill the application context for the AudioRecord to work.
            mustFillAppContext = true;
        }

        apply(mustFillAppInfo, mustFillBaseContext, mustFillAppContext);
    }

    public static void apply(boolean mustFillAppInfo, boolean mustFillBaseContext, boolean mustFillAppContext) {
        if (mustFillAppInfo) {
            Workarounds.fillAppInfo();
        }
        if (mustFillBaseContext) {
            Workarounds.fillBaseContext();
        }
        if (mustFillAppContext) {
            Workarounds.fillAppContext();
        }
    }

    @SuppressWarnings("deprecation")
    private static void prepareMainLooper() {
        // Some devices internally create a Handler when creating an input Surface, causing an exception:
        //   "Can't create handler inside thread that has not called Looper.prepare()"
        // <https://github.com/Genymobile/scrcpy/issues/240>
        //
        // Use Looper.prepareMainLooper() instead of Looper.prepare() to avoid a NullPointerException:
        //   "Attempt to read from field 'android.os.MessageQueue android.os.Looper.mQueue'
        //    on a null object reference"
        // <https://github.com/Genymobile/scrcpy/issues/921>
        Looper.prepareMainLooper();
    }

    public static Object getActivityThread() throws Exception {
        setActivityThread();
        return activityThread;
    }

    @SuppressLint("PrivateApi,DiscouragedPrivateApi")
    private static void setActivityThread() throws Exception {
        if (activityThread == null) {
            Workarounds.prepareMainLooper();

            // ActivityThread activityThread = new ActivityThread();
            activityThreadClass = Class.forName("android.app.ActivityThread");
            Constructor<?> activityThreadConstructor = activityThreadClass.getDeclaredConstructor();
            activityThreadConstructor.setAccessible(true);
            activityThread = activityThreadConstructor.newInstance();
        }
    }

    private static void fillActivityThread() throws Exception {
        setActivityThread();

        // ActivityThread.sCurrentActivityThread = activityThread;
        Field sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
        sCurrentActivityThreadField.setAccessible(true);
        sCurrentActivityThreadField.set(null, activityThread);
    }

    @SuppressLint("PrivateApi,DiscouragedPrivateApi")
    private static void fillAppInfo() {
        try {
            fillActivityThread();

            // ActivityThread.AppBindData appBindData = new ActivityThread.AppBindData();
            Class<?> appBindDataClass = Class.forName("android.app.ActivityThread$AppBindData");
            Constructor<?> appBindDataConstructor = appBindDataClass.getDeclaredConstructor();
            appBindDataConstructor.setAccessible(true);
            Object appBindData = appBindDataConstructor.newInstance();

            ApplicationInfo applicationInfo = new ApplicationInfo();
            applicationInfo.packageName = FakeContext.PACKAGE_NAME;

            // appBindData.appInfo = applicationInfo;
            Field appInfoField = appBindDataClass.getDeclaredField("appInfo");
            appInfoField.setAccessible(true);
            appInfoField.set(appBindData, applicationInfo);

            // activityThread.mBoundApplication = appBindData;
            Field mBoundApplicationField = activityThreadClass.getDeclaredField("mBoundApplication");
            mBoundApplicationField.setAccessible(true);
            mBoundApplicationField.set(activityThread, appBindData);
        } catch (Throwable t) {
            // this is a workaround, so failing is not an error
            Log.d(LOG_TAG, "Could not fill app info. message: " + t.getMessage() + ", cause" + t.getCause());
        }
    }

    @SuppressLint("PrivateApi,DiscouragedPrivateApi")
    private static void fillAppContext() {
        try {
            fillActivityThread();

            Application app = Application.class.newInstance();
            Field baseField = ContextWrapper.class.getDeclaredField("mBase");
            baseField.setAccessible(true);
            baseField.set(app, FakeContext.get());

            // activityThread.mInitialApplication = app;
            Field mInitialApplicationField = activityThreadClass.getDeclaredField("mInitialApplication");
            mInitialApplicationField.setAccessible(true);
            mInitialApplicationField.set(activityThread, app);
        } catch (Throwable t) {
            // this is a workaround, so failing is not an error
            Log.d(LOG_TAG, "Could not fill app context. message: " + t.getMessage() + ", cause" + t.getCause());
        }
    }

    public static void fillBaseContext() {
        try {
            fillActivityThread();

            Method getSystemContextMethod = activityThreadClass.getDeclaredMethod("getSystemContext");
            Context context = (Context) getSystemContextMethod.invoke(activityThread);
            FakeContext.get().setBaseContext(context);
        } catch (Throwable t) {
            // this is a workaround, so failing is not an error
            Log.d(LOG_TAG, "Could not fill base context. message: " + t.getMessage() + ", cause" + t.getCause());
        }
    }

}
