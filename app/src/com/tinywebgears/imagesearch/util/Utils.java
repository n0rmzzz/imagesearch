package com.tinywebgears.imagesearch.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.StrictMode;

import com.tinywebgears.imagesearch.view.ImageDetailActivity;
import com.tinywebgears.imagesearch.view.ImageGridActivity;

/**
 * Class containing some static utility methods.
 */
public class Utils
{
    private Utils()
    {
    };

    @TargetApi(11)
    public static void enableStrictMode()
    {
        if (Utils.hasGingerbread())
        {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll()
                    .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll().penaltyLog();

            if (Utils.hasHoneycomb())
            {
                threadPolicyBuilder.penaltyFlashScreen();
                vmPolicyBuilder.setClassInstanceLimit(ImageGridActivity.class, 1).setClassInstanceLimit(
                        ImageDetailActivity.class, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    public static boolean hasFroyo()
    {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
}
