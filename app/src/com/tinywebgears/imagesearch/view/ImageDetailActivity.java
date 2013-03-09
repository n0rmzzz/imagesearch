/**
 * This code has highly borrowed from Google's sample code, BitmapFun.
 * http://developer.android.com/training/displaying-bitmaps/index.html
 */

package com.tinywebgears.imagesearch.view;

import roboguice.inject.ContentView;
import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.tinywebgears.imagesearch.Platform;
import com.tinywebgears.imagesearch.R;
import com.tinywebgears.imagesearch.provider.Images;
import com.tinywebgears.imagesearch.util.ImageCache;
import com.tinywebgears.imagesearch.util.ImageFetcher;

/**
 * Simple fragment activity to hold the main {@link ImageDetailFragment}.
 */
@ContentView(R.layout.image_detail_pager)
public class ImageDetailActivity extends BaseActivity implements OnClickListener
{
    private static final String TAG = "ImageDetailActivity";
    private static final String IMAGE_CACHE_DIR = "images";
    public static final String EXTRA_IMAGE = "extra_image";

    private ActionBar mActionBar;
    private ImagePagerAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    private ViewPager mPager;

    // /////////////////
    // Lifecycle methods
    // /////////////////

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.hide();
        setUpSystemUiVisibility();

        loadImage();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

    // //////////////
    // Event handlers
    // //////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called by the ViewPager child fragments to load images via the one ImageFetcher
     */
    public ImageFetcher getImageFetcher()
    {
        return mImageFetcher;
    }

    @Override
    public void onClick(View v)
    {
        toggleSystemUiVisibility();
    }

    // ///////////////
    // Private methods
    // ///////////////

    private void loadImage()
    {
        // TODO: Refactor this part.
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;
        final int longest = Math.max(height, width) / 2;

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(this, longest);
        mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
        mImageFetcher.setImageFadeIn(false);

        // Set up ViewPager and backing adapter
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), Images.imageUrls.length);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setPageMargin((int) getResources().getDimension(R.dimen.image_detail_pager_margin));
        mPager.setOffscreenPageLimit(2);

        // Set the current item based on the extra passed in to this activity
        final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
        if (extraCurrentItem != -1)
            mPager.setCurrentItem(extraCurrentItem);
    }

    @TargetApi(14)
    private void toggleSystemUiVisibility()
    {
        final int vis = mPager.getSystemUiVisibility();
        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0)
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        else
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    @TargetApi(14)
    private void setUpSystemUiVisibility()
    {
        if (Platform.hasHoneycomb())
        {
            // Hide and show the ActionBar as the visibility changes
            mPager.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
            {
                @Override
                public void onSystemUiVisibilityChange(int vis)
                {
                    if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0)
                        mActionBar.hide();
                    else
                        mActionBar.show();
                }
            });
            // Start low profile mode and hide ActionBar
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    // /////////////////
    // Inner classes //
    // /////////////////

    /**
     * The main adapter that backs the ViewPager.
     */
    private class ImagePagerAdapter extends FragmentStatePagerAdapter
    {
        private final int mSize;

        public ImagePagerAdapter(FragmentManager fm, int size)
        {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount()
        {
            return mSize;
        }

        @Override
        public Fragment getItem(int position)
        {
            return ImageDetailFragment.newInstance(Images.imageUrls[position]);
        }
    }
}
