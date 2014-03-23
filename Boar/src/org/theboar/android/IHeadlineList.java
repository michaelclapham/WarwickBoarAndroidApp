/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.theboar.android;

import java.util.List;

/**
 * Used to return a list of headlines. Allows a list of headlines to be
 * used before the query that generated it has completed.
 * For example if a query is a date range this may take a while to search
 * through local cache and then check online also.
 * If a query is a text search then it can provide results from the cache
 * and then results from an online search.
 * @author Michael
 */
public interface IHeadlineList {
	
	/* The list of headlines so far */
	public List<IHeadline> getList();
	
	/* The list of headlines ready to be added to the GUI. This list acts merely as
	 * a buffer. Once a headline is parsed it is added to this list, and once it is
	 * added to the GUI it is removed from this list */
	public List<IHeadline> getReadyList();
	
	/* String presented to user whilst some headlines are loading.
	 * Depending on the query
	 * It Could be "Loading: ..." or maybe "Searching for: Nigel Thrift"
	 */
	public String getLoadingString();
	
	/* True once we are convinced the list of headlines is complete */
	public boolean doneLoading();
	
	/* The number of headlines loaded so far */ 
	public int numLoaded();
	
	/* The total number of headlines that the list will finally contain.
	 * If negative this value is unknown (i.e in an online search we don't
	 * know how many items it will contain
	 */
	public int numTotal();
	
}
