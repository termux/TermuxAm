package com.termux.termuxam.logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {

    public static void logError(String message) {
        logError(null, message);
    }

    public static void logError(String tag, String message) {
        System.err.println((tag != null ? tag + " " : "") + message);
    }



    public static void logStackTraceWithMessage(String message, Throwable throwable) {
        logStackTraceWithMessage(null, message, throwable);
    }

    public static void logStackTraceWithMessage(String tag, String message, Throwable throwable) {
        Logger.logError(tag, getMessageAndStackTraceString(message, throwable));
    }

    public static String getMessageAndStackTraceString(String message, Throwable throwable) {
        if (message == null && throwable == null)
            return null;
        else if (message != null && throwable != null)
            return message + ":\n" + getStackTraceString(throwable);
        else if (throwable == null)
            return message;
        else
            return getStackTraceString(throwable);
    }

    public static String getStackTraceString(Throwable throwable) {
        if (throwable == null) return null;

        String stackTraceString = null;

        try {
            StringWriter errors = new StringWriter();
            PrintWriter pw = new PrintWriter(errors);
            throwable.printStackTrace(pw);
            pw.close();
            stackTraceString = errors.toString();
            errors.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stackTraceString;
    }

}
