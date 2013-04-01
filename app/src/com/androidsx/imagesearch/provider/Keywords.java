package com.androidsx.imagesearch.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A static list used to mimic a content provider.
 */
// TODO: Replace this with a decent content provider.
public class Keywords
{
    public static String[] keywords = new String[] { "Cool", "Great", "Awesome", "Yuk", "Aweful", "Bright" };

    public static List<String> getKeywords(String prefix)
    {
        if (prefix == null || prefix.length() < 1)
            return Arrays.asList(keywords);
        List<String> result = new ArrayList<String>();
        for (String keyword : keywords)
        {
            // TODO: Locale
            if (keyword.toLowerCase().startsWith(prefix.toLowerCase()))
                result.add(keyword);
        }
        return result;
    }
}
