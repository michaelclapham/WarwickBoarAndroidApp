package org.theboar.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

public class NewsStore implements INewsStore
{

	public static final String boarJSON = "http://theboar.org/?json=1";
	private static Context context;

	public NewsStore(Context ctx) {
		context = ctx;
	}

	public JSONArray getJSONHeadlines()
	{
		//RequestTask1 rt = new RequestTask1(list);
		//rt.execute("http://www.theboar.org/?json=1");

		String result = null;

		// File for local cache of JSON
		String path = context.getFilesDir().getAbsolutePath();
		File file = new File(path + "warwick_boar_latest_json.txt");

		// Check if there is network available
		if (CNS.isNetworkConnected(context)) {
			requestRefresh();
		}
		// Load from JSON cache
		int length = (int) file.length();

		byte[] bytes = new byte[length];

		try {
			FileInputStream in = new FileInputStream(file);
			try {
				in.read(bytes);
			} finally {
				in.close();
			}
		} catch (IOException e3) {
			//
		}

		String contents = new String(bytes);
		result = contents;

		// Return array of JSON objects
		JSONArray jArray = null;

		// Now parse JSON
		//Create JSON Object
		if (result != null) {
			try {
				JSONObject jObject = new JSONObject(result);
				jArray = jObject.getJSONArray("posts");
			} catch (JSONException jex) {
				//
			}
		}
		return jArray;
	}

	@Override
	public int requestRefresh()
	{
		return doRefresh(boarJSON,"warwick_boar_latest_json.txt");
	}

