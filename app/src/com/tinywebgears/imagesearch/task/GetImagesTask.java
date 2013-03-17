package com.tinywebgears.imagesearch.task;

import java.io.IOException;

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

public class GetImagesTask extends AsyncTask<String, Void, String>
{
    private static final String TAG = "GetImgesTask";

    private HttpClient client = new DefaultHttpClient();

    @Override
    protected String doInBackground(String... params)
    {
        Log.w(TAG, "Task execution params: " + params);
        String searchEngineId = "004032200943388316906:xcng10daspw";
        // TODO: Fix the Android app key. Using a browser key for tests only.
        String apiKey = "AIzaSyDtqxkys3TWrfw4kFwbvfEZUVzUGwQLeeY";
        // String apiKey = "AIzaSyB9nLGzeOiMnpGqsBYiCD5cWr_OCRM_JNc";
        String keyword = "table";
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
                JSONObject firstItem = (JSONObject) items.get(0);
                String thumbnailUrl = firstItem.getJSONObject("pagemap").getString("cse_thumbnail");
                String imageUrl = firstItem.getJSONObject("pagemap").getString("cse_image");
                return firstItem.toString();
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
        return "";
    }

    @Override
    protected void onPostExecute(String result)
    {
        Log.w(TAG, "Task execution result: " + result);
    }
}
