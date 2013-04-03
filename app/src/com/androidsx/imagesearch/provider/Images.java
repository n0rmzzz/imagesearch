package com.androidsx.imagesearch.provider;

import java.util.ArrayList;
import java.util.List;

/**
 * A static list used to mimic a content provider.
 */
// TODO: Replace this with a decent content provider.
public class Images
{
    /**
     * This filed is to keep track of the number of items displayed in the grid, since the last row is chopped.
     */
    private static int count = 0;

    public static int getCount()
    {
        assert imageUrls.size() == imageThumbUrls.size();
        if (count < 1)
            return imageUrls.size();
        return count;
    }

    public static void setCount(int count)
    {
        Images.count = count;
    }

    public static List<String> imageUrls = new ArrayList<String>();

    public static List<String> imageThumbUrls = new ArrayList<String>();
}
