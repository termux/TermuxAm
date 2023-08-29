package com.termux.termuxam;

public class FakeContext {

    private static final String TERMUX_PACKAGES_BUILD_PACKAGE_NAME =  "@TERMUX_APP_PACKAGE@";
    public static String PACKAGE_NAME = setPackageName();

    @SuppressWarnings("ConstantConditions")
    private static String setPackageName() {
        return TERMUX_PACKAGES_BUILD_PACKAGE_NAME.startsWith("@") ?
                BuildConfig.TERMUX_PACKAGE_NAME : TERMUX_PACKAGES_BUILD_PACKAGE_NAME;
    }

}
