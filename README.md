# Android Oreo-compatible `am` command reimplementation
`am` (Activity Manager) command in Android can be used to start Activity
or send Broadcast from shell, however since Android Oreo that command
only works from adb shell, not from apps. This is modified version of that
command that is usable from app.

# Running
In this repository there are two wrapper scripts:
* `am-libexec-packaged`
* `am-apk-installed`

First one is for use as installed package in Termux, while second one
is for development, using TermuxAm apk that is installed as app in Android,
allowing installation of Java part from Android Studio

# Running tests/debugging
Tests checking IActivityManager wrapper class are in `app/src/androidTest/java/com/example/termuxam/IActivityManagerTest.java`
and are runnable/debuggable from Android Studio
