package com.tinywebgears.imagesearch.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.tinywebgears.imagesearch.provider.Images;
import com.tinywebgears.imagesearch.view.ImageGridFragment;

public class GetImagesTask extends AsyncTask<String, Void, List<Pair<String, String>>>
{
    private static final String TAG = "GetImgesTask";

    private HttpClient client = new DefaultHttpClient();
    private final ImageGridFragment imageGridFragment;

    // TODO: FIXME: Implement this properly.
    public GetImagesTask(ImageGridFragment imageGridFragment)
    {
        this.imageGridFragment = imageGridFragment;
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
        String urlString = urlPrefix + "?key=" + apiKey + "&cx=" + searchEngineId + "&q=" + keyword;
        try
        {
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
                List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
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
                return result;
            }
            else
            {
                Log.w(TAG, "HTTP Error: " + status);
                return null;
            }
        }
        catch (ClientProtocolException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    protected void onPostExecute(List<Pair<String, String>> result)
    {
        Log.w(TAG, "Task execution result: " + result);
        List<String> thumbnailUrls = new ArrayList<String>();
        List<String> imageUrls = new ArrayList<String>();
        for (Pair<String, String> entry : result)
        {
            thumbnailUrls.add(entry.first);
            imageUrls.add(entry.second);
        }
        Images.imageThumbUrls = thumbnailUrls.toArray(Images.imageThumbUrls);
        Images.imageUrls = imageUrls.toArray(Images.imageThumbUrls);

        imageGridFragment.processGetImageTaskResult();
    }
}
