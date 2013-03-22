/**
 * This code has highly borrowed from Google's sample code, BitmapFun.
 * http://developer.android.com/training/displaying-bitmaps/index.html
 */

package com.tinywebgears.imagesearch.view;

import javax.annotation.Nullable;

import roboguice.inject.ContentView;
import roboguice.inject.InjectFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.tinywebgears.imagesearch.R;
import com.tinywebgears.imagesearch.task.GetImagesTask;
import com.tinywebgears.imagesearch.task.GetImagesTask.Callbacks;

/**
 * Simple fragment activity to hold the main {@link ImageGridFragment}.
 */
@ContentView(R.layout.image_grid_activity)
public class ImageGridActivity extends BaseActivity implements GetImagesTask.Callbacks
{
    private static final String TAG = "ImageGridActivity";
    private static final String STATE_SEARCH_STRING = "state-search-string";

    @Nullable
    @InjectFragment(R.id.image_grid)
    private Fragment mImageGridFragment;

    // /////////////////
    // Lifecycle methods
    // /////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mImageGridFragment != null)
            outState.putString(STATE_SEARCH_STRING, ((ImageGridFragment) mImageGridFragment).getSearchStr());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (mImageGridFragment != null)
            ((ImageGridFragment) mImageGridFragment).setSearchStr(savedInstanceState.getString(STATE_SEARCH_STRING));
    }

    @Override
    public void onImagesReade()
    {
        Log.d(TAG, "Images are ready.");
        if (mImageGridFragment != null)
            ((Callbacks) mImageGridFragment).onImagesReade();
    }
}
