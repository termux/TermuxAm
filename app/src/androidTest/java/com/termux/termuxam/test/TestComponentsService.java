package com.termux.termuxam.test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.termux.termuxam.ITestComponentsService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Helper service for reporting operations performed on test
 * {@link TestActivity} and {@link TestService}
 */
public class TestComponentsService extends Service {

    private static CountDownLatch awaitedEventLatch;
    private static String awaitedEvent;

    static void noteEvent(String name) {
        if (awaitedEventLatch != null) {
            awaitedEvent = name;
            awaitedEventLatch.countDown();
        }
    }

    private final ITestComponentsService.Stub aidlImpl = new ITestComponentsService.Stub() {
        @Override
        public void prepareAwait() throws RemoteException {
            awaitedEvent = null;
            awaitedEventLatch = new CountDownLatch(1);
        }

        @Override
        public String await() throws RemoteException {
            try {
                if (awaitedEventLatch.await(5, TimeUnit.SECONDS)) {
                    return awaitedEvent;
                }
                return "timed out";
            } catch (InterruptedException e) {
                return "await interrupted";
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return aidlImpl;
    }
}
