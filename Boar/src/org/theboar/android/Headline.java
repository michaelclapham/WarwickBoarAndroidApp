package org.theboar.android;

import java.util.Date;

import android.graphics.drawable.Drawable;

public class Headline implements IHeadline {

	private int uniqueId = -1;
	private String headlineTitle;
	private boolean favourite = false;
	private boolean isNew = false;
	private String authorName = "Unknown";
	private Date datePublished;
	private Drawable lowResImage = null;
	private boolean hasImage = false;
	private String[] tags;
	private int catagory = Catagory.OTHER;
	private String internalHTML;
	
	public Headline() {
		//
	}
	
	public void setCatagory(int catagory) {
		this.catagory = catagory;
	}
	
	public void setLowResImage(Drawable lowResImage) {
		this.lowResImage = lowResImage;
		hasImage = true;
	}
	
	public void storeHTML(String HTML){
		internalHTML = HTML;
	}
	
	public void setHeadlineTitle(String headlineTitle) {
		this.headlineTitle = headlineTitle;
	}
	
	@Override
	public String getHeadline() {
		return headlineTitle;
	}

	@Override
	public Drawable getImage() {
		if(hasImage){
			if(lowResImage != null){
				return lowResImage;
			} else {
				return null;
			}
		}
		return null;
	}

	@Override
	public Drawable getHighResImage() {
		if(hasImage){
			if(lowResImage != null){
				return lowResImage;
			} else {
				return null;
			}
		}
		return null;
	}

	@Override
	public String[] getTags() {
		return tags;
	}

	@Override
	public int getCategory() {
		return Catagory.NEWS;
	}

	@Override
	public boolean isFavourite() {
		return favourite;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	@Override
	public boolean setFavourite(boolean fav) {
		favourite = fav;
		return true;
	}

	@Override
	public boolean setNew(boolean isNew) {
		this.isNew = isNew;
		return true;
	}

	@Override
	public String getAuthor() {
		return authorName;
	}

	@Override
	public Date getDatePublished() {
		return datePublished;
	}

	@Override
	public String getArticleHTML() {
		return internalHTML;
	}

	public int getUniqueId() {
		return uniqueId;
	}

}
