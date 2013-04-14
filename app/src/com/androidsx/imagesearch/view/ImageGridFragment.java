/**
 * This code has highly borrowed from Google's sample code, BitmapFun.
 * http://developer.android.com/training/displaying-bitmaps/index.html
 */

package com.androidsx.imagesearch.view;

import java.util.ArrayList;
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
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
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
import com.androidsx.imagesearch.task.GetImagesTask.GetImagesTaskError;
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
    private static final String sImageCacheDir = "thumbs";
    private static final String sStateGridViewLoading = "ImageGridFragment.STATE_GRID_VIEW_LOADING";
    private static final String sStateSearchString = "ImageGridFragment.STATE_SEARCH_STRING";
    private static final String sStateStartIndex = "ImageGridFragment.STATE_START_INDEX";
    private static final String sStateFailed = "ImageGridFragment.STATE_FAILED";

    private SearchView searchView;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageFetcher mImageFetcher;
    private ImageAdapter mAdapter;
    private SuggestionsAdapter mSuggestionsAdapter;
    private GridView mGridView;
    private LinearLayout mFooter;
    private InfiniteGridView mInfiniteGridView;
    private String mSearchStr = "";
    private int mSearchQueryIndex = 1;
    private boolean mFailed = false;

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

        ImageCacheParams cacheParams = new ImageCacheParams(activity, sImageCacheDir);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(activity, mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(activity.getSupportFragmentManager(), cacheParams);

        mFooter = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.image_grid_footer, null);
        mAdapter = new ImageAdapter(activity, mFooter, mImageFetcher);

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
                searchView.setQuery(mSearchStr, false);
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
                    searchForImages(true);
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
        mGridView = (GridView) v.findViewById(R.id.gridView);
        mInfiniteGridView = new InfiniteGridView(mGridView, mFooter, new InfiniteGridView.Callbacks()
        {
            @Override
            public void onNearTheEnd()
            {
                Log.d(TAG, "Must fetch next images now.");
                // TODO: Check the time and retry maybe.
                if (mFailed)
                    Log.i(TAG, "Last try failed, not going to try again.");
                else
                    searchForImages(false);
            }

            @Override
            public boolean isNearEnd(int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                // Scrolling doesn't make sense if the user hasn't performed a search at all.
                if (mSearchStr.length() < 1)
                    return false;
                if (mAdapter.getLastVisibleRow(firstVisibleItem, visibleItemCount) + 1 > mAdapter
                        .getTotalRows(totalItemCount))
                    return true;
                return false;
            }
        });
        mInfiniteGridView.hideRefreshView();
        if (savedInstanceState != null)
        {
            mSearchStr = savedInstanceState.getString(sStateSearchString);
            mSearchQueryIndex = savedInstanceState.getInt(sStateStartIndex);
            mFailed = savedInstanceState.getBoolean(sStateFailed);
            boolean isLoading = savedInstanceState.getBoolean(sStateGridViewLoading);
            if (isLoading)
                mInfiniteGridView.showRefreshView();
            else
                mInfiniteGridView.hideRefreshView();
        }

        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mInfiniteGridView.setScrollListener(new AbsListView.OnScrollListener()
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated(): " + (savedInstanceState != null ? "NOT NULL" : "NULL"));
    }

    @Override
    public void onSaveInstanceState(Bundle bundle)
    {
        super.onSaveInstanceState(bundle);
        Log.i(TAG, "onSaveInstanceState()");
        bundle.putString(sStateSearchString, mSearchStr);
        bundle.putInt(sStateStartIndex, mSearchQueryIndex);
        bundle.putBoolean(sStateGridViewLoading, mInfiniteGridView.isLoading().get());
        bundle.putBoolean(sStateFailed, mFailed);
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

    public void searchForImages(boolean fresh)
    {
        assert (mSearchStr != null);
        if (mSearchStr.length() < 1)
            return;
        mFailed = false;
        if (fresh)
        {
            Images.imageThumbUrls = new ArrayList<String>();
            Images.imageUrls = new ArrayList<String>();
            mSearchQueryIndex = 1;
        }
        mInfiniteGridView.showRefreshView();
        mAdapter.notifyDataSetChanged();
        GetImagesTask task = new GetImagesTask(getActivity(), mSearchQueryIndex, fresh);
        task.execute(mSearchStr);
    }

    @Override
    public void onImagesReady(boolean successful, GetImagesTaskError error, int nextStartIndex)
    {
        if (successful)
        {
            mSearchQueryIndex = nextStartIndex;
            mAdapter.notifyDataSetChanged();
        }
        else
        {
            mFailed = true;
            if (error != GetImagesTaskError.MAX_REACHED)
                Toast.makeText(getActivity(), R.string.query_failed, Toast.LENGTH_LONG).show();
        }
        mInfiniteGridView.hideRefreshView();
    }

    // /////////////
    // Inner classes
    // /////////////

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
