/**
 * This code has highly borrowed from Google's sample code, BitmapFun.
 * http://developer.android.com/training/displaying-bitmaps/index.html
 */

package com.androidsx.imagesearch.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.androidsx.imagesearch.R;
import com.androidsx.imagesearch.util.ImageWorker;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends Fragment implements OnClickListener
{
    private static final String sImageDataExtra = "extra_image_data";

    private static Callbacks sDummyCallbacks = new Callbacks()
    {
        public void loadImage(String imageUrl, ImageView imageView)
        {
        }

        @Override
        public void onImageViewClick()
        {
        }
    };

    private String mImageUrl;
    private ImageView mImageView;
    private Callbacks mCallbacks = sDummyCallbacks;

    // /////////////////
    // Lifecycle methods
    // /////////////////

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     * 
     * @param imageUrl
     *            The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImageDetailFragment newInstance(String imageUrl)
    {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString(sImageDataExtra, imageUrl);
        f.setArguments(args);

        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(sImageDataExtra) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        // Pass clicks on the ImageView to the parent activity to handle
        mImageView.setOnClickListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mCallbacks.loadImage(mImageUrl, mImageView);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mImageView != null)
        {
            // Cancel any pending image work
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        if (!(activity instanceof Callbacks))
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        mCallbacks = sDummyCallbacks;
    }

    // /////////////////
    // UI Event handlers
    // /////////////////

    @Override
    public void onClick(View v)
    {
        mCallbacks.onImageViewClick();
    }

    // /////////////
    // Inner classes
    // /////////////

    public static interface Callbacks
    {
        void loadImage(String imageUrl, ImageView imageView);

        void onImageViewClick();
    }
}
