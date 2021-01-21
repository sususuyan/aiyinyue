package com.zeusee.main.hyperlandmark;

import android.content.Context;

import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.zeusee.main.hyperlandmark", appContext.getPackageName());
        String phoneModel = Build.MODEL;
        Log.e("获取手机型号--->", phoneModel);
        String phoneReleaseVersion = Build.VERSION.RELEASE;
        Log.e("获取手机系统版本-->" , phoneReleaseVersion);




    }
}