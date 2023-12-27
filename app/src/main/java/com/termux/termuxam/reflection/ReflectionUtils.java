package com.termux.termuxam.reflection;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.termuxam.logger.Logger;
import com.termux.termuxam.reflection.result.MethodInvokeResult;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtils {

    private static boolean HIDDEN_API_REFLECTION_RESTRICTIONS_BYPASSED = Build.VERSION.SDK_INT < Build.VERSION_CODES.P;

    /**
     * Bypass android hidden API reflection restrictions.
     * https://github.com/LSPosed/AndroidHiddenApiBypass
     * https://developer.android.com/guide/app-compatibility/restrictions-non-sdk-interfaces
     */
    public synchronized static void bypassHiddenAPIReflectionRestrictions() {
        if (!HIDDEN_API_REFLECTION_RESTRICTIONS_BYPASSED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                HiddenApiBypass.addHiddenApiExemptions("");
            } catch (Throwable t) {
                Logger.logStackTraceWithMessage("Failed to bypass hidden API reflection restrictions", t);
            }

            HIDDEN_API_REFLECTION_RESTRICTIONS_BYPASSED = true;
        }
    }

    /** Check if android hidden API reflection restrictions are bypassed. */
    public synchronized static boolean areHiddenAPIReflectionRestrictionsBypassed() {
        return HIDDEN_API_REFLECTION_RESTRICTIONS_BYPASSED;
    }





    /**
     * Wrapper for {@link #getDeclaredMethod(Class, String, Class[])} without parameters.
     */
    @Nullable
    public static Method getDeclaredMethod(Class<?> clazz, String methodName) {
        return getDeclaredMethod(clazz, methodName, new Class<?>[0]);
    }

    /**
     * Get a {@link Method} for the specified class with the specified parameters.
     *
     * @param clazz The {@link Class} for which to return the method.
     * @param methodName The name of the {@link Method}.
     * @param parameterTypes The parameter types of the method.
     * @return Returns the {@link Method} if getting the it was successful, otherwise {@code null}.
     */
    @Nullable
    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            Logger.logStackTraceWithMessage("Failed to get" +
                " \"" + methodName + "\" method for \"" + (clazz != null ? clazz.getName() : null) + "\"" +
                " class with parameter types: " + Arrays.toString(parameterTypes), e);
            return null;
        }
    }


    /**
     * Wrapper for {@link #invokeMethod(Method, Object, Object...)} without arguments.
     */
    @NonNull
    public static MethodInvokeResult invokeMethod(Method method, Object obj) {
        return invokeMethod(method, obj, new Object[0]);
    }

    /**
     * Invoke a {@link Method} on the specified object with the specified arguments.
     *
     * @param method The {@link Method} to invoke.
     * @param obj The {@link Object} the method should be invoked from.
     * @param args The arguments to pass to the method.
     * @return Returns the {@link MethodInvokeResult} of invoking the method. The
     * {@link MethodInvokeResult#success} will be {@code true} if invoking the method was successful,
     * otherwise {@code false}. The {@link MethodInvokeResult#value} will contain the {@link Object}
     * returned by the method.
     */
    @NonNull
    public static MethodInvokeResult invokeMethod(Method method, Object obj, Object... args) {
        try {
            method.setAccessible(true);
            return new MethodInvokeResult(true, method.invoke(obj, args));
        } catch (Exception e) {
            Logger.logStackTraceWithMessage("Failed to invoke" +
                " \"" + (method != null ? method.getName() : null) + "\" method with object" +
                " \"" + obj + "\" and args: " + Arrays.toString(args), e);
            return new MethodInvokeResult(false, null);
        }
    }

}
