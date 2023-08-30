package com.termux.termuxam;

import android.annotation.TargetApi;
import android.content.AttributionSource;
import android.content.MutableContextWrapper;
import android.os.Build;
import android.os.Process;

/**
 * - https://github.com/Genymobile/scrcpy/blob/v2.1.1/server/src/main/java/com/genymobile/scrcpy/FakeContext.java
 */
public class FakeContext extends MutableContextWrapper {

    private static final String TERMUX_PACKAGES_BUILD_PACKAGE_NAME =  "@TERMUX_APP_PACKAGE@";
    public static String PACKAGE_NAME = setPackageName();

    public static final int ROOT_UID = 0; // Like android.os.Process.ROOT_UID, but before API 29

    private static final FakeContext INSTANCE = new FakeContext();

    public static FakeContext get() {
        return INSTANCE;
    }

    private FakeContext() {
        super(null);
    }

    @Override
    public String getPackageName() {
        return PACKAGE_NAME;
    }

    @SuppressWarnings("ConstantConditions")
    private static String setPackageName() {
        return TERMUX_PACKAGES_BUILD_PACKAGE_NAME.startsWith("@") ?
                BuildConfig.TERMUX_PACKAGE_NAME : TERMUX_PACKAGES_BUILD_PACKAGE_NAME;
    }

    @Override
    public String getOpPackageName() {
        return PACKAGE_NAME;
    }

    @TargetApi(Build.VERSION_CODES.S)
    @Override
    public AttributionSource getAttributionSource() {
        AttributionSource.Builder builder = new AttributionSource.Builder(Process.myUid());
        builder.setPackageName(PACKAGE_NAME);
        return builder.build();
    }

    // @Override to be added on SDK upgrade for Android 14
    @SuppressWarnings("unused")
    public int getDeviceId() {
        return 0;
    }

}
