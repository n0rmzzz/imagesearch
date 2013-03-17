package com.tinywebgears.imagesearch.task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import android.util.Log;

public class GetImagesTask extends AsyncTask<String, Void, String> {

	private static final String TAG = "GetImgesTask";

	@Override
	protected String doInBackground(String... params) {
		Log.w(TAG, "Task execution params: " + params);
		String searchEngineId = "004032200943388316906:xcng10daspw";
		String apiKey = "AIzaSyB9nLGzeOiMnpGqsBYiCD5cWr_OCRM_JNc";
		String keyword = "table";
		String urlString = "https://www.googleapis.com/customsearch/v1?key="
				+ apiKey + "&cx=" + searchEngineId + "&q=" + keyword;
		try {
			URL url = new URL(urlString);
			URLConnection urlConnection = url.openConnection();
			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			try {
				readStream(in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	@Override
	protected void onPostExecute(String result) {
		Log.w(TAG, "Task execution result: " + result);
	}

	private String readStream(InputStream in) {
		return "";
	}
}
