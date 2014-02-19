package org.theboar.adroid;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.boar.R;

import android.app.Activity;
import android.content.Context;
import android.content.ClipData.Item;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabletActivity extends Activity {
	boolean isTablet = false;

	private NewsStore newsStore;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tablet);
		populateNews();
	}

	private void populateNews() {
		
		newsStore = new NewsStore();

		LinearLayout l1 = (LinearLayout) findViewById(R.id.tablet_lld1);
		LinearLayout l2 = (LinearLayout) findViewById(R.id.tablet_lld2);
		LinearLayout l3 = (LinearLayout) findViewById(R.id.tablet_lld3);

		View newsItems = null;
		ImageView iv = null, star = null;
		LinearLayout categoryColor;
		FrameLayout content_isNew;
		TextView authorName = null, newsDate = null, newsName = null;

//			l3.setVisibility(LinearLayout.GONE); l3 is GONE by default
		Log.d("Print","isPortrait:" + isPortrait(this));
		if (!isTablet(this)) {
			if (isPortrait(this)) l2.setVisibility(LinearLayout.GONE);
			else if (!isPortrait(this)) l2.setVisibility(LinearLayout.VISIBLE);
		}
		else if (isTablet(this)) {
			isTablet = true;
			l3.setVisibility(LinearLayout.VISIBLE);
			if (isPortrait(this)) l3.setVisibility(LinearLayout.GONE);
			else if (!isPortrait(this)) l3.setVisibility(LinearLayout.VISIBLE);
		}
		//-------------------------------Retrieve Headlines------------------------------------------------
		/**Do this however you like. E.g fetching from xml from internet or some feed etc. In the end, required is:
		 * Image of the News : Drawable d
		 * News Title: String
		 * News Date: String
		 * News Author: String
		 * favourited: boolean (if Favourited)
		 */
		
		//-------------------------MAKE decoding ASYNCTASK----------------------------
		List<IHeadline> hl_list = newsStore.getHeadlines(10).getList();
		for (int i = 0; i < hl_list.size(); i++) {

			//--------------------calculating the height of content-------------------------
			//In case of some error--------------------------
			Drawable d = hl_list.get(i).getImage();
			if (d != null) {
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
				Log.d("Print",": " + hl_list.get(i).getHeadline());
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
				if (i % 4 == 0) //in order to randomise for now
					categoryColor.setBackgroundColor(getResources().getColor(R.color.orange));
				//--------------------------------News Title||-----------------------------
				newsName = (TextView) newsItems.findViewById(R.id.topicname);
				//TODO return title String
//				String newsTitle = "";
				if (i % 3 == 0) ////in order to randomise for now
					newsName.setText(hl_list.get(i).getHeadline());
				//-----------------------------------------News Author------------------------------------
				authorName = (TextView) newsItems.findViewById(R.id.author_name);
//				String aName = "Snehil Is Awesome!";
//				authorName.setText(aName);
				//-------------------------------------------Date----------------------------------------
				newsDate = (TextView) newsItems.findViewById(R.id.content_date);
				//TODO return date
				//String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
				String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
				newsDate.setText(currentDateTimeString);
				//-----------------------------------If New or Favourite--------------------------------------
				star = (ImageView) newsItems.findViewById(R.id.content_star);
				//TODO true false is news is favourited
				boolean isFavourite = false;//provide if the news is in favourite
//				if (isFavourite)  star.setImageDrawable(getResources().getDrawable(R.drawable.starFalse));

				content_isNew = (FrameLayout) newsItems.findViewById(R.id.content_isNewLay);
				//TODO return boolean if new or not
				if (i % 4 == 0) content_isNew.setVisibility(FrameLayout.INVISIBLE);
				else content_isNew.setVisibility(FrameLayout.VISIBLE);

				//-------------------------------------------------------------------------------------------
//				Log.v("print","SCREEN SIZE: "	+ getResources().getConfiguration().screenLayout);
				//-------------------------------Finally Add View (Old Tablet Code)-----------------------------------------

				switch (getNextLayoutAdd(i,isTablet)) {
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
}
