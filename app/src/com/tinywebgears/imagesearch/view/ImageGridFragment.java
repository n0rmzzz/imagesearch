/**
 * This code has highly borrowed from Google's sample code, BitmapFun.
 * http://developer.android.com/training/displaying-bitmaps/index.html
 */

package com.tinywebgears.imagesearch.view;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.tinywebgears.imagesearch.BuildConfig;
import com.tinywebgears.imagesearch.Platform;
import com.tinywebgears.imagesearch.R;
import com.tinywebgears.imagesearch.provider.Images;
import com.tinywebgears.imagesearch.task.GetImagesTask;
import com.tinywebgears.imagesearch.util.ImageCache.ImageCacheParams;
import com.tinywebgears.imagesearch.util.ImageFetcher;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView implementation with the
 * key addition being the ImageWorker class w/ImageCache to load children asynchronously, keeping the UI nice and smooth
 * and caching thumbnails for quick retrieval. The cache is retained over configuration changes like orientation change
 * so the images are populated quickly if, for example, the user rotates the device.
 */
public class ImageGridFragment extends SherlockFragment implements AdapterView.OnItemClickListener
{
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;
    private ImageAdapter mAdapter;

    // /////////////////
    // Lifecycle methods
    // /////////////////

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageGridFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SherlockFragmentActivity activity = getSherlockActivity();
        mAdapter = new ImageAdapter(activity);

        ImageCacheParams cacheParams = new ImageCacheParams(activity, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(activity, mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(activity.getSupportFragmentManager(), cacheParams);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflator)
    {
        super.onCreateOptionsMenu(menu, inflator);

        inflator.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.clear_cache:
            mImageFetcher.clearCache();
            Toast.makeText(getSherlockActivity(), R.string.clear_cache_complete_toast, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.image_grid_fragment, container, false);
        final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState)
            {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
                    mImageFetcher.setPauseWork(true);
                else
                    mImageFetcher.setPauseWork(false);
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
            }
        });

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                if (mAdapter.getNumColumns() == 0)
                {
                    final int numColumns = (int) Math.floor(mGridView.getWidth()
                            / (mImageThumbSize + mImageThumbSpacing));
                    if (numColumns > 0)
                    {
                        final int columnWidth = (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                        mAdapter.setNumColumns(numColumns);
                        mAdapter.setItemHeight(columnWidth);
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

    // /////////////////
    // UI Event handlers
    // /////////////////

    @TargetApi(16)
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
    {
        final Intent i = new Intent(getSherlockActivity(), ImageDetailActivity.class);
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
        if (Platform.hasJellyBean())
        {
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
            getSherlockActivity().startActivity(i, options.toBundle());
        }
        else
            startActivity(i);
    }

    // ////////////////
    // Business methods
    // ////////////////

    public void searchForImages(String keyword)
    {
        if (keyword == null)
            throw new IllegalArgumentException("Keywork should not be null");
        // TODO: Validate the input.
        GetImagesTask task = new GetImagesTask();
        task.execute();
        mAdapter.keyword = keyword;
        mAdapter.notifyDataSetChanged();
    }

    // /////////////
    // Inner classes
    // /////////////

    /**
     * The main adapter that backs the GridView.
     */
    private class ImageAdapter extends BaseAdapter
    {
        private final Context mContext;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private GridView.LayoutParams mImageViewLayoutParams;
        private String keyword = "";

        public ImageAdapter(Context context)
        {
            super();
            mContext = context;
            mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        @Override
        public int getCount()
        {
            if (keyword.length() < 1)
                return 0;
            return Images.imageThumbUrls.length + mNumColumns;
        }

        @Override
        public Object getItem(int position)
        {
            return position < mNumColumns ? null : Images.imageThumbUrls[position - mNumColumns];
        }

        @Override
        public long getItemId(int position)
        {
            return position < mNumColumns ? 0 : position - mNumColumns;
        }

        @Override
        public int getViewTypeCount()
        {
            return 2;
        }

        @Override
        public int getItemViewType(int position)
        {
            return (position < mNumColumns) ? 1 : 0;
        }

        @Override
        public boolean hasStableIds()
        {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container)
        {
            // First check if this is the top row
            if (position < mNumColumns)
            {
                if (convertView == null)
                    convertView = new View(mContext);
                return convertView;
            }

            // Now handle the main ImageView thumbnails
            ImageView imageView;
            if (convertView == null)
            {
                // if it's not recycled, instantiate and initialize
                imageView = new RecyclingImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(mImageViewLayoutParams);
            }
            else
                imageView = (ImageView) convertView;

            // Check the height matches our calculated column width
            if (imageView.getLayoutParams().height != mItemHeight)
                imageView.setLayoutParams(mImageViewLayoutParams);

            // Finally load the image asynchronously into the ImageView, this also takes care of
            // setting a placeholder image while the background thread runs
            mImageFetcher.loadImage(Images.imageThumbUrls[position - mNumColumns], imageView);
            return imageView;
        }

        /**
         * Sets the item height. Useful for when we know the column width so the height can be set to match.
         * 
         * @param height
         */
        public void setItemHeight(int height)
        {
            if (height == mItemHeight)
                return;
            mItemHeight = height;
            mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
            mImageFetcher.setImageSize(height);
            notifyDataSetChanged();
        }

        public void setNumColumns(int numColumns)
        {
            mNumColumns = numColumns;
        }

        public int getNumColumns()
        {
            return mNumColumns;
        }
    }
}