	public int doRefresh(String requestURL, String cacheString)
	{
		// File for local cache of JSON
		String path = context.getFilesDir().getAbsolutePath();
		File file = new File(path + cacheString);

		// Used to store the downloaded JSON
		String result = "";

		// Changed to true if downloading the JSON worked
		boolean downloadWorked = false;

		// Check for internet connectivity
		if (CNS.isNetworkConnected(context)) {
			InputStream inputStream = null;
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
				HttpPost httppost = new HttpPost(requestURL);
				httppost.setHeader("Content-type","application/json");

				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();

				inputStream = entity.getContent();
				// json is UTF-8 by default
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"),8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null)
				{
					sb.append(line + "\n");
				}
				result = sb.toString();
				downloadWorked = true;
			} catch (Exception e) {
				downloadWorked = false;
				TabletActivity.activity.runOnUiThread(new Runnable() {
					public void run()
					{
						Toast.makeText(TabletActivity.activity,"An error occurred",Toast.LENGTH_LONG).show();
					}
				});
			} finally {
				try {
					if (inputStream != null) inputStream.close();
				} catch (Exception squish) {}
			}

			/*Now take result JSON string and save it to a file
			 	if the download worked*/
			if (downloadWorked) {
				try {
					FileOutputStream stream = new FileOutputStream(file);
					try {
						stream.write(result.getBytes());
					} finally {
						stream.close();
					}
				} catch (IOException e2) {
					downloadWorked = false;
				}
			}
		}
		if (downloadWorked) { return 1; }
		return 0;
	}

	public IHeadlineList fetchHeadlines(int lastN, String requestURL, String cacheFile, IHeadlineListener hl_listener)
	{
		HeadlineList list = new HeadlineList(lastN);

		// Check if there is network available
		if (CNS.isNetworkConnected(context)) {
			doRefresh(requestURL,cacheFile);
		}
		//RequestTask1 rt = new RequestTask1(list);
		//rt.execute("http://www.theboar.org/?json=1");

		String result = null;

		// File for local cache of JSON
		String path = context.getFilesDir().getAbsolutePath();
		File file = new File(path + cacheFile);

		// Load from JSON cache
		int length = (int) file.length();

		byte[] bytes = new byte[length];

		try {
			FileInputStream in = new FileInputStream(file);
			try {
				in.read(bytes);
			} finally {
				in.close();
			}
		} catch (IOException e3) {}

		String contents = new String(bytes);
		result = contents;

		// Now parse JSON
		//Create JSON Object
		if (result != null) {
			try {
				JSONObject jObject = new JSONObject(result);

				String pagesTotal = "0";
				String countTotal = "0";
				try {
					pagesTotal = jObject.getString("pages");
					countTotal = jObject.getString("count_total");
				} catch (org.json.JSONException e) {
					Log.d("PRINT","JSON Pages or Count Undefined");
				}

				JSONArray jArray = jObject.getJSONArray("posts");
				for (int i = 0; i < jArray.length(); i++) {
//					Log.i("PRINT","JArray i=" + i);
					JSONObject story = jArray.getJSONObject(i);
					Headline head = parseHeadlineJSON(story);
					try {
						list.addHeadline(head);
						// Create loading string
						String[] ls = new String[3];
						ls[0] = "Loading: " + (i + 1) + "/" + jArray.length();
						ls[1] = pagesTotal;
						ls[2] = countTotal;
						// Inform listener that new headline was parsed
						hl_listener.onHeadlineParsed(head,ls);
					} catch (Exception e) {
						Log.e("PRINT","JSON Exception! i=" + i);
						//
					}
				}
				list.setDoneLoading(true);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public static Headline parseHeadlineJSON(JSONObject story) throws JSONException, ParseException
	{
		Headline head = new Headline();
		String storyTitle = story.getString("title");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date datePublished = sdf.parse(story.getString("date"));
		String imageURL = story.getJSONObject("thumbnail_images").getJSONObject("medium").getString("url");

		head.setHeadlineTitle((Html.fromHtml(storyTitle)).toString());
		head.setImageUrl(imageURL);
		head.setDatePublished(datePublished);
		head.setAuthor(story.getJSONObject("author").getString("name"));
		head.setPageUrl(story.getString("url"));
		head.storeHTML(story.getString("content"));
		head.setCategory(Category.parseCategoryID(story));
		return head;
	}

	@Override
	public IHeadlineList getHeadlines(int lastN)
	{
		HeadlineList list = new HeadlineList(lastN);

		String result = null;
		// File for local cache of JSON
		String path = context.getFilesDir().getAbsolutePath();
		File file = new File(path + "warwick_boar_latest_json.txt");

		boolean downloadWorked = false;

		// Check for internet connectivity
		if (CNS.isNetworkConnected(context)) {
			// Load synchronously
			DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
			HttpPost httppost = new HttpPost(boarJSON);
			// Depends on your web service
			httppost.setHeader("Content-type","application/json");

			InputStream inputStream = null;
			try {
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();

				inputStream = entity.getContent();
				// json is UTF-8 by default
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"),8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null)
				{
					sb.append(line + "\n");
				}
				result = sb.toString();
				downloadWorked = true;
			} catch (Exception e) {
				downloadWorked = false;
			} finally {
				try {
					if (inputStream != null) inputStream.close();
				} catch (Exception squish) {}
			}

			/*Now take result JSON string and save it to a file
			 	if the download worked*/
			if (downloadWorked) {
				try {
					FileOutputStream stream = new FileOutputStream(file);
					try {
						stream.write(result.getBytes());
					} finally {
						stream.close();
					}
				} catch (IOException e2) {
					downloadWorked = false;
				}
			}
		} else {
			// If internet not connected load from JSON cache
			int length = (int) file.length();

			byte[] bytes = new byte[length];

			try {
				FileInputStream in = new FileInputStream(file);
				try {
					in.read(bytes);
				} finally {
					in.close();
				}
			} catch (IOException e3) {
				//
			}

			String contents = new String(bytes);
			result = contents;
		}

		// Now parse JSON
		//Create JSON Object
		if (result != null) {
			try {
				JSONObject jObject = new JSONObject(result);
				JSONArray jArray = jObject.getJSONArray("posts");
				for (int i = 0; i < jArray.length(); i++) {
					JSONObject story = jArray.getJSONObject(i);
					Headline head = new Headline();

					String storyTitle = story.getString("title");
					String imageURL = story.getJSONObject("thumbnail_images").getJSONObject("medium").getString("url");
					String dateTimeString = story.getString("date");
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date datePublished = df.parse(dateTimeString);

					head.setHeadlineTitle((Html.fromHtml(storyTitle)).toString());
					head.setImageUrl(imageURL);
					head.setDatePublished(datePublished);
					head.setAuthor(story.getJSONObject("author").getString("name"));
					head.storeHTML(story.getString("content"));
					head.setPageUrl(story.getString("url"));
					head.setCategory(Category.parseCategoryID(story));
					list.addHeadline(head);
				}
				list.setDoneLoading(true);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	@Override
	public IHeadlineList getFavourites(int lastN)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IHeadlineList getHeadlinesFromCategory(int categoryId, int pageNum, int lastN, IHeadlineListener hl_listener)
	{
		String categoryRequestURL = Category.getCategoryRequestURL(categoryId,pageNum);
		return fetchHeadlines(lastN,categoryRequestURL,
				Category.getCacheFileName(categoryId),hl_listener);
	}

	public IHeadlineList getHeadlinesFromSearch(String query, int page, int lastN, IHeadlineListener hl_listener)
	{
		query = query.replaceAll(" ","%20");
		if (query.endsWith(" ")) query = query.substring(0,query.length() - 2);
		return fetchHeadlines(lastN,boarJSON + "&s=" + query + "&page=" + page,
				"warwick_boar_latest_json",hl_listener);
	}

	//----------------------------------------------UNUSED----------------------------------------------------

	public static void downloadFile(String fileURL, String fileName, String directory)
	{
		try {
			URL u = new URL(fileURL);
			HttpURLConnection c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.connect();
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + directory);
			dir.mkdirs();
			File file = new File(dir,fileName);

			FileOutputStream f = new FileOutputStream(file);

			InputStream in = c.getInputStream();

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = in.read(buffer)) > 0) {
				f.write(buffer,0,len1);
			}
			f.close();
		} catch (Exception e) {
			Log.d("Downloader",e.getMessage());
		}

	}

	@Override
	public IHeadlineList getHeadlines(Date dateFrom, Date dateTo)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean storeHeadline(IHeadline newHeadline)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
