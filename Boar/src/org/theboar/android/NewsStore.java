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

	public NewsStore(Context ctx) {
		context = ctx;
	}

	public IHeadlineList getHeadlinesFromCategory(int categoryId, int pageNum, int lastN, IHeadlineListener hl_listener)
	{
		this.pageNum = pageNum;
		String categoryRequestURL = Category.getCategoryRequestURL(categoryId,pageNum);
		return fetchHeadlines(lastN,categoryRequestURL,hl_listener,categoryId);
	}

	public IHeadlineList getHeadlinesFromSearch(String query, int page, int lastN, IHeadlineListener hl_listener)
	{
		this.pageNum = page;
		query = query.replaceAll(" ","%20");
		if (query.endsWith(" ")) query = query.substring(0,query.length() - 2);
		return fetchHeadlines(lastN,boarJSON + "&s=" + query + "&page=" + page,hl_listener,-1);
	}

	//----------------------------------------JSON--------------------------------

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
		JSONObject joNEIL = null;
		try {
			joNEIL = (JSONObject) new JSONTokener(IOUtils.toString(new URL(requestURL))).nextValue();
			String categoryName = Category.getCategoryName(categoryId,false);
			if (categoryName != null && pageNum == 1) { //only add 1st page to cache
				addJSONtoCache(joNEIL,categoryName);
			}
		}
		catch (Exception e) {
			Log.e(CNS.LOGPRINT,"An Error Occurred retrieving JSON.");
		}
		return joNEIL;
	}

	private JSONObject getJSONFromCache(String categoryName)
	{
		JSONObject jObject = null;
		String path = context.getCacheDir().getAbsolutePath();
		File cacheFile = new File(path + categoryName + ".json");
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

	private void addJSONtoCache(final JSONObject json, final String category)
	{
		Runnable r = new Runnable() {
			public void run()
			{
				try {
					String path = context.getCacheDir().getAbsolutePath();
					File cacheFile = new File(path + category + ".json");
					if (!cacheFile.exists()) cacheFile.createNewFile();

					FileUtils.writeStringToFile(cacheFile,json.toString());
//					JSONParser jsonParser = new JSONParser();
//					JSONArray a = (JSONArray) jsonParser.parse(new FileReader("c:\\exer4-courses.json"));
				}
				catch (IOException e) {}
			}
		};
		new Thread(r).start();

	}

	//----------------------------------------ARTICLES--------------------------------

	private IHeadlineList parseJSONtoHeadlineList(int lastN, IHeadlineListener hl_listener, JSONObject jObject)
	{
		if (jObject == null) { return null; }

		HeadlineList list = new HeadlineList(lastN);
		String pagesTotal = "0";
		String countTotal = "0";
		try {
			pagesTotal = jObject.getString("pages");
			countTotal = jObject.getString("count_total");
		}
		catch (org.json.JSONException e) {
			Log.e(CNS.LOGPRINT,"JSON Pages or Count Undefined");
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
					list.addHeadline(parseHeadlineJSON(story));

					String[] ls = new String[3];
					ls[0] = "Loading: " + (i + 1) + "/" + jArray.length();
					ls[1] = pagesTotal;
					ls[2] = countTotal;

					hl_listener.onHeadlineParsed(parseHeadlineJSON(story),ls);
				}
				catch (JSONException e) {}
			}
		}
		list.setDoneLoading(true);
		return list;
	}

	public Headline parseHeadlineJSON(JSONObject story)
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
			head.setCategory(Category.parseCategoryID(story));
			return head;
		}
		catch (JSONException e) {}
		catch (ParseException e1) {}
		return null;
	}

	private IHeadlineList fetchHeadlines(int lastN, String categoryRequestURL, IHeadlineListener hl_listener, int categoryId)
	{
		JSONObject downloadedJSON = downloadJSON(categoryRequestURL,categoryId);

		if (downloadedJSON == null) {//retrieve from cache
			String categoryName = Category.getCategoryName(categoryId,false);
			if (categoryName != null) { //eg. query
				downloadedJSON = getJSONFromCache(categoryName);
			}
		}
		IHeadlineList hList = parseJSONtoHeadlineList(lastN,hl_listener,downloadedJSON);
		return hList;
	}

}
