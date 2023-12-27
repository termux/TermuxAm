package com.termux.termuxam.reflection.result;

import java.lang.reflect.Method;

/**
 * Result for an invocation of a {@link Method} that has a non-void return type.
 *
 * See also {@link InvokeResult}.
 */
public class MethodInvokeResult extends InvokeResult {

    public MethodInvokeResult(boolean success, Object value) {
        super(success, value);
    }

}
