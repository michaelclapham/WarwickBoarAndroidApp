package org.theboar.android;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.ClipData.Item;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;

public class TabletActivity extends Activity implements IHeadlineListener {
	// Used to determine if the screen is large enough for more layouts
	private boolean isTablet = false;
	
	// Layout for each column of news items
	private LinearLayout l1, l2, l3;

	// Object used to access cache of news articles, download news articles, and associated images
	private NewsStore newsStore;
	
	private int currentCategory = Category.HOMEPAGE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_tablet);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		populateNews();
	}


	private void populateNews()
	{

		newsStore = new NewsStore(getApplicationContext());

		l1 = (LinearLayout) findViewById(R.id.tablet_lld1);
		l2 = (LinearLayout) findViewById(R.id.tablet_lld2);
		l3 = (LinearLayout) findViewById(R.id.tablet_lld3);
		
		

//			l3.setVisibility(LinearLayout.GONE); l3 is GONE by default
		Log.d("Print","isPortrait:" + isPortrait(this));
		if (!isTablet(this))
		{
			if (isPortrait(this)) l2.setVisibility(LinearLayout.GONE);
			else if (!isPortrait(this)) l2.setVisibility(LinearLayout.VISIBLE);
		}
		else if (isTablet(this))
		{
			isTablet = true;
			l3.setVisibility(LinearLayout.VISIBLE);
			if (isPortrait(this)) l3.setVisibility(LinearLayout.GONE);
			else if (!isPortrait(this)) l3.setVisibility(LinearLayout.VISIBLE);
		}
		
		HeadlineAsyncTask hat = new HeadlineAsyncTask();
		hat.execute("home");

	}

	private class HeadlineAsyncTask extends AsyncTask<String, Object, Void> implements IHeadlineListener
	{

		@Override
		protected Void doInBackground(String... params) {
			newsStore.getHeadlines2(10,this);
			return null;
		}

		@Override
		public void onHeadlineParsed(IHeadline hl) {
			publishProgress(hl,"Loading: 0%");
		}
		
		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
			Headline hl = (Headline) values[0];
			String loadingMessage = (String) values[1];
			// Do stuff on UI thread
			addHeadlineToView(hl);
		}

	}


	private int getNextLayoutAdd(int i, boolean largeScreen) {
		int L = 1;
		if (largeScreen == false) {
			if (isPortrait(this)) {
				L = 1;
			}
			else {
				int k2 = i % 2;
				L = k2 == 0 ? 1 : 2;
			}
		}
		else {//Have 3 ||| Layouts
			int k3 = i % 3;
			if (k3 == 0) L = 1;
			else if (k3 == 1) L = 2;
			else L = 3;
		}
		return L;
	}
	
	public static int convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return (int) px;
	}

	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
		& Configuration.SCREENLAYOUT_SIZE_MASK)
		>= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public boolean isPortrait(Context context) {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) return true;
		else return false;
	}
	
	/* Used for when an article is touched */
	private class MyTouchListener implements OnTouchListener {
		
		private String touchUrl;
		
		public MyTouchListener(String touchUrl) {
			this.touchUrl = touchUrl;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_UP){
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(touchUrl));
				startActivity(i);
			}
			return true;
		}
		
	}

	// Increases everytime we parse and add a headline to the view
	private int headlinesParsedSoFar = 0;
	
	@Override
	public void onHeadlineParsed(IHeadline hl) {
			addHeadlineToView(hl);
	}


	private void addHeadlineToView(IHeadline hl) {
		View newsItems = null;
		ImageView iv = null, star = null;
		LinearLayout categoryColor;
		FrameLayout content_isNew;
		TextView authorName = null, newsDate = null, newsName = null;

		//--------------------calculating the height of content-------------------------
		//In case of some error--------------------------
		Drawable d = hl.getImage();
		if (d != null)
		{
			int hMin = convertDpToPixel((float) 150,this);
			int hMax = convertDpToPixel((float) 700,this);
			int hReal = d.getMinimumHeight();
			int hFinal = hMin;
			if (hReal > hMax)
				hFinal = hMax;

			//---------------------------------------------------------
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					hFinal);
			Log.d("Print",": " + hl.getHeadline());
			Log.d("Print","hSmallest:" + hMin
					+ ", hLargest:"
					+ hMax + ", heightPIX:" + hFinal);

			//-----------------------------INFLATE + IMAGE--------------------------------
			newsItems = getLayoutInflater().inflate(R.layout.content_fragment,null,false);
			iv = (ImageView) newsItems.findViewById(R.id.content_newsImage);
			iv.setImageDrawable(d);
			iv.setLayoutParams(params);

			//---------------------------------News Type Colour---------------------------
			//---------------------------------News Type Colour---------------------------
			//---------------------------------News Type Colour---------------------------

			categoryColor = (LinearLayout) newsItems.findViewById(R.id.content_typecolor);
			//TODO return color.  Ideally they would already be defined in res/values/color
			categoryColor.setBackgroundColor(Category.getCategoryColour(hl.getCategory(),getResources()));

			// News Type Name
			TextView categoryName = (TextView) newsItems.findViewById(R.id.category_name);
			categoryName.setText(Category.getCategoryNameShort(hl.getCategory()).toUpperCase());
			//--------------------------------News Title||-----------------------------
			newsName = (TextView) newsItems.findViewById(R.id.topicname);
			//TODO return title String
//			String newsTitle = "";
			//if (i % 3 == 0) ////in order to randomise for now
			newsName.setText(hl.getHeadline());
			//-----------------------------------------News Author------------------------------------
			authorName = (TextView) newsItems.findViewById(R.id.author_name);
//			String aName = "Snehil Is Awesome!";
			authorName.setText(hl.getAuthor());
			//-------------------------------------------Date----------------------------------------
			newsDate = (TextView) newsItems.findViewById(R.id.content_date);
			//TODO return date
			//String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
			String dateTimeString = DateFormat.getDateInstance().format(hl.getDatePublished());
			Log.i(this.toString(),"DATE IS: " + hl.getDatePublished().toString());
			newsDate.setText(dateTimeString);
			//-----------------------------------If New or Favourite--------------------------------------
			star = (ImageView) newsItems.findViewById(R.id.content_star);
			//TODO true false is news is favourited
			boolean isFavourite = false;//provide if the news is in favourite
//			if (isFavourite)  star.setImageDrawable(getResources().getDrawable(R.drawable.starFalse));

			content_isNew = (FrameLayout) newsItems.findViewById(R.id.content_isNewLay);
			//TODO return boolean if new or not
			if (headlinesParsedSoFar % 4 == 0) content_isNew.setVisibility(FrameLayout.INVISIBLE);
			else content_isNew.setVisibility(FrameLayout.VISIBLE);

			String clickUrl = hl.getPageUrl();
			newsItems.setOnTouchListener(new MyTouchListener(clickUrl));
			
			// Increase number of headlines parsed
			headlinesParsedSoFar++;

			//-------------------------------------------------------------------------------------------
//			Log.v("print","SCREEN SIZE: "	+ getResources().getConfiguration().screenLayout);
			//-------------------------------Finally Add View (Old Tablet Code)-----------------------------------------

			switch (getNextLayoutAdd(headlinesParsedSoFar,isTablet))
			{
			case 1:
				l1.addView(newsItems);
				break;
			case 2:
				l2.addView(newsItems);
				break;
			case 3:
				l3.addView(newsItems);
				break;
			}
		}
		
	}
}
