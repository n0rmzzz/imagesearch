/**
 * This code has highly borrowed from Google's sample code, BitmapFun.
 * http://developer.android.com/training/displaying-bitmaps/index.html
 */

package com.androidsx.imagesearch.view;

import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
import com.androidsx.imagesearch.BuildConfig;
import com.androidsx.imagesearch.Platform;
import com.androidsx.imagesearch.R;
import com.androidsx.imagesearch.provider.Images;
import com.androidsx.imagesearch.provider.Keywords;
import com.androidsx.imagesearch.task.GetImagesTask;
import com.androidsx.imagesearch.task.GetMemesTask;
import com.androidsx.imagesearch.util.ImageCache.ImageCacheParams;
import com.androidsx.imagesearch.util.ImageFetcher;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView implementation with the
 * key addition being the ImageWorker class w/ImageCache to load children asynchronously, keeping the UI nice and smooth
 * and caching thumbnails for quick retrieval. The cache is retained over configuration changes like orientation change
 * so the images are populated quickly if, for example, the user rotates the device.
 */
public class ImageGridFragment extends SherlockFragment implements AdapterView.OnItemClickListener,
        GetImagesTask.Callbacks
{
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private SearchView searchView;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;
    private ImageAdapter mAdapter;
    private String mSearchStr = "";

    private SuggestionsAdapter mSuggestionsAdapter;

    // /////////////////
    // Lifecycle methods
    // /////////////////

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageGridFragment()
    {
    }

    public String getSearchStr()
    {
        return mSearchStr;
    }

    public void setSearchStr(String searchStr)
    {
        mSearchStr = searchStr;
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

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null)
        {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();
            SearchableInfo info = searchManager.getSearchableInfo(getActivity().getComponentName());
            for (SearchableInfo inf : searchables)
            {
                if (inf.getSuggestAuthority() != null && inf.getSuggestAuthority().startsWith("applications"))
                    info = inf;
            }
            searchView.setSearchableInfo(info);
        }
        updateSuggestionsAdapter(null);
        searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                    return;
                String query = searchView.getQuery().toString();
                searchView.setQuery(query, false);
            }
        });
        searchView.setOnSuggestionListener(new OnSuggestionListener()
        {
            @Override
            public boolean onSuggestionSelect(int position)
            {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position)
            {
                Cursor c = (Cursor) mSuggestionsAdapter.getItem(position);
                String query = c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                searchView.setQuery(query, true);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                searchView.setIconified(true);
                searchView.setIconified(true);
                if (mSearchStr != query && !mSearchStr.equals(query))
                {
                    mSearchStr = query;
                    searchForImages();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                updateSuggestionsAdapter(newText);
                return true;
            }
        });
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

    private void updateSuggestionsAdapter(String prefix)
    {
        String[] columns = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, };
        MatrixCursor cursor = new MatrixCursor(columns);
        int index = 1;
        for (String keyword : Keywords.getKeywords(prefix))
            cursor.addRow(new String[] { Integer.toString(index++), keyword });
        if (mSuggestionsAdapter == null)
            mSuggestionsAdapter = new SuggestionsAdapter(((SherlockFragmentActivity) getActivity())
                    .getSupportActionBar().getThemedContext(), cursor);
        else
            mSuggestionsAdapter.swapCursor(cursor);
        searchView.setSuggestionsAdapter(mSuggestionsAdapter);
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

    public void searchForImages()
    {
        if (mSearchStr == null)
            throw new IllegalArgumentException("Keywork should not be null.");
        if (mSearchStr.length() < 1)
            return;
        Toast.makeText(getActivity(), "Please wait...", Toast.LENGTH_LONG).show();
        GetImagesTask task = new GetMemesTask(getActivity(), this);
        task.execute(mSearchStr);
    }

    @Override
    public void onImagesReady(boolean result)
    {
        if (result)
            mAdapter.notifyDataSetChanged();
        else
            Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_LONG).show();
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

        public ImageAdapter(Context context)
        {
            super();
            mContext = context;
            mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        @Override
        public int getCount()
        {
            if (mNumColumns < 2)
                Images.setCount(Images.imageThumbUrls.length);
            else
                Images.setCount(Images.imageThumbUrls.length / mNumColumns * mNumColumns);
            return (Images.getCount() == 0) ? 0 : Images.getCount() + 1;
        }

        @Override
        public Object getItem(int position)
        {
            if (position == getCount() - 1)
                return null;
            return Images.imageThumbUrls[position];
        }

        @Override
        public long getItemId(int position)
        {
            if (position == getCount() - 1)
                return 0;
            return position;
        }

        @Override
        public int getViewTypeCount()
        {
            return 2;
        }

        @Override
        public int getItemViewType(int position)
        {
            if (position == getCount() - 1)
                return 1;
            return 0;
        }

        @Override
        public boolean hasStableIds()
        {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container)
        {
            if (position == getCount() - 1)
            {
                LinearLayout item = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.image_grid_footer,
                        null);
                return item;
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
            mImageFetcher.loadImage(Images.imageThumbUrls[position], imageView);
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

    /**
     * Search view suggestion adapter.
     */
    private class SuggestionsAdapter extends CursorAdapter
    {

        public SuggestionsAdapter(Context context, Cursor c)
        {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor)
        {
            TextView tv = (TextView) view;
            final int textIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
            tv.setText(cursor.getString(textIndex));
        }
    }
}
