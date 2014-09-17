/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package org.theboar.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import java.util.Date;

/**
 * Stores information about each headline. Can potentially contain
 * the full article if this is requested.
 * @author Michael
 */
public interface IHeadline
{

	public String getHeadline();

	public Drawable getImage();

	/* This could be needed but is up for debate. 
	 * getHighResImage could be used to indicate we are requesting an
	 * image for an article the user is now reading, so pulling a
	 * high resolution image is quite important. For browsing headlines a lower
	 * resolution image stored in cache is acceptable.
	 */
	public Drawable getHighResImage();

	public void setTags(String[] tags);
	public String[] getTags();

	public int getCategory();

	public boolean isFavourite(Context context);

	public boolean isNew();

	/* Returns the value of isFavourite after the change.
		fav - true if we want this article to be favourited
			- false if we want this article to be unfavourited */
//	public boolean setFavourite(boolean fav);

	/* Returns the value of isNew after the change.
		fav - true if the article is now not new (could be like setting
			an email as unread.)
			- false if this article has been read or seen in the list
			of headlines depending on UI implementation */
	public boolean setNew(boolean isNew);

	public String getAuthor();

	public Date getDatePublished();

	/* I'd assume the articles will be stored as HTML */
	public String getArticleHTML();

	/* Get URL to page of article */
	public String getPageUrl();

	/* Get URL to page of article */
	public String getImageUrl();

	boolean setFavourite(boolean fav, Context context);

}
