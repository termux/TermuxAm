package com.termux.termuxam;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import java.lang.reflect.InvocationTargetException;

/**
 * Wrapper around android.app.IActivityManager internal interface
 */
@SuppressLint("PrivateApi")
class IActivityManager {

    private Object mAm;
    private final String mCallingAppPackage;
    private CrossVersionReflectedMethod mGetProviderMimeTypeMethod;
    private CrossVersionReflectedMethod mStartActivityAsUserMethod;
    //private CrossVersionReflectedMethod mBroadcastIntentMethod;
    private CrossVersionReflectedMethod mStartServiceMethod;
    private CrossVersionReflectedMethod mStopServiceMethod;
    private CrossVersionReflectedMethod mGetIntentSenderMethod;
    private CrossVersionReflectedMethod mIntentSenderSendMethod;

    IActivityManager() throws Exception {
        this(FakeContext.PACKAGE_NAME, false);
    }

    IActivityManager(String callingAppPackage, boolean setupMethods) throws Exception {
        mCallingAppPackage = callingAppPackage;

        try {
            try {
                mAm = android.app.ActivityManager.class
                        .getMethod("getService")
                        .invoke(null);
            } catch (Exception e) {
                mAm = Class.forName("android.app.ActivityManagerNative")
                        .getMethod("getDefault")
                        .invoke(null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (setupMethods) {
            getGetProviderMimeTypeMethod();
            getStartActivityAsUserMethod();
            //getBroadcastIntentMethod();
            getStartServiceMethod();
            getStopServiceMethod();
            getGetIntentSenderMethod();
            getIntentSenderSendMethod();
        }
    }

    private CrossVersionReflectedMethod getGetProviderMimeTypeMethod() {
        if (mGetProviderMimeTypeMethod != null) return mGetProviderMimeTypeMethod;

        try {
            mGetProviderMimeTypeMethod = new CrossVersionReflectedMethod(mAm.getClass())
                    .tryMethodVariantInexact(
                            "getProviderMimeType",
                            Uri.class, "uri",
                            int.class, "userId"
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mGetProviderMimeTypeMethod;
    }

    private CrossVersionReflectedMethod getStartActivityAsUserMethod() {
        if (mStartActivityAsUserMethod != null) return mStartActivityAsUserMethod;

        try {
            mStartActivityAsUserMethod = new CrossVersionReflectedMethod(mAm.getClass())
                    .tryMethodVariantInexact(
                            "startActivityAsUser",
                            "android.app.IApplicationThread", "caller", null,
                            String.class, "callingPackage", mCallingAppPackage,
                            Intent.class, "intent", null,
                            String.class, "resolvedType", null,
                            IBinder.class, "resultTo", null,
                            String.class, "resultWho", null,
                            int.class, "requestCode", -1,
                            int.class, "flags", 0,
                            //ProfilerInfo profilerInfo, - let's autodetect
                            Bundle.class, "options", null,
                            int.class, "userId", 0
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mStartActivityAsUserMethod;
    }

    /*
    private CrossVersionReflectedMethod getBroadcastIntentMethod() {
        if (mBroadcastIntentMethod != null) return mBroadcastIntentMethod;

        try {
            mBroadcastIntentMethod = new CrossVersionReflectedMethod(mAm.getClass())
                    .tryMethodVariantInexact(
                            "broadcastIntent",
                            "android.app.IApplicationThread", "caller", null,
                            Intent.class, "intent", null,
                            String.class, "resolvedType", null,
                            IIntentReceiver.class, "resultTo", null,
                            int.class, "resultCode", -1,
                            String.class, "resultData", null,
                            Bundle.class, "map", null,
                            String[].class, "requiredPermissions", null,
                            int.class, "appOp", 0,
                            Bundle.class, "options", null,
                            boolean.class, "serialized", false,
                            boolean.class, "sticky", false,
                            int.class, "userId", 0
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mBroadcastIntentMethod;
    }
    */

    private CrossVersionReflectedMethod getStartServiceMethod() {
        if (mStartServiceMethod != null) return mStartServiceMethod;

        try {
            mStartServiceMethod = new CrossVersionReflectedMethod(mAm.getClass())
                    .tryMethodVariantInexact(
                            "startService",
                            "android.app.IApplicationThread", "caller", null,
                            Intent.class, "service", null,
                            String.class, "resolvedType", null,
                            boolean.class, "requireForeground", false,
                            String.class, "callingPackage", mCallingAppPackage,
                            int.class, "userId", 0
                    ).tryMethodVariantInexact(
                            "startService",
                            "android.app.IApplicationThread", "caller", null,
                            Intent.class, "service", null,
                            String.class, "resolvedType", null,
                            String.class, "callingPackage", mCallingAppPackage,
                            int.class, "userId", 0
                    ).tryMethodVariantInexact( // Pre frameworks/base 99b6043
                            "startService",
                            "android.app.IApplicationThread", "caller", null,
                            Intent.class, "service", null,
                            String.class, "resolvedType", null,
                            int.class, "userId", 0
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mStartServiceMethod;
    }

    private CrossVersionReflectedMethod getStopServiceMethod() {
        if (mStopServiceMethod != null) return mStopServiceMethod;

        try {
            mStopServiceMethod = new CrossVersionReflectedMethod(mAm.getClass())
                    .tryMethodVariantInexact(
                            "stopService",
                            "android.app.IApplicationThread", "caller", null,
                            Intent.class, "service", null,
                            String.class, "resolvedType", null,
                            int.class, "userId", 0
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mStopServiceMethod;
    }

    private CrossVersionReflectedMethod getGetIntentSenderMethod() {
        if (mGetIntentSenderMethod != null) return mGetIntentSenderMethod;

        try {
            mGetIntentSenderMethod = new CrossVersionReflectedMethod(mAm.getClass())
                    .tryMethodVariantInexact(
                            "getIntentSender",
                            int.class, "type", 0,
                            String.class, "packageName", mCallingAppPackage,
                            IBinder.class, "token", null,
                            String.class, "resultWho", null,
                            int.class, "requestCode", 0,
                            Intent[].class, "intents", null,
                            String[].class, "resolvedTypes", null,
                            int.class, "flags", 0,
                            Bundle.class, "options", null,
                            int.class, "userId", 0
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mGetIntentSenderMethod;
    }

    private CrossVersionReflectedMethod getIntentSenderSendMethod() {
        if (mIntentSenderSendMethod != null) return mIntentSenderSendMethod;

        try {
            mIntentSenderSendMethod = new CrossVersionReflectedMethod(
                    Class.forName("android.content.IIntentSender"))
                    .tryMethodVariantInexact(
                            "send",
                            int.class, "code", 0,
                            Intent.class, "intent", null,
                            String.class, "resolvedType", null,
                            //IBinder.class, "android.os.IBinder whitelistToken", null,
                            "android.content.IIntentReceiver", "finishedReceiver", null,
                            String.class, "requiredPermission", null,
                            Bundle.class, "options", null
                    ).tryMethodVariantInexact( // Pre frameworks/base a750a63
                            "send",
                            int.class, "code", 0,
                            Intent.class, "intent", null,
                            String.class, "resolvedType", null,
                            "android.content.IIntentReceiver", "finishedReceiver", null,
                            String.class, "requiredPermission", null
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mIntentSenderSendMethod;
    }



    int startActivityAsUser(Intent intent, String resolvedType, int flags, Bundle options, int userId) throws InvocationTargetException {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2)
            Workarounds.apply(true, false, false);

        return (Integer) getStartActivityAsUserMethod().invoke(
                mAm,
                "intent", intent,
                "resolvedType", resolvedType,
                "flags", flags,
                "options", options,
                "userId", userId
        );
    }

    void broadcastIntent(Intent intent, IIntentReceiver resultTo, String[] requiredPermissions,
                         boolean serialized, boolean sticky, int userId) throws InvocationTargetException {
        /*
        getBroadcastIntentMethod().invoke(
                mAm,
                "intent", intent,
                "resultTo", resultTo,
                "requiredPermissions", requiredPermissions,
                "serialized", serialized,
                "sticky", sticky,
                "userId", userId
        );
        */
        Object pendingIntent = getGetIntentSenderMethod().invoke(
                mAm,
                "type", 1 /*ActivityManager.INTENT_SENDER_BROADCAST*/,
                "intents", new Intent[] { intent },
                "flags", PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT,
                "userId", userId
        );
        getIntentSenderSendMethod().invoke(
                pendingIntent,
                "requiredPermission", (requiredPermissions == null || requiredPermissions.length == 0) ? null : requiredPermissions[0],
                "finishedReceiver", resultTo
        );
    }

    String getProviderMimeType(Uri uri, int userId) throws InvocationTargetException {
        if (!"content".equals(uri.getScheme())) return null;

        // The `IActivityManager.getProviderMimeType()` method has been removed in Android `>= 14` and
        // replaced by `getMimeTypeFilterAsync()` (previously `getProviderMimeTypeAsync()`, which
        // is an async method that requires a `RemoteCallback` instance to be passed.
        // `ActivityManagerShellCommand` also now instead uses `Intent.resolveType()` to get
        // mime type, which uses `ContentResolver.getType()` internally and that requires a `Context`.
        // The `ContentResolver.getType()` internally calls `IActivityManager.getProviderMimeType()`,
        // if failed to get an existing `IContentProvider` with `ActivityThread`, which then calls
        // `ContentProviderHelper.getProviderMimeTypeAsync()`, however calling it or
        // `getProviderMimeType()` just seems to always return `null` and not call target provider
        // for some reason. The `IContentProvider.getTypeAsync()` should call the target provider,
        // but to actually get the `IContentProvider` with `IActivityManager.getContentProvider()`
        // requires passing an `IApplicationThread` that belongs to an app process since
        // `ContentProviderHelper.getContentProviderImpl()` uses that to get the relevant
        // `ProcessRecord`, and ir will otherwise throw `Unable to find app for caller` error.
        // But since we are not running in an app we don't have a valid `IApplicationThread`,
        // even if we get it from `ActivityThread.mAppThread` we create.
        // `ActivityThread.acquireExistingProvider()` method used by `ContextImpl`, it is not used.
        // - https://cs.android.com/android/platform/superproject/+/android-13.0.0_r74:frameworks/base/core/java/android/app/IActivityManager.aidl;l=339
        // - https://cs.android.com/android/platform/superproject/+/android-13.0.0_r74:frameworks/base/services/core/java/com/android/server/am/ActivityManagerShellCommand.java;l=497
        // - https://cs.android.com/android/platform/superproject/+/android-13.0.0_r74:frameworks/base/core/java/android/content/ContentResolver.java;l=906
        // - https://cs.android.com/android/platform/superproject/+/android-13.0.0_r74:frameworks/base/core/java/android/content/Intent.java;l=8563
        // - https://cs.android.com/android/platform/superproject/+/android-13.0.0_r74:frameworks/base/services/core/java/com/android/server/am/ContentProviderHelper.java;l=1030
        // - https://cs.android.com/android/platform/superproject/+/android-13.0.0_r74:frameworks/base/core/java/android/app/ActivityThread.java;l=7187
        // - https://cs.android.com/android/platform/superproject/+/android-13.0.0_r74:frameworks/base/core/java/android/app/ContextImpl.java;l=3222
        // - https://cs.android.com/android/platform/superproject/+/android-13.0.0_r74:frameworks/base/services/core/java/com/android/server/am/ContentProviderHelper.java;l=178
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            CrossVersionReflectedMethod method = getGetProviderMimeTypeMethod();
            if (method.isFound()) {
                return (String) getGetProviderMimeTypeMethod().invoke(
                        mAm,
                        "uri", uri,
                        "userId", userId
                );
            }
        }

        return null;
    }

    ComponentName startService(Intent service, String resolvedType, int userId) throws InvocationTargetException {
        return (ComponentName) getStartServiceMethod().invoke(
                mAm,
                "service", service,
                "resolvedType", resolvedType,
                "userId", userId
        );
    }

    int stopService(Intent service, String resolvedType, int userId) throws InvocationTargetException {
        return (Integer) getStopServiceMethod().invoke(
                mAm,
                "service", service,
                "resolvedType", resolvedType,
                "userId", userId
        );
    }
}
