// ITestComponentsService.aidl
package com.termux.termuxam;

// Declare any non-default types here with import statements

interface ITestComponentsService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    /*void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);*/
    void prepareAwait();
    String await();
}
