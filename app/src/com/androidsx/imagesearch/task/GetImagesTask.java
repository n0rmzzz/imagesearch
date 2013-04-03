package com.androidsx.imagesearch.task;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;

import com.androidsx.imagesearch.provider.Images;
import com.androidsx.imagesearch.view.ImageGridFragment;

public class GetImagesTask extends BaseAsyncTask<String, Void, List<Pair<String, String>>>
{
    private static final String TAG = "GetImgesTask";

    private static final int sMinItesmPerRequest = 24;
    private static final int sMaxQueriesPerRequest = 4;

    private int mStartingIndex;
    private boolean mFresh;
    private HttpClient client = new DefaultHttpClient();

    public GetImagesTask(Activity activity, ImageGridFragment imageGridFragment, int startingIndex, boolean fresh)
    {
        super(activity, Callbacks.class);
        mStartingIndex = startingIndex;
        mFresh = fresh;
    }

    @Override
    protected List<Pair<String, String>> doInBackground(String... params)
    {
        Log.w(TAG, "Task execution params: " + params);
        assert params.length >= 1;
        String keyword = params[0];
        String searchEngineId = "004032200943388316906:xcng10daspw";
        // TODO: Fix the Android app key. Using a browser key for tests only.
        String apiKey = "AIzaSyDtqxkys3TWrfw4kFwbvfEZUVzUGwQLeeY";
        // String apiKey = "AIzaSyB9nLGzeOiMnpGqsBYiCD5cWr_OCRM_JNc";
        String urlPrefix = "https://www.googleapis.com/customsearch/v1";
        try
        {
            List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
            for (int queriesCount = 0; queriesCount < sMaxQueriesPerRequest && result.size() < sMinItesmPerRequest; queriesCount++)
            {
                String encodedKeyword = URLEncoder.encode(createQueryKeyword(keyword), "utf-8");
                String urlString = urlPrefix + "?key=" + apiKey + "&cx=" + searchEngineId + "&q=" + encodedKeyword
                        + "&start=" + mStartingIndex;
                StringBuilder portURL = new StringBuilder(urlString);
                HttpGet get = new HttpGet(portURL.toString());
                HttpResponse r = client.execute(get);
                int status = r.getStatusLine().getStatusCode();
                if (status == 200)
                {
                    HttpEntity e = r.getEntity();
                    String data = EntityUtils.toString(e);
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray items = jsonObject.getJSONArray("items");
                    for (int i = 0; i < items.length(); i++)
                    {
                        JSONObject item = (JSONObject) items.get(i);
                        JSONObject pageMap = item.getJSONObject("pagemap");
                        String imageUrl = null;
                        String thumbnailUrl = null;
                        try
                        {
                            imageUrl = ((JSONObject) pageMap.getJSONArray("cse_image").get(0)).getString("src");
                            thumbnailUrl = ((JSONObject) pageMap.getJSONArray("cse_thumbnail").get(0)).getString("src");
                        }
                        catch (JSONException jsone)
                        {
                            if (imageUrl != null && thumbnailUrl == null)
                                thumbnailUrl = imageUrl;
                        }
                        if (thumbnailUrl != null && imageUrl != null)
                            result.add(new Pair<String, String>(thumbnailUrl, imageUrl));
                    }
                    try
                    {
                        mStartingIndex = (Integer) ((JSONObject) ((JSONArray) jsonObject.getJSONObject("queries").get(
                                "nextPage")).get(0)).get("startIndex");
                    }
                    catch (Exception ee)
                    {
                        Log.w(TAG, "Error getting information about next page: " + ee.getMessage(), ee);
                        break;
                    }
                }
                else
                {
                    Log.w(TAG, "HTTP Error: " + r.getStatusLine());
                    break;
                }
            }
            return result;
        }
        catch (Exception e)
        {
            Log.w(TAG, "Error occurred while getting images: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    protected void onPostExecuteCallback(List<Pair<String, String>> result)
    {
        Log.w(TAG, "Task execution result: " + result);
        List<String> thumbnailUrls = new ArrayList<String>();
        List<String> imageUrls = new ArrayList<String>();
        for (Pair<String, String> entry : result)
        {
            thumbnailUrls.add(entry.first);
            imageUrls.add(entry.second);
        }
        if (mFresh)
        {
            Images.imageThumbUrls = new ArrayList<String>(thumbnailUrls);
            Images.imageUrls = new ArrayList<String>(imageUrls);
        }
        else
        {
            Images.imageThumbUrls.addAll(thumbnailUrls);
            Images.imageUrls.addAll(imageUrls);
        }

        ((Callbacks) mActivity).onImagesReady(result.size());
    }

    protected String createQueryKeyword(String keyword)
    {
        return "\"" + keyword + "\"";
    }

    public static interface Callbacks
    {
        void onImagesReady(int count);
    }
}
