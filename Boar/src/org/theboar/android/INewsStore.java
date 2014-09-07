/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package org.theboar.android;

import java.util.Date;

/**
 * Represents a class used for storage of headlines (and possibly whole
 * articles) on the device.
 * @authors Michael, Snehil
 */
public interface INewsStore
{

	/* Used by the user to request that the internal store is
	 * refreshed with new content from the website.
	 * Returns the number of new articles or -1 if the 
	 * refresh failed.
	 */
	public int requestRefresh();

	/* Use this to store headlines and articles pulled from the
	 * website
	 */
	public boolean storeHeadline(IHeadline newHeadline);

	/* Gets headlines between certain dates 
	 * If dateFrom is null it gets all stories 
	 */
	public IHeadlineList getHeadlines(Date dateFrom, Date dateTo);

	/* All of these get the last n (some number) of headlines.
	 * If n is negative, it gets all applicable headlines.
	 */
	public IHeadlineList getHeadlines(int lastN);

	public IHeadlineList getFavourites(int lastN);

//	public IHeadlineList getNew(int lastN);

//	public IHeadlineList getHeadlines(String category, int lastN);

	/* Methods that return headlines based on a search query */

//	public IHeadlineList basicSearch(String query, int lastN);

}
