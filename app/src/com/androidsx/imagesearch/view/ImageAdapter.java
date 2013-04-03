package com.androidsx.imagesearch.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.androidsx.imagesearch.provider.Images;
import com.androidsx.imagesearch.util.ImageFetcher;

/**
 * The main adapter that backs the GridView.
 */
class ImageAdapter extends BaseAdapter
{
    private final Context mContext;
    private int mItemHeight = 0;
    private int mNumColumns = 0;
    private GridView.LayoutParams mImageViewLayoutParams;
    private LinearLayout mFooter;
    private ImageFetcher mImageFetcher;

    public ImageAdapter(Context context, LinearLayout footer, ImageFetcher imageFetcher)
    {
        mContext = context;
        mFooter = footer;
        mImageFetcher = imageFetcher;
        mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public int getCount()
    {
        if (mNumColumns < 2)
            Images.setCount(Images.imageThumbUrls.size());
        else
            Images.setCount(Images.imageThumbUrls.size() / mNumColumns * mNumColumns);
        return (Images.getCount() == 0) ? 1 : Images.getCount() + 1;
    }

    @Override
    public Object getItem(int position)
    {
        if (position == getCount() - 1)
            return null;
        return Images.imageThumbUrls.get(position);
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
            // LinearLayout item = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.image_grid_footer,
            // null);
            // return item;
            return mFooter;
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
        mImageFetcher.loadImage(Images.imageThumbUrls.get(position), imageView);
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

    public int getTotalRows(int totalItemCount)
    {
        return mNumColumns == 0 ? totalItemCount : totalItemCount / mNumColumns;
    }

    public int getLastVisibleRow(int firstVisibleItem, int visibleItemCount)
    {
        int lastVisibleItem = firstVisibleItem + visibleItemCount;
        return mNumColumns == 0 ? lastVisibleItem : lastVisibleItem / mNumColumns;
    }
}
