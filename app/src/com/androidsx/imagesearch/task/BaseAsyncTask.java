package com.androidsx.imagesearch.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.androidsx.imagesearch.view.CustomApplication;

public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
    protected CustomApplication mApp;
    protected Activity mActivity;
    private Class<?> mCallbackClass;

    public BaseAsyncTask(Activity activity, Class<?> callbackClass)
    {
        mActivity = activity;
        mApp = (CustomApplication) mActivity.getApplication();
        mCallbackClass = callbackClass;
    }

    public void setActivity(Activity activity)
    {
        mActivity = activity;
        if (mActivity == null)
            onActivityDetached();
        else
            onActivityAttached();
    }

    protected void onActivityAttached()
    {
    }

    protected void onActivityDetached()
    {
    }

    @Override
    protected void onPreExecute()
    {
        mApp.addTask(mActivity, this);
    }

    @Override
    protected void onCancelled()
    {
        mApp.removeTask(this);
    }

    @Override
    protected void onPostExecute(Result result)
    {
        mApp.removeTask(this);
        Log.i(BaseAsyncTask.class.getName(), "Async task result: " + result);
        if (mActivity == null)
            Log.w(BaseAsyncTask.class.getName(), "The activity wasn't there when I finished!");
        else if (!mCallbackClass.isInstance(mActivity))
            Log.w(BaseAsyncTask.class.getName(), "The activity " + mActivity.getClass().getName()
                    + " doesn't implement my callback interface!");
        else
            onPostExecuteCallback(result);
    }

    abstract protected void onPostExecuteCallback(Result result);
}
