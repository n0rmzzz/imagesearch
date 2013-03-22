package com.androidsx.imagesearch.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Application;

import com.androidsx.imagesearch.task.BaseAsyncTask;

public class CustomApplication extends Application
{
    // Maps activity class names to the list of currently running AsyncTasks.
    private Map<String, List<BaseAsyncTask<?, ?, ?>>> mActivityTaskMap;

    public CustomApplication()
    {
        mActivityTaskMap = new HashMap<String, List<BaseAsyncTask<?, ?, ?>>>();
    }

    public void removeTask(BaseAsyncTask<?, ?, ?> task)
    {
        for (Entry<String, List<BaseAsyncTask<?, ?, ?>>> entry : mActivityTaskMap.entrySet())
        {
            List<BaseAsyncTask<?, ?, ?>> tasks = entry.getValue();
            for (int i = 0; i < tasks.size(); i++)
                if (tasks.get(i) == task)
                {
                    tasks.remove(i);
                    break;
                }

            if (tasks.size() == 0)
            {
                mActivityTaskMap.remove(entry.getKey());
                return;
            }
        }
    }

    public void addTask(Activity activity, BaseAsyncTask<?, ?, ?> task)
    {
        String key = activity.getClass().getCanonicalName();
        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(key);
        if (tasks == null)
        {
            tasks = new ArrayList<BaseAsyncTask<?, ?, ?>>();
            mActivityTaskMap.put(key, tasks);
        }
        tasks.add(task);
    }

    public void detach(Activity activity)
    {
        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName());
        if (tasks != null)
            for (BaseAsyncTask<?, ?, ?> task : tasks)
                task.setActivity(null);
    }

    public void attach(Activity activity)
    {
        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName());
        if (tasks != null)
            for (BaseAsyncTask<?, ?, ?> task : tasks)
                task.setActivity(activity);
    }
}
