/**
 * This code has highly borrowed from Google's sample code, BitmapFun.
 * http://developer.android.com/training/displaying-bitmaps/index.html
 */

package com.androidsx.imagesearch.view;

import javax.annotation.Nullable;

import roboguice.inject.ContentView;
import roboguice.inject.InjectFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.androidsx.imagesearch.R;
import com.androidsx.imagesearch.task.GetImagesTask;
import com.androidsx.imagesearch.task.GetImagesTask.Callbacks;
import com.androidsx.imagesearch.task.GetImagesTask.GetImagesTaskError;

/**
 * Simple fragment activity to hold the main {@link ImageGridFragment}.
 */
@ContentView(R.layout.image_grid_activity)
public class ImageGridActivity extends BaseActivity implements GetImagesTask.Callbacks
{
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
    public void onImagesReady(boolean successful, GetImagesTaskError error, int nextStartIndex)
    {
        if (mImageGridFragment != null)
            ((Callbacks) mImageGridFragment).onImagesReady(successful, error, nextStartIndex);
    }
}
