package com.termux.termuxam;

import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.termux.termuxam.test.TestActivity;
import com.termux.termuxam.test.TestComponentsService;
import com.termux.termuxam.test.TestReceiver;
import com.termux.termuxam.test.TestService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;


@RunWith(AndroidJUnit4.class)
public class IActivityManagerTest {

    private IActivityManager mAm;
    private String mAction;

    private ITestComponentsService mTestComponentsService;
    private ServiceConnection mServiceConnection;

    @Before
    public void setUp() throws Exception {
        mAm = new IActivityManager(InstrumentationRegistry.getTargetContext().getPackageName());

        // Generate Intent action for use in tests
        mAction = "com.termux.termuxam.test.TEST_INTENT_" + Math.random();

        // Connect to test components service
        final CountDownLatch serviceConnectedLatch = new CountDownLatch(1);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mTestComponentsService = ITestComponentsService.Stub.asInterface(service);
                serviceConnectedLatch.countDown();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(InstrumentationRegistry.getContext(), TestComponentsService.class);
                InstrumentationRegistry.getTargetContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        });
        serviceConnectedLatch.await();
    }

    @After
    public void tearDown() throws Exception {
        InstrumentationRegistry.getTargetContext().unbindService(mServiceConnection);
    }

    @Test
    public void testMethodsAvailable() throws Exception {

        for (Field field : IActivityManager.class.getDeclaredFields()) {
            if (field.getType() == CrossVersionReflectedMethod.class) {
                field.setAccessible(true);
                CrossVersionReflectedMethod method = (CrossVersionReflectedMethod) field.get(mAm);
                assertTrue(field.getName(), method.isFound());
            }
        }
    }

    @Test
    public void testStartActivity() throws Exception {
        mTestComponentsService.prepareAwait();
        Intent intent = new Intent(mAction, null, InstrumentationRegistry.getContext(), TestActivity.class);
        mAm.startActivityAsUser(intent, null, 0, null, 0);
        assertEquals("TestActivity " + mAction, mTestComponentsService.await());
    }

    @Test
    public void testBroadcastIntent() throws Exception {
        // Do not run on Android `>= 14` since it will trigger the
        // `Sending broadcast <action> with resultTo requires resultToApp` exception in logcat by
        // ActivityManagerService and will hang the test forever
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) return;

        final CountDownLatch latch = new CountDownLatch(1);
        final Intent[] outIntent = new Intent[1];
        final String[] outData = new String[1];
        IIntentReceiver finishReceiver = new IIntentReceiver.Stub() {
            @Override
            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                outIntent[0] = intent;
                outData[0] = data;
                latch.countDown();
            }
        };

        // Send the broadcast
        mAm.broadcastIntent(new Intent(mAction, null, InstrumentationRegistry.getContext(), TestReceiver.class), finishReceiver, null, true, false, 0);

        // Wait for result and check values
        latch.await();
        boolean notTimedOut = latch.await(3, TimeUnit.SECONDS);
        assertTrue(notTimedOut);
        assertNotNull(outIntent[0]);
        assertEquals(mAction, outIntent[0].getAction());
        assertEquals(TestReceiver.REPLY_DATA + mAction, outData[0]);
    }

    @Test
    public void testStartStopService() throws Exception {
        Intent intent = new Intent(mAction, null, InstrumentationRegistry.getContext(), TestService.class);
        // Start service
        mTestComponentsService.prepareAwait();
        mAm.startService(intent, null, 0);
        assertEquals("Start TestService " + mAction, mTestComponentsService.await());
        // Stop service
        mTestComponentsService.prepareAwait();
        mAm.stopService(intent, null, 0);
        assertEquals("Stop TestService", mTestComponentsService.await());
    }
}