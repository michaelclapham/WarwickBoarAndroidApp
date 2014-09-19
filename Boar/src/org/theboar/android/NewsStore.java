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

	public static final String NO_NEW = "no_new";
	public static final String HAS_NEW = "has_new";
	public static final String boarJSON = "http://theboar.org/?json=1";
	private static Context context;
	private int pageNum;
	private IHeadlineListener hl_listener;

	public NewsStore(Context ctx) {
		context = ctx;
	}

	public IHeadlineList headlinesFromCategory(int categoryId, int pageNum, int count, IHeadlineListener hl_listener, boolean checkAgain)
	{
		this.pageNum = pageNum;
		this.hl_listener = hl_listener;
		String categoryRequestURL = Category.getCategoryRequestURL(categoryId,pageNum,count);
		return generateHeadlines(count,categoryRequestURL,categoryId,checkAgain);
	}

	public boolean checkIfNewHeadlines(int categoryId)
	{
		String categoryRequestURL = Category.getCategoryRequestURL(categoryId,pageNum,1);
		JSONObject downloaded = downloadJSON(categoryRequestURL,categoryId);
		JSONObject cached = getJSONFromFile(CNS.getCategoryCacheFile(context,Category.getCategoryName(categoryId,false,false)));

		return !checkJSONPostsSame(downloaded,cached);
	}

	@Override
	public IHeadlineList headlinesFromFavourites(IHeadlineListener hl_listener)
	{

		this.hl_listener = hl_listener;
		File favFile = CNS.getFavouriteFile();
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

	public IHeadlineList headlinesFromSearch(String query, int page, int count, IHeadlineListener hl_listener)
	{
		this.pageNum = page;
		this.hl_listener = hl_listener;
		query = query.replaceAll(" ","%20");
		if (query.endsWith(" ")) query = query.substring(0,query.length() - 2);
		return generateHeadlines(count,boarJSON + "&s=" + query + "&page=" + page,-1,true);
	}

	@Override
	public IHeadlineList headlinesFromTag(String slug, int page, int count, IHeadlineListener hl_listener)
	{
		this.pageNum = page;
		this.hl_listener = hl_listener;
		return generateHeadlines(count,"http://theboar.org/tag/" + slug + "/?json=1&page=" + page
				+ "&count=" + count,-1,true);
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

		}
		catch (Exception e) {
			Log.e(CNS.LOGPRINT,"An Error Occurred retrieving JSON.");
		}
		return jsonObj;
	}

	private boolean checkJSONPostsSame(JSONObject json1, JSONObject json2)
	{
		try {

			JSONArray jArray1 = json1.getJSONArray("posts");
			String id1 = jArray1.getJSONObject(0).getString("id");

			JSONArray jArray2 = json2.getJSONArray("posts");
			String id2 = jArray2.getJSONObject(0).getString("id");

			if (id1.equals(id2)) return true;
		}
		catch (Exception e1) {}
		return false;
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

	public static void writeJSONtoFile(final JSONObject json, final File file)
	{
		Runnable r = new Runnable() {
			public void run()
			{
				try {
					if (!file.exists()) file.createNewFile();
					FileUtils.writeStringToFile(file,json.toString());
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
//					ls[0] = "Loading: " + (i + 1) + "/" + jArray.length();
					ls[0] = HAS_NEW;
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
			String[] imageDimension = { "0", "0" };
			try {
				boolean imgRes = CNS.getSharedPreferences(context).getBoolean("image_quality",true);

				JSONObject imgObj = story.getJSONObject("thumbnail_images").getJSONObject(imgRes
						? "large"
						: "medium");
				imageURL = imgObj.getString("url");
				imageDimension[0] = imgObj.getString("width");
				imageDimension[1] = imgObj.getString("height");
			}
			catch (Exception e) {}

			JSONArray tagArray = story.getJSONArray("tags");
			String[] tags = new String[tagArray.length()];
			for (int i = 0; i < tagArray.length(); i++) {
				tags[i] = ((JSONObject) tagArray.get(i)).getString("slug");
			}

			head.setHeadlineTitle((Html.fromHtml(storyTitle)).toString());
			head.setImageUrl(imageURL);
			head.setDatePublished(datePublished);
			head.setAuthor(story.getJSONObject("author").getString("name"));
			head.setPageUrl(story.getString("url"));
			head.setImageDimensions(imageDimension);
			head.storeHTML(story.getString("content"));
			head.setCategory(Category.parseCategoryID(story.getJSONArray("categories")));
			head.setTags(tags);
			head.setJSONStory(story);
			head.setUniqueId(story.getString("id"));
			return head;
		}
		catch (JSONException e) {
			Log.e(CNS.LOGPRINT,"JSON Exception");
		}
		catch (ParseException e1) {
			Log.e(CNS.LOGPRINT,"Parsing Exception");
		}
		return null;
	}

	private IHeadlineList generateHeadlines(int count, String categoryRequestURL, int categoryId, boolean checkAgain)
	{
		JSONObject downloadedJSON = null;
		if (checkAgain) { //only download from url when requested
			downloadedJSON = downloadJSON(categoryRequestURL,categoryId);
		}

		if (downloadedJSON == null || !checkAgain) {//retrieve from cache
			String categoryName = Category.getCategoryName(categoryId,false,false);
			if (categoryName != null) { //eg. query
				File cacheFile = CNS.getCategoryCacheFile(context,categoryName);
				downloadedJSON = getJSONFromFile(cacheFile);
				if (downloadedJSON == null) //if not cache then download from url
					downloadedJSON = downloadJSON(categoryRequestURL,categoryId);
			}
		}

		String categoryName = Category.getCategoryName(categoryId,false,false);
		if (categoryName != null && pageNum == 1) { //only add 1st page to cache
			File cacheFile = CNS.getCategoryCacheFile(context,categoryName);
			writeJSONtoFile(downloadedJSON,cacheFile);
		}

		IHeadlineList hList = parseJSONtoHeadlineList(count,downloadedJSON);
		return hList;
	}

}
