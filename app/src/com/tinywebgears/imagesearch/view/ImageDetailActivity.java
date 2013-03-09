package com.tinywebgears.imagesearch.view;

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
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.tinywebgears.imagesearch.BuildConfig;
import com.tinywebgears.imagesearch.R;
import com.tinywebgears.imagesearch.provider.Images;
import com.tinywebgears.imagesearch.util.ImageCache;
import com.tinywebgears.imagesearch.util.ImageFetcher;
import com.tinywebgears.imagesearch.util.Utils;

public class ImageDetailActivity extends RoboSherlockFragmentActivity implements OnClickListener
{
    private static final String IMAGE_CACHE_DIR = "images";
    public static final String EXTRA_IMAGE = "extra_image";

    private ImagePagerAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    private ViewPager mPager;

    @TargetApi(14)
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        if (BuildConfig.DEBUG)
        {
            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_detail_pager);

        // Fetch screen height and width, to use as our max size when loading images as this
        // activity runs full screen
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // For this sample we'll use half of the longest width to resize our images. As the
        // image scaling ensures the image is larger than this, we should be left with a
        // resolution that is appropriate for both portrait and landscape. For best image quality
        // we shouldn't divide by 2, but this will use more memory and require a larger memory
        // cache.
        final int longest = (height > width ? height : width) / 2;

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

        // Set up activity to go full screen
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.hide();

        // Enable some additional newer visibility and ActionBar features to create a more
        // immersive photo viewing experience
        if (Utils.hasHoneycomb())
        {
            // Hide and show the ActionBar as the visibility changes
            mPager.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
            {
                @Override
                public void onSystemUiVisibilityChange(int vis)
                {
                    if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0)
                        actionBar.hide();
                    else
                        actionBar.show();
                }
            });
            // Start low profile mode and hide ActionBar
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        // Set the current item based on the extra passed in to this activity
        final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
        if (extraCurrentItem != -1)
            mPager.setCurrentItem(extraCurrentItem);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        case R.id.clear_cache:
            mImageFetcher.clearCache();
            Toast.makeText(this, R.string.clear_cache_complete_toast, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Called by the ViewPager child fragments to load images via the one ImageFetcher
     */
    public ImageFetcher getImageFetcher()
    {
        return mImageFetcher;
    }

    /**
     * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there could be a large
     * number of items in the ViewPager and we don't want to retain them all in memory at once but create/destroy them
     * on the fly.
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

    /**
     * Set on the ImageView in the ViewPager children fragments, to enable/disable low profile mode when the ImageView
     * is touched.
     */
    @TargetApi(14)
    @Override
    public void onClick(View v)
    {
        final int vis = mPager.getSystemUiVisibility();
        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0)
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        else
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }
}
