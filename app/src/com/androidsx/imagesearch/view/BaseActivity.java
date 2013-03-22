package com.androidsx.imagesearch.view;

import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.os.StrictMode;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.androidsx.imagesearch.BuildConfig;
import com.androidsx.imagesearch.Platform;

public class BaseActivity extends RoboSherlockFragmentActivity
{
    @SuppressWarnings("unchecked")
    private static final List<Class<? extends BaseActivity>> childrenActivities = Arrays.asList(
            ImageGridActivity.class, ImageDetailActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        enableStrictModeInDebug();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        ((CustomApplication) getApplication()).detach(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        ((CustomApplication) getApplication()).attach(this);
    }

    @TargetApi(11)
    public static void enableStrictModeInDebug()
    {
        if (Platform.hasGingerbread() && BuildConfig.DEBUG)
        {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll()
                    .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll().penaltyLog();

            if (Platform.hasHoneycomb())
            {
                threadPolicyBuilder.penaltyFlashScreen();
                for (Class<? extends BaseActivity> clazz : childrenActivities)
                    vmPolicyBuilder.setClassInstanceLimit(clazz, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }
}
