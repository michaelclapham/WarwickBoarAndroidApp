package org.theboar.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;

public class Headline implements IHeadline
{

	private String uniqueId;
	private String headlineTitle;
	private boolean favourite = false;
	private boolean isNew = false;
	private String authorName = "Anonymous"; //Anonymous 
	private Date datePublished;
	private String[] tags;
	private int category = Category.OTHER;
	private String internalHTML;
	private String pageUrl;
	private String imageUrl;
	private JSONObject jsonStory;
	private String[] imageDimension;
	private int commentsNum;
	private Map<String, Node> mapCom;
	private HashMap<Integer, String> categoriesMap;

	public Headline() {
		//
	}

	public void setCategory(int cat)
	{
		this.category = cat;
	}

	public static int parseCategory(JSONArray json)
	{
		return Category.parseCategoryID(json);
	}

	public void storeHTML(String HTML)
	{
		internalHTML = HTML;
	}

	public void setHeadlineTitle(String headlineTitle)
	{
		this.headlineTitle = headlineTitle;
	}

	@Override
	public String getHeadline()
	{
		return headlineTitle;
	}

	@Override
	public String[] getTags()
	{
		return tags;
	}

	public void setTags(String[] tags)
	{

		this.tags = tags;

	}

	@Override
	public int getCategory()
	{
		return category;
	}

	@Override
	public boolean isFavourite(Context context)
	{
		if (favourite == true) return true;
		File favFile = CNS.getFavouriteFile();
		JSONObject jsonObj = NewsStore.getJSONFromFile(favFile);
		if (jsonObj == null) {
			return false;
		} else {
			try {
				JSONArray jArray = jsonObj.getJSONArray("posts");
				checkFavourite(jArray,false);
				return favourite;
			}
			catch (JSONException e) {}
		}
		return false;
	}

	private JSONArray checkFavourite(JSONArray jArray, boolean remove)
	{
		if (jArray != null) {
			for (int i = 0; i < jArray.length(); i++) {
				try {
					JSONObject story = jArray.getJSONObject(i);
					if (story.getString("id").equals(uniqueId)) {
						favourite = true;
						if (remove) {
							jArray = removeJSONArray(jArray,uniqueId);
							favourite = false;
						}
					}
				}
				catch (JSONException e) {}
			}
		}
		return jArray;
	}

