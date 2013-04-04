package com.androidsx.imagesearch.task;

import android.app.Activity;

public class GetMemesTask extends GetImagesTask
{
    public GetMemesTask(Activity activity, int startingIndex, boolean fresh)
    {
        super(activity, startingIndex, fresh);
    }

    @Override
    protected String createQueryKeyword(String keyword)
    {
        return "\"" + keyword + "\" AND \"Rage Face\" OR \"Meme\"";
    }
}
