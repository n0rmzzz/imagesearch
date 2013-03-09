/**
 * This code has highly borrowed from Google's sample code, BitmapFun.
 * http://developer.android.com/training/displaying-bitmaps/index.html
 */

package com.tinywebgears.imagesearch.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

/**
 * Simple fragment activity to hold the main {@link ImageGridFragment}.
 */
public class ImageGridActivity extends BaseActivity
{
    private static final String TAG = "ImageGridActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentByTag(TAG) == null)
        {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, new ImageGridFragment(), TAG);
            ft.commit();
        }
    }
}
