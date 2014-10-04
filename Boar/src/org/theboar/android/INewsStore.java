package org.theboar.android;

import org.json.JSONObject;

public interface INewsStore
{

	public abstract boolean checkIfNewHeadlines(int categoryId);

	public abstract IHeadlineList headlinesFromCategory(int categoryId, int pageNum, int count, IHeadlineListener hl_listener, boolean checkAgain);

	public abstract IHeadlineList headlinesFromSearch(String query, int page, int count, IHeadlineListener hl_listener);

	public abstract JSONObject downloadJSON(String requestURL, int categoryId);

	public abstract IHeadlineList parseJSONtoHeadlineList(int count, JSONObject jObject, boolean isFromCache);

	public abstract IHeadlineList headlinesFromFavourites(IHeadlineListener hl_listener);

	public abstract IHeadlineList headlinesFromTag(String slug, int page, int count, IHeadlineListener hl_listener);

	IHeadlineList headlinesFromFeaturedNews(int pageNum, int count, IHeadlineListener hl_listener);

}
