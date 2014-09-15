package org.theboar.android;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.res.Resources;
import android.util.Log;

public class Category
{

	public static final String[] MENU_STRINGS = { "Home", "News", "Comment", "Features",
			"Lifestyle", "Money",
			"Arts", "Books", "Film", "Games", "Music", "Science-Tech", "Travel", "TV", "Sport",
			"Favourites" };

	public static final int HOME = 0
			, NEWS
			= 1
			, COMMENT
			= 2
			, FEATURES
			= 3
			, LIFESTYLE
			= 4
			, MONEY
			= 5
			, ARTS
			= 6
			, BOOKS
			= 7
			, FILM
			= 8
			, GAMES
			= 9
			, MUSIC
			= 10
			, SCI_TECH
			= 11
			, TRAVEL
			= 12
			, TV
			= 13
			, SPORT
			= 14
			, FAVOURITES
			= 15
			;

	public static final int OTHER = 80;

	public static String getCategoryName(int index, boolean fullName, boolean shortName)
	{
		if (index == SCI_TECH && fullName == true) return "Science and Technology";
		else if (index == SCI_TECH && shortName == true) return "Sci - Tech";
		else if (index == OTHER) return "Other";
		try {
			return MENU_STRINGS[index];
		}
		catch (Exception e) {
			return null;
		}
	}

	public static String getCategoryRequestURL(int categoryId, int pageNum, int count)
	{
		if (categoryId != HOME) {
			return "http://theboar.org/category/" + MENU_STRINGS[categoryId].toLowerCase()
					+ "/?json=1&page=" + pageNum + "&count=" + count;
		}
		else {
			return NewsStore.boarJSON + "&page=" + pageNum;
		}
	}

	public static int getCategoryColourText(int categoryId, Resources res)
	{
		if (categoryId == HOME) return res.getColor(R.color.black_60);
		else if (categoryId == FAVOURITES) return res.getColor(R.color.black_60);
		else return getCategoryColourBar(categoryId,res);
	}

	public static int getCategoryColourBar(int categoryId, Resources res)
	{
		switch (categoryId) {
		case HOME:
			return res.getColor(R.color.home_colour);
		case NEWS:
			return res.getColor(R.color.news);
		case COMMENT:
			return res.getColor(R.color.comment);
		case FEATURES:
			return res.getColor(R.color.features);
		case LIFESTYLE:
			return res.getColor(R.color.lifestyle);
		case MONEY:
			return res.getColor(R.color.money);
		case ARTS:
			return res.getColor(R.color.arts);
		case BOOKS:
			return res.getColor(R.color.books);
		case FILM:
			return res.getColor(R.color.film);
		case GAMES:
			return res.getColor(R.color.games);
		case MUSIC:
			return res.getColor(R.color.music);
		case SCI_TECH:
			return res.getColor(R.color.science_tech);
		case TRAVEL:
			return res.getColor(R.color.travel);
		case TV:
			return res.getColor(R.color.tv);
		case SPORT:
			return res.getColor(R.color.sport);
		case OTHER:
			return res.getColor(R.color.black);
		case FAVOURITES:
			return res.getColor(R.color.favourites_white);
//		case PHOTOGRAPHY:
//			return res.getColor(R.color.photography);
		}
		return res.getColor(R.color.white);
	}

	public static int getCategoryIDFromName(String name)
	{
		for (int i = 0; i < MENU_STRINGS.length; i++) {
			if (MENU_STRINGS[i].equalsIgnoreCase(name)) { return i; }
		}
		return OTHER;
	}

	/** 
	 * Takes a JSON Object for an article and returns the category ID 
	 * **/
	public static int parseCategoryID(JSONArray cats)
	{
		try {
			for (int i = 0; i < cats.length(); i++) {
				String catSlug = cats.getJSONObject(i).getString("slug");
				for (int j = 0; j < MENU_STRINGS.length; j++) {
					if (catSlug.equalsIgnoreCase(MENU_STRINGS[j])) { return j; }
				}
			}
		}
		catch (JSONException e) {}
		return OTHER;
	}

	public static boolean isCategoryColorDark(int catID)
	{
		if (catID == TV || catID == TRAVEL || catID == FILM) return true;
		return false;
	}

	//-------------------------------------------------------------------------------------------------------------------

}
