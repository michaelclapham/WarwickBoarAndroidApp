package org.theboar.android;

import java.io.File;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Headline implements IHeadline
{

	private String uniqueId;
	private String headlineTitle;
	private boolean favourite = false;
	private boolean isNew = false;
	private String authorName = "Unknown";
	private Date datePublished;
	private Drawable lowResImage = null;
	private boolean hasImage = false;
	private String[] tags;
	private int category = Category.OTHER;
	private String internalHTML;
	private String pageUrl;
	private String imageUrl;
	private JSONObject jsonStory;
	private String[] imageDimension;
	private int commentsNum;

	public Headline() {
		//
	}

	public void setCategory(int category)
	{
		this.category = category;
	}

	public void setLowResImage(Drawable lowResImage)
	{
		this.lowResImage = lowResImage;
		hasImage = true;
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

	@Deprecated
	public Drawable getImage()
	{
		if (hasImage) {
			if (lowResImage != null) {
				return lowResImage;
			} else {
				return null;
			}
		}
		return null;
	}

	@Deprecated
	public Drawable getHighResImage()
	{
		if (hasImage) {
			if (lowResImage != null) {
				return lowResImage;
			} else {
				return null;
			}
		}
		return null;
	}

	@Override
	public String[] getTags()
	{
		return tags;
	}

	@Override
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
	public boolean isNew()
	{
		return isNew;
	}

	@Override
	public boolean setFavourite(boolean fav, Context context)
	{
		favourite = fav;
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
	@Override
	public boolean setNew(boolean isNew)
	{
		this.isNew = isNew;
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

	public void setJSONStory(JSONObject story)
	{
		this.jsonStory = story;

	}

	public JSONObject getJSONStory()
	{
		return jsonStory;
	}

	public void setImageDimensions(String[] imageDimension)
	{
		this.imageDimension = imageDimension;

	}
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

}
