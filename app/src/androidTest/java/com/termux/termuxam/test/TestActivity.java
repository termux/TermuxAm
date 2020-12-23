package com.termux.termuxam.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.termux.termuxam.IActivityManagerTest;

/**
 * {@link Activity} used for {@link IActivityManagerTest#testStartActivity()}
 */
public class TestActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestComponentsService.noteEvent("TestActivity " + getIntent().getAction());
        finish();
    }
}
