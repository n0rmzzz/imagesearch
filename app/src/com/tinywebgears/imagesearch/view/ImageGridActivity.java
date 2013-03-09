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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.tinywebgears.imagesearch.R;

/**
 * Simple fragment activity to hold the main {@link ImageGridFragment}.
 */
@ContentView(R.layout.image_grid_activity)
public class ImageGridActivity extends BaseActivity
{
    private static final String TAG = "ImageGridActivity";
    private static final String STATE_SEARCH_STRING = "state-search-string";

    @Nullable
    @InjectFragment(R.id.image_grid)
    private Fragment mImageGridFragment;

    private String mSearchStr = "";

    // /////////////////
    // Lifecycle methods
    // /////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        SearchView searchView = new SearchView(this);
        MenuItem searchItem = menu.add("Search").setIcon(R.drawable.abs__ic_search).setActionView(searchView);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        searchView.setOnQueryTextListener(new OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
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
                return false;
            }
        });

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SEARCH_STRING, mSearchStr);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        mSearchStr = savedInstanceState.getString(STATE_SEARCH_STRING);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        searchForImages();
    }

    // ////////////////
    // Business methods
    // ////////////////

    private void searchForImages()
    {
        if (mImageGridFragment != null)
            ((ImageGridFragment) mImageGridFragment).searchForImages(mSearchStr);
    }
}
