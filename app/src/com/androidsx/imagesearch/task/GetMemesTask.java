package com.androidsx.imagesearch.task;

import android.app.Activity;

import com.androidsx.imagesearch.view.ImageGridFragment;

public class GetMemesTask extends GetImagesTask
{
    public GetMemesTask(Activity activity, ImageGridFragment imageGridFragment, int startingIndex, boolean fresh)
    {
        super(activity, imageGridFragment, startingIndex, fresh);
    }

    @Override
    protected String createQueryKeyword(String keyword)
    {
        return "\"" + keyword + "\" AND \"Rage Face\" OR \"Meme\"";
    }
}
