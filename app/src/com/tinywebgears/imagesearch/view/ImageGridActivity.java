package com.tinywebgears.imagesearch.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.tinywebgears.imagesearch.BuildConfig;
import com.tinywebgears.imagesearch.util.Utils;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends RoboSherlockFragmentActivity
{
    private static final String TAG = "ImageGridActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (BuildConfig.DEBUG)
        {
            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentByTag(TAG) == null)
        {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, new ImageGridFragment(), TAG);
            ft.commit();
        }
    }
}
