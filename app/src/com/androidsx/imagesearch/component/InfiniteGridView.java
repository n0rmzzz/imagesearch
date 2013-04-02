package com.androidsx.imagesearch.component;

import java.util.concurrent.atomic.AtomicBoolean;

import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.LinearLayout;

public class InfiniteGridView implements OnScrollListener
{
    private OnScrollListener mScrollListener;
    private Callbacks mCallbacks;
    private AtomicBoolean mIsLoading;
    private GridView mGridView;
    private LinearLayout mRefreshView;

    public InfiniteGridView(GridView gridView, LinearLayout refreshView, Callbacks callbacks)
    {
        mGridView = gridView;
        mRefreshView = refreshView;
        mCallbacks = callbacks;
        mIsLoading = new AtomicBoolean(false);
        mGridView.setOnScrollListener(this);
        mRefreshView.setVisibility(View.GONE);
    }

    public AtomicBoolean isLoading()
    {
        return mIsLoading;
    }

    public OnScrollListener getScrollListener()
    {
        return mScrollListener;
    }

    public void setScrollListener(OnScrollListener scrollListener)
    {
        mScrollListener = scrollListener;
    }

    /** {@inheritDoc} */
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        // Call the custom scroll listener, if one is provided.
        if (mScrollListener != null)
            mScrollListener.onScrollStateChanged(view, scrollState);
    }

    /** {@inheritDoc} */
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        // Check whether we are near the end or not.
        if (mCallbacks != null && mGridView.getAdapter() != null && !mGridView.getAdapter().isEmpty())
        {
            if (!mIsLoading.get() && mCallbacks.isNearEnd(firstVisibleItem, visibleItemCount, totalItemCount))
            {
                showRefreshView();
                mCallbacks.onNearTheEnd();
            }
        }

        // Call the custom scroll listener, if one is provided.
        if (mScrollListener != null)
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }

    public void showRefreshView()
    {
        mIsLoading.set(true);
        mRefreshView.setVisibility(View.VISIBLE);
    }

    public void hideRefreshView()
    {
        mRefreshView.setVisibility(View.GONE);
        mIsLoading.set(false);
    }

    public interface Callbacks
    {
        boolean isNearEnd(int firstVisibleItem, int visibleItemCount, int totalItemCount);

        void onNearTheEnd();
    }
}
