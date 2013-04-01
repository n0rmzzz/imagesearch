package com.androidsx.imagesearch.provider;

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
        assert imageUrls.length == imageThumbUrls.length;
        if (count < 1)
            return imageUrls.length;
        return count;
    }

    public static void setCount(int count)
    {
        Images.count = count;
    }

    public static String[] imageUrls = new String[] {};

    public static String[] imageThumbUrls = new String[] {};
}