	public static JSONArray removeJSONArray(JSONArray jArray, String id)
	{

		JSONArray newJarray = new JSONArray();
		try {
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject story = jArray.getJSONObject(i);
				if (!story.getString("id").equals(id)) {
					newJarray.put(jArray.get(i));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return newJarray;
	}

	@Override
	public boolean setFavourite(boolean fav, Context context)
	{
		favourite = fav;

		//cool. now modify the file to store favs
		File favFile = CNS.getFavouriteFile();
		JSONObject jsonObj = NewsStore.getJSONFromFile(favFile);
		JSONArray jsArr = new JSONArray();
		try {
			if (jsonObj == null) {
				jsonObj = new JSONObject();
			} else {
				jsArr = jsonObj.getJSONArray("posts");
			}
			if (fav) {
				jsArr.put(jsonStory);
			} else {
				jsArr = checkFavourite(jsArr,true);
			}
			jsonObj.put("count",jsArr.length());
			jsonObj.put("posts",jsArr);
			NewsStore.writeJSONtoFile(jsonObj,favFile);
		}
		catch (JSONException e) {}
		return true;
	}

	public void setAuthor(String authorName)
	{
		this.authorName = authorName;
	}

	public void setDatePublished(Date datePublished)
	{

		this.datePublished = datePublished;
	}

	@Override
	public String getAuthor()
	{
		return authorName;
	}

	@Override
	public Date getDatePublished()
	{
		return datePublished;
	}

	@Override
	public String getArticleHTML()
	{
		return internalHTML;
	}

	public String getUniqueId()
	{
		return uniqueId;
	}
	public void setUniqueId(String id)
	{
		this.uniqueId = id;
	}

	public String getPageUrl()
	{
		return pageUrl;
	}

	public void setPageUrl(String pageUrl)
	{
		this.pageUrl = pageUrl;
	}

	public void setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;

	}
	public String getImageUrl()
	{
		return imageUrl;
	}

	@Override
	public void setImageDimensions(String[] imageDimension)
	{
		this.imageDimension = imageDimension;

	}

	@Override
	public int[] getImageDimensions()
	{
		int[] tmp = { 0, 0 };
		try {
			tmp[0] = Integer.parseInt(imageDimension[0]);
			tmp[1] = Integer.parseInt(imageDimension[1]);
		}
		catch (Exception e) {}
		return tmp;
	}

	public int getCommentsNum()
	{
		return commentsNum;
	}

	public void setCommentsNum(int num)
	{
		this.commentsNum = num;
	}

	public Map<String, Node> getComments()
	{
		return mapCom;
	}

	public void setComments(Map<String, Node> map)
	{
		this.mapCom = map;
	}

	class Node
	{
		String id;
		String comment;
		Node parent;
		ArrayList<String> children;
		String name;

		public Node(String id, String comment, String name, Node Parent, ArrayList<String> children) {
			this.id = id;
			this.comment = comment;
			this.name = name;
			this.parent = Parent;
			this.children = children;

		}
	}

	public static String[] parseTags(JSONArray tagArray)
	{
		String[] tags = new String[0];
		try {
			tags = new String[tagArray.length()];
			for (int i = 0; i < tagArray.length(); i++) {
				tags[i] = ((JSONObject) tagArray.get(i)).getString("slug");
			}
		}
		catch (JSONException e) {}
		return tags;
	}

	public void setComments(final JSONArray jsonArray)
	{
//		final JSONArray commentsTree = jsonArray;

		Thread t = new Thread() {

			public void run()
			{

				HashMap<String, Node> mapCom = new HashMap<String, Node>();
				try {
					for (int i = 0; i < jsonArray.length(); i++) {
						String id = ((JSONObject) jsonArray.get(i)).getString("id");
						String idParent = ((JSONObject) jsonArray.get(i)).getString("parent");
						String name = ((JSONObject) jsonArray.get(i)).getString("name");
						String content = ((JSONObject) jsonArray.get(i)).getString("content");

						if (!mapCom.containsKey(id)) {
							if (idParent.equals("0")) {
								mapCom.put(id,new Node(id,content,name,null,new ArrayList<String>()));
							} else {
								Node parentNode = mapCom.get(idParent);
								Node subComment = new Node(id,content,name,parentNode,
										new ArrayList<String>());
								parentNode.children.add(subComment.id);
								mapCom.put(id,subComment);
							}
						}
					}
					setCommentsNum(mapCom.size());
					setComments(mapCom);
				}
				catch (Exception e) {
					Log.d(CNS.LOGPRINT,"Error Occurred setting Comments to map");
				}
			}
		};
		t.start();
	}

	public void setStory(JSONObject story)
	{
		jsonStory = story;
	}

	public static HashMap<Integer, String> parseCategories(JSONArray jsonArray)
	{
		HashMap<Integer, String> cats = new HashMap<Integer, String>();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				String title = ((JSONObject) jsonArray.get(i)).getString("title");
				int id = Integer.parseInt(((JSONObject) jsonArray.get(i)).getString("id"));
				cats.put(id,Html.fromHtml(title).toString());
			}
		}
		catch (JSONException e) {}
		return cats;
	}

	/**
	 * @return map [Integer, String] : id, title
	 */
	public HashMap<Integer, String> getCategoriesList()
	{
		return categoriesMap;
	}

	public void setCategoriesList(HashMap<Integer, String> map)
	{
		this.categoriesMap = map;

	}

}
