/**
 * This code has highly borrowed from Google's sample code, BitmapFun.
 * http://developer.android.com/training/displaying-bitmaps/index.html
 */

package com.tinywebgears.imagesearch.view;

import roboguice.inject.ContentView;
import roboguice.inject.InjectFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.tinywebgears.imagesearch.R;

/**
 * Simple fragment activity to hold the main {@link ImageGridFragment}.
 */
@ContentView(R.layout.image_grid_activity)
public class ImageGridActivity extends BaseActivity
{
    private static final String TAG = "ImageGridActivity";

    @InjectFragment(R.id.image_grid)
    private Fragment imageGridFragment;

    // /////////////////
    // Lifecycle methods
    // /////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
}
