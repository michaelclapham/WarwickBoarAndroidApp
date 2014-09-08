package org.theboar.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

public class NewsStore implements INewsStore
{

	public static final String boarJSON = "http://theboar.org/?json=1";
	private static Context context;
	private int pageNum;
	private IHeadlineListener hl_listener;

	public NewsStore(Context ctx) {
		context = ctx;
	}

	@Override
	public IHeadlineList headlinesFromCategory(int categoryId, int pageNum, int count, IHeadlineListener hl_listener)
	{
		this.pageNum = pageNum;
		this.hl_listener = hl_listener;
		String categoryRequestURL = Category.getCategoryRequestURL(categoryId,pageNum);
		return generateHeadlines(count,categoryRequestURL,categoryId);
	}

	@Override
	public IHeadlineList headlinesFromFavourites(IHeadlineListener hl_listener)
	{

		this.hl_listener = hl_listener;
		String path = context.getCacheDir().getAbsolutePath();
		File favFile = new File(path + "favourites" + ".json");
		JSONObject favJSON = getJSONFromFile(favFile);

		if (favJSON != null) {//retrieve from cache
			int numPosts = 0;
			try {
				numPosts = Integer.parseInt(favJSON.getString("count_total"));
			}
			catch (NumberFormatException e) {}
			catch (JSONException e) {}
			IHeadlineList hList = parseJSONtoHeadlineList(numPosts,favJSON);
			return hList;
		}
		return null;
	}
	@Override
	public IHeadlineList headlinesFromSearch(String query, int page, int count, IHeadlineListener hl_listener)
	{
		this.pageNum = page;
		this.hl_listener = hl_listener;
		query = query.replaceAll(" ","%20");
		if (query.endsWith(" ")) query = query.substring(0,query.length() - 2);
		return generateHeadlines(count,boarJSON + "&s=" + query + "&page=" + page,-1);
	}

	//----------------------------------------JSON--------------------------------

	@Override
	public JSONObject downloadJSON(String requestURL, int categoryId)
	{
		if (CNS.isNetworkConnected(context) == false) {
			BoarActivity.activity.runOnUiThread(new Runnable() {
				public void run()
				{
					Toast.makeText(BoarActivity.activity,"Please check your internet connection",Toast.LENGTH_LONG).show();
				}
			});
			return null;
		}
		JSONObject jsonObj = null;
		try {
			jsonObj = (JSONObject) new JSONTokener(IOUtils.toString(new URL(requestURL))).nextValue();
			String categoryName = Category.getCategoryName(categoryId,false);
			if (categoryName != null && pageNum == 1) { //only add 1st page to cache
				String path = context.getCacheDir().getAbsolutePath();
				File cacheFile = new File(path + categoryName + ".json");
				writeJSONtoFile(jsonObj,cacheFile);
			}
		}
		catch (Exception e) {
			Log.e(CNS.LOGPRINT,"An Error Occurred retrieving JSON.");
		}
		return jsonObj;
	}

	public static JSONObject getJSONFromFile(File cacheFile)
	{
		JSONObject jObject = null;

		if (cacheFile.exists()) {
			int length = (int) cacheFile.length();
			byte[] bytes = new byte[length];
			try {
				FileInputStream in = new FileInputStream(cacheFile);
				try {
					in.read(bytes);
				}
				finally {
					in.close();
				}
			}
			catch (IOException e3) {}
			String contents = new String(bytes);
			if (contents != null) {
				try {
					jObject = new JSONObject(contents);
				}
				catch (JSONException e) {}
			}
		}
		return jObject;

	}

	public static void writeJSONtoFile(final JSONObject json, final File cacheFile)
	{
		Runnable r = new Runnable() {
			public void run()
			{
				try {
					if (!cacheFile.exists()) cacheFile.createNewFile();
					FileUtils.writeStringToFile(cacheFile,json.toString());
				}
				catch (IOException e) {}
			}
		};
		new Thread(r).start();

	}

	//----------------------------------------ARTICLES--------------------------------

	public IHeadlineList parseJSONtoHeadlineList(int count, JSONObject jObject)
	{
		if (jObject == null) { return null; }

		HeadlineList list = new HeadlineList(count);
		String pagesTotal = "0";
		String countTotal = "0";
		try {
			pagesTotal = jObject.getString("pages");
			countTotal = jObject.getString("count_total");
		}
		catch (org.json.JSONException e) {
//			Log.e(CNS.LOGPRINT,"JSON Pages or Count Undefined");
		}

		JSONArray jArray = null;
		try {
			jArray = jObject.getJSONArray("posts");
		}
		catch (JSONException e1) {}
		if (jArray != null) {
			for (int i = 0; i < jArray.length(); i++) {
				try {
					JSONObject story;
					story = jArray.getJSONObject(i);
					list.addHeadline(parseJSONtoHeadline(story));

					String[] ls = new String[3];
					ls[0] = "Loading: " + (i + 1) + "/" + jArray.length();
					ls[1] = pagesTotal;
					ls[2] = countTotal;

					Headline hl = parseJSONtoHeadline(story);
					hl_listener.onHeadlineParsed(hl,ls);
				}
				catch (JSONException e) {}
			}
		}
		list.setDoneLoading(true);
		return list;
	}

	private Headline parseJSONtoHeadline(JSONObject story)
	{
		try {
			Headline head = new Headline();
			String storyTitle = story.getString("title");

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date datePublished = sdf.parse(story.getString("date"));

			String imageURL = null;
			try {
				imageURL = story.getJSONObject("thumbnail_images").getJSONObject("medium").getString("url");
			}
			catch (Exception e) {}

			head.setHeadlineTitle((Html.fromHtml(storyTitle)).toString());
			head.setImageUrl(imageURL);
			head.setDatePublished(datePublished);
			head.setAuthor(story.getJSONObject("author").getString("name"));
			head.setPageUrl(story.getString("url"));
			head.storeHTML(story.getString("content"));
			head.setCategory(Category.parseCategoryID(story.getJSONArray("categories")));
			head.setJSONStory(story);
			head.setUniqueId(story.getString("id"));
			return head;
		}
		catch (JSONException e) {}
		catch (ParseException e1) {}
		return null;
	}

	private IHeadlineList generateHeadlines(int count, String categoryRequestURL, int categoryId)
	{
		JSONObject downloadedJSON = downloadJSON(categoryRequestURL,categoryId);

		if (downloadedJSON == null) {//retrieve from cache
			String categoryName = Category.getCategoryName(categoryId,false);
			if (categoryName != null) { //eg. query
				String path = context.getCacheDir().getAbsolutePath();
				File cacheFile = new File(path + categoryName + ".json");
				downloadedJSON = getJSONFromFile(cacheFile);
			}
		}
		IHeadlineList hList = parseJSONtoHeadlineList(count,downloadedJSON);
		return hList;
	}

}
