package org.theboar.android;

import java.util.Date;

import android.graphics.drawable.Drawable;

public class Headline implements IHeadline
{

	private int uniqueId = -1;
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
	public int getCategory()
	{
		return category;
	}

	@Override
	public boolean isFavourite()
	{
		return favourite;
	}

	@Override
	public boolean isNew()
	{
		return isNew;
	}

	@Override
	public boolean setFavourite(boolean fav)
	{
		favourite = fav;
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

	public int getUniqueId()
	{
		return uniqueId;
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

}
