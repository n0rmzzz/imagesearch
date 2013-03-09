package com.tinywebgears.imagesearch.view;

import android.os.Build;

public class VersionDependentConstants
{
    public static int getListItemLayout()
    {
        return (Build.VERSION.SDK_INT >= 11) ? android.R.layout.simple_list_item_activated_1
                : android.R.layout.simple_list_item_1;
    }
}
