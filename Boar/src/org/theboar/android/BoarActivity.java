package org.theboar.android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.theboar.android.Headline.Node;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.callback.ImageOptions;
import com.androidquery.util.AQUtility;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class BoarActivity extends Activity implements BottomReachedListener
{

	public static Activity activity;
	private BottomReachedListener btmListener;
	private AQuery aq;
	private Context context;

	private LinearLayout l1, l2, l3;
	private SlidingMenu menu;
//	private SliderLayout sliderShow;
	private HeadlineAsyncTask hat;

	private View currPos;
	private INewsStore newsStore;
	private int currentCategory;
	private Headline currHeadline;
	private float currHeadlineImageSize;

	public static final String VISIBLE = "vis", GONE = "gone", INVISIBLE = "inv";
	private static final String SEARCH = "Search", TAG = "Tag";
	public static int numCount = 10;

	private boolean populating = false;
	private boolean articleOpen = false;

	private int headlinesParsedSoFar = 0;
	private int pageNum;

//	private boolean forSearch;
	private String query;

	private boolean isFullScreenEnabled;
	private boolean[] beenReloaded;
	private boolean forTag;
	private String REQUEST;
	private String tagSlug;
	private BoarMagicalScrollView storyScroll;

	//----------------------------------Lifecycle------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);
		context = this;
		activity = this;
		btmListener = this;
		pageNum = 1;
		aq = new AQuery(this);
		AQUtility.setCacheDir(getCacheDir());
		//	-----------------------------------------

		//check and clean cache asyncly at every launch.
		int maxCacheSize = CNS.getSharedPreferences(this).getInt("max_cache",10);
		long triggerSize = maxCacheSize * 1000000;
		AQUtility.cleanCacheAsync(BoarActivity.activity,triggerSize,triggerSize);

		beenReloaded = new boolean[Category.MENU_STRINGS.length];
		for (int i = 0; i < beenReloaded.length; i++) {
			beenReloaded[i] = false;
		}

		if (CNS.isTablet(this)) {
			numCount = 12;
		}

		Bundle bundle = getIntent().getExtras();
//		forSearch = false;
		if (bundle != null && bundle.getBoolean("forSearch",false)) {
			REQUEST = SEARCH;
		} else if (bundle != null && bundle.getBoolean("forTag",false)) {
			REQUEST = TAG;
			tagSlug = bundle.getString("tag");
			populateNews(true,true);
		} else {
			setSlidingMenu();
			String catName = CNS.getSharedPreferences(this).getString("default_category","Home");
			currentCategory = Category.getCategoryIDFromName(catName);
			startCategory(currentCategory,null);
		}
		setOnClickListeners();
	}

	@Override
	public void onBackPressed()
	{
		View dropDown = findViewById(R.id.drop_down);
		if (dropDown.getVisibility() == View.VISIBLE) {
			vis(GONE,R.id.drop_down,true);
		}
		else if (articleOpen) {
			closeArticle();
		}
		else super.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		populating = true;
		refreshOrientationUX();

		List<View> l1List = new LinkedList<View>();
		List<View> l2List = new LinkedList<View>();
		List<View> l3List = new LinkedList<View>();

		View newsItem;

		int childCountL1 = l1.getChildCount();
		int childCountL2 = l2.getChildCount();
		int childCountL3 = l3.getChildCount();

		int mostChildCount = childCountL1;
		if (mostChildCount < childCountL2) mostChildCount = childCountL2;
		if (mostChildCount < childCountL3) mostChildCount = childCountL3;

		int totalItems = 0;
		for (int i = 0; i < mostChildCount; i++) {
			if (i < childCountL1) {
				newsItem = l1.getChildAt(i);
				int next = getNextLayoutAdd(totalItems);
				if (next == 1) l1List.add(newsItem);
				else if (next == 2) l2List.add(newsItem);
				else if (next == 3) l3List.add(newsItem);
				totalItems++;
			}
			if (i < childCountL2) {
				newsItem = l2.getChildAt(i);
				int next = getNextLayoutAdd(totalItems);
				if (next == 1) l1List.add(newsItem);
				else if (next == 2) l2List.add(newsItem);
				else if (next == 3) l3List.add(newsItem);
				totalItems++;
			}
			if (i < childCountL3) {
				newsItem = l3.getChildAt(i);
				int next = getNextLayoutAdd(totalItems);
				if (next == 1) l1List.add(newsItem);
				else if (next == 2) l2List.add(newsItem);
				else if (next == 3) l3List.add(newsItem);
				totalItems++;
			}
		}

		l1.removeAllViews();
		l2.removeAllViews();
		l3.removeAllViews();

		selectColumns();

		addViewsToLay(l1List,l1,500);
		addViewsToLay(l2List,l2,500);
		addViewsToLay(l3List,l3,500);

		new Handler().postDelayed(new Runnable() {
			public void run()
			{
				populating = false;
			}
		},l1List.size() * 250);
	}

	//----------------------------------Articles to Views------------------------------

	private void addViewsToLay(List<View> list, final LinearLayout lay, int delay)
	{
		for (final View view : list) {
			new Handler().postDelayed(new Runnable() {
				public void run()
				{
					lay.addView(view);
				}
			},delay);
		}
	}

	private void refreshOrientationUX()
	{
		if (menu != null) menu.setBehindWidthRes(R.dimen.slidingmenu_size);

		View storyPage = findViewById(R.id.story_root);
		setOrientationMargin(storyPage,false);

		if (articleOpen) {
			setOrientationMargin(findViewById(R.id.action_bar_main),false);
		}
	}

	private int setOrientationMargin(View storyPage, boolean isSize0)
	{
		int marginHor = (int) getResources().getDimension(R.dimen.articleMargHor);
		if (isSize0 == true) marginHor = 0;
		if (storyPage != null) {
			FrameLayout.LayoutParams llp = (FrameLayout.LayoutParams) storyPage.getLayoutParams();
			llp.leftMargin = marginHor;
			llp.rightMargin = marginHor;
		}
		Display display = getWindowManager().getDefaultDisplay();

		int width = display.getWidth() - (2 * marginHor);
		return width;
	}

	private void selectColumns()
	{
		l1.setVisibility(View.VISIBLE);
		boolean isPortrait = CNS.isPortrait(this);
		boolean isTablet = CNS.isTablet(this);

		if (!isTablet) {
			if (isPortrait) l2.setVisibility(LinearLayout.GONE);
			else if (!isPortrait) l2.setVisibility(LinearLayout.VISIBLE);
		}
		else if (isTablet) {
			l3.setVisibility(LinearLayout.VISIBLE);
			if (isPortrait) l3.setVisibility(LinearLayout.GONE);
			else if (!isPortrait) l3.setVisibility(LinearLayout.VISIBLE);
		}
	}

	private int getNextLayoutAdd(int i)
	{
		int L = 1;
		int h1 = l1.getHeight();
//		Log.d(CNS.LOGPRINT,"h1: " + h1);
		if (!CNS.isTablet(this)) { // isPhone
			if (CNS.isPortrait(this)) { //isPortrait
				L = 1;
			}
			else {	//isLandscape
				int k2 = i % 2;
				L = k2 == 0 ? 1 : 2;
			}
		}
		else { //isTablet

			if (CNS.isPortrait(this)) { //isPortrait
				int k2 = i % 2;
				L = k2 == 0 ? 1 : 2;
			}
			else { //isLandscape

				int k3 = i % 3;
				if (k3 == 0) L = 1;
				else if (k3 == 1) L = 2;
				else L = 3;
			}

		}
		return L;
	}

	//----------------------------------Main UI changes------------------------------

	private void setSlidingMenu()
	{
		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.attachToActivity(this,SlidingMenu.SLIDING_CONTENT);
		menu.setBehindWidthRes(R.dimen.slidingmenu_size);
		menu.setShadowDrawable(R.drawable.side_bar_shadow1);
		menu.setShadowWidthRes(R.dimen.slidingmenu_shadow_size);
		menu.setMenu(R.layout.menu);
		menu.setFadeDegree(0.35f);
		menu.setSlidingEnabled(true);

		final ListView lv;
		lv = (ListView) findViewById(R.id.menu_list);
//		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setAdapter(new MySimpleArrayAdapter(this,Category.MENU_STRINGS));
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				menu.toggle();
				startCategory(position,view);
			}

		});
	}

	public void setActionBarForCategory(int category, boolean changeHeading)
	{
		int colorTo = Category.getCategoryColourBar(category,getResources());

		vis(VISIBLE,R.id.drop_down_browser);
		View actionBar = findViewById(R.id.action_bar_main);

		View menu = findViewById(R.id.menu_button_img);
		View close = findViewById(R.id.close_button_img);
		View more = findViewById(R.id.more_button_img);
		View logo = findViewById(R.id.actionbar_logo_img);

		//----------------article open-----------------------
		if (articleOpen) {//in article View
			setOrientationMargin(actionBar,false);

			vis(GONE,R.id.back_button);
			vis(GONE,R.id.menu_button);

			vis(VISIBLE,R.id.close_button);
			vis(VISIBLE,R.id.drop_down_favourite);
			vis(VISIBLE,R.id.drop_down_share);
			vis(VISIBLE,R.id.actionbar_logo);
		}
		//		----------------article closed-----------------------
		else {
			setOrientationMargin(actionBar,true);

//			actionBar.getLayoutParams().width = LayoutParams.MATCH_PARENT;
			vis(GONE,R.id.close_button);
			vis(GONE,R.id.actionBar_shadow);
			if (category == Category.FAVOURITES || category == Category.HOME) {
				vis(VISIBLE,R.id.actionBar_shadow);
			}
			vis(GONE,R.id.drop_down_share);

			if (REQUEST == SEARCH || REQUEST == TAG) {
				vis(VISIBLE,R.id.actionBar_shadow);

				vis(VISIBLE,R.id.back_button);
				colorTo = getResources().getColor(R.color.white);

				vis(GONE,R.id.menu_button);
				vis(GONE,R.id.page_heading);
				vis(GONE,R.id.drop_down_share);
				vis(INVISIBLE,R.id.actionbar_logo);
				vis(VISIBLE,R.id.more_button);
				vis(VISIBLE,R.id.search_query);

			} else {
				vis(VISIBLE,R.id.menu_button);
			}

			if (category == Category.FAVOURITES) {
				vis(GONE,R.id.drop_down_browser);
			}
		}

		if (changeHeading) {
			String categoryName = Category.getCategoryName(category,true,false);
			TextView heading = (TextView) findViewById(R.id.page_heading);
			int colorText = Category.getCategoryColourText(category,getResources());
			heading.setText(categoryName);
			heading.setTextColor(colorText);
		}

		if (!articleOpen
				&& (category == Category.HOME ||
						category == Category.FAVOURITES || REQUEST == SEARCH || REQUEST == TAG)) { //set fonts to white
			setImage(menu,R.drawable.menu_black);
			setImage(more,R.drawable.more_black);
			setImage(close,R.drawable.close_black);
			setImage(logo,R.drawable.the_boar_logo);
		} else {
			setImage(menu,R.drawable.menu);
			setImage(more,R.drawable.more);
			setImage(close,R.drawable.close);
			setImage(logo,R.drawable.the_boar_logo_white);
		}

		View topBar = findViewById(R.id.top_bar_layout);
//		CNS.animateBackgroundColor(topBar,currColor,colorTo);
		topBar.setBackgroundColor(colorTo);

		//--------------------------------------------other ui elements--------------------
		if (currentCategory == Category.FAVOURITES) {
			vis(VISIBLE,R.id.favourite_detail_root_scroll);
		} else {
			vis(GONE,R.id.favourite_detail_root_scroll);
		}
	}

	protected void hideActionBar()
	{
		View actionBar = findViewById(R.id.action_bar_main);
		Animation slideUp = AnimationUtils.loadAnimation(context,R.anim.slide_up);
		actionBar.startAnimation(slideUp);
		actionBar.setVisibility(View.GONE);
		Log.d(CNS.LOGPRINT,"HIDDEN!");
	}

	protected void showActionBar()
	{
		View actionBar = findViewById(R.id.action_bar_main);
		Animation slideDown = AnimationUtils.loadAnimation(context,R.anim.slide_down);
		actionBar.startAnimation(slideDown);
		actionBar.setVisibility(View.VISIBLE);
	}

	private void setOnClickListeners()
	{

		l1 = (LinearLayout) findViewById(R.id.tablet_lld1);
		l2 = (LinearLayout) findViewById(R.id.tablet_lld2);
		l3 = (LinearLayout) findViewById(R.id.tablet_lld3);
//		sliderShow = (SliderLayout) findViewById(R.id.main_slider);

		findViewById(R.id.menu_button).setOnClickListener(clickEvent);
		findViewById(R.id.more_button).setOnClickListener(clickEvent);
		findViewById(R.id.close_button).setOnClickListener(clickEvent);
		findViewById(R.id.back_dark_underlay).setOnClickListener(clickEvent);
		findViewById(R.id.refresh_button).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_contact_us).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_settings).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_browser).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_favourite).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_share).setOnClickListener(clickEvent);
		findViewById(R.id.main_toast_root).setOnClickListener(clickEvent);
		findViewById(R.id.story_comment_expander).setOnClickListener(clickEvent);

		TextView heading = (TextView) findViewById(R.id.page_heading);
		heading.setTypeface(Typeface.createFromAsset(getAssets(),"Lato-Regular.ttf"));

		//to hide dropdown
		findViewById(R.id.dummy_actionBar_underlay).setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event)
			{
				if (findViewById(R.id.drop_down).getVisibility() == View.VISIBLE) {
					vis(GONE,R.id.drop_down,true);
				}
				return false;
			}
		});

		ScrollViewExt smartScroll = (ScrollViewExt) findViewById(R.id.scrollView_main);
		smartScroll.setBottomListener(btmListener);

		storyScroll = (BoarMagicalScrollView) findViewById(R.id.storyScrollView);
		storyScroll.iv = (ImageView) findViewById(R.id.story_newsImage);
		storyScroll.scaleFactor = 1.5;
		storyScroll.maxYOverscrollDistance = 0;
		storyScroll.getViewTreeObserver().addOnScrollChangedListener(new OnScrollChangedListener() {

			float itrY = 0;
			boolean isHidden = false;
			float itrUP = 0;
			float itrDOWN = 0;

			@Override
			public void onScrollChanged()
			{
				float y = ((ScrollView) findViewById(R.id.storyScrollView)).getScrollY();
				if (isFullScreenEnabled) {
					if (y <= itrY || y <= currHeadlineImageSize) { //scrolling up
						itrDOWN = 0;
						if ((isHidden && itrUP > 20) || (y < 50 && isHidden)) {
							showActionBar();
							isHidden = false;
							itrUP = 0;
						}
						itrUP++;
					}
					else if (y > itrY && y > currHeadlineImageSize) {
						itrUP = 0;
						if (!isHidden && itrDOWN > 20) {
							hideActionBar();
							isHidden = true;
							itrDOWN = 0;
						}
						itrDOWN++;
					}
					itrY = y;
				}
			}
		});

		if (REQUEST == TAG) {
			setActionBarForCategory(-1,false);
			vis(VISIBLE,R.id.back_button).setOnClickListener(clickEvent);

			TextView title = (TextView) findViewById(R.id.actionbar_title);
			title.setText("#" + tagSlug);
		}
		if (REQUEST == SEARCH) {
			setActionBarForCategory(-1,false);

			vis(VISIBLE,R.id.back_button).setOnClickListener(clickEvent);

			TextView title = (TextView) findViewById(R.id.actionbar_title);
			title.setText(REQUEST);

			vis(VISIBLE,R.id.search_area_root);
			final EditText textarea = (EditText) vis(VISIBLE,R.id.search_area);
			textarea.setOnEditorActionListener(new EditText.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					query = v.getText().toString();
					TextView txtQuery = (TextView) findViewById(R.id.search_query);
					txtQuery.setText("Searching for '" + query + "'");
					populateNews(true,true);

					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(textarea.getWindowToken(),0);

					return true;
				}
			});
		}

	}

	public OnClickListener clickEvent = new OnClickListener() {
		@Override
		public void onClick(View v)
		{
			switch (v.getId()) {
			case R.id.back_button:
				finish();
				break;
			case R.id.menu_button:
//				Animation anim2 = CNS.Animate(moreButton,CNS.ROTATE,0,180,500,true);
//				moreButton.startAnimation(anim2);
				menu.toggle();
				break;
			case R.id.more_button:
				View dropDown = findViewById(R.id.drop_down);
				if (dropDown.getVisibility() == View.GONE | dropDown.getVisibility() == View.INVISIBLE) {
					vis(VISIBLE,R.id.drop_down,true);
				} else {
					vis(GONE,R.id.drop_down,true);
				}
				break;
			case R.id.refresh_button:
				Animation anim = AnimationUtils.loadAnimation(context,R.anim.refresh);
				findViewById(R.id.refresh_button_img).startAnimation(anim);
				if (articleOpen) {
					populateArticle(currHeadline);
				} else {
					populateNews(true,true);
				}
				break;
			case R.id.close_button:
			case R.id.back_dark_underlay:
				closeArticle();
				break;
			case R.id.drop_down_contact_us:
				vis(GONE,R.id.drop_down,true);
				Intent intent1 = new Intent(BoarActivity.this,ContactActivity.class);
				startActivity(intent1);
				break;
			case R.id.drop_down_settings:
				vis(GONE,R.id.drop_down,true);
				Intent intent2 = new Intent(BoarActivity.this,Preferences.class);
				startActivity(intent2);
				break;
			case R.id.drop_down_share:
				vis(GONE,R.id.drop_down,true);
				try {
					Intent share = new Intent(Intent.ACTION_SEND);
					share.setType("text/plain");
					share.putExtra(Intent.EXTRA_TEXT,currHeadline.getPageUrl());
					startActivity(Intent.createChooser(share,"Share with Friends"));
				}
				catch (Exception e) {
					Toast.makeText(context,"An Error Occured.",Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.drop_down_favourite:
				ImageView favIcon = (ImageView) findViewById(R.id.drop_down_favourite_icon);
				if (favIcon.getTag().equals("true")) { //remove from fav
					if (currHeadline.setFavourite(false,context)) {
						favIcon.setImageDrawable(getResources().getDrawable(R.drawable.fav_false_black));
						favIcon.setTag("false");
					}
				} else { //addToFav
					if (currHeadline.setFavourite(true,context)) {
						favIcon.setImageDrawable(getResources().getDrawable(R.drawable.fav_true_black));
						Animation favourited = AnimationUtils.loadAnimation(context,R.anim.favourited);
						favIcon.startAnimation(favourited);
						favIcon.setTag("true");
					}
				}
				break;
			case R.id.drop_down_browser:
				vis(GONE,R.id.drop_down,true);
				String url = "http://theboar.org/";
				if (!articleOpen) {
					if (REQUEST == SEARCH) {
						url = "http://theboar.org/?s=" + (query == null ? "" : query);
					} else if (REQUEST == TAG) {
						url = "http://theboar.org/tag/" + tagSlug + "/";
					} else {
						if (currentCategory != Category.HOME)
							url = "http://theboar.org/"
									+ Category.getCategoryName(currentCategory,false,false) + "/";
					}
				} else {
					url = currHeadline.getPageUrl();
				}
				try {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
				}
				catch (Exception e) {
					Toast.makeText(getApplicationContext(),"An Error Occured",Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.main_toast_root://settag for flags
				populateNews(true,true);
				if (articleOpen) closeArticle();
				vis(GONE,R.id.main_toast_root,true);
				beenReloaded[currentCategory] = true;
				break;
			case R.id.story_comment_expander://settag for flags
				View commentSection = findViewById(R.id.story_comment_root);
				if (commentSection.getVisibility() == View.VISIBLE) {
					commentSection.setVisibility(View.GONE);
					vis(GONE,R.id.story_comment_divider);
				} else {
					commentSection.setVisibility(View.VISIBLE);
					vis(VISIBLE,R.id.story_comment_divider);
				}
				break;

			default:
				break;
			}
		}
	};

	private void setArticleColors(int category)
	{

		isFullScreenEnabled = CNS.getSharedPreferences(context).getBoolean("stick_heading",false);
		int actionBarHeight = (int) getResources().getDimension(R.dimen.actionBarHeight);
		if (!isFullScreenEnabled) {
			findViewById(R.id.story_headline_group_headline).setTag("sticky");
			findViewById(R.id.story_layout).setPadding(0,0,0,0);
			findViewById(R.id.storyScrollView).setPadding(0,actionBarHeight,0,0);
			findViewById(R.id.actionBar_shadow).setVisibility(View.GONE);
		} else {
			findViewById(R.id.story_layout).setPadding(0,actionBarHeight,0,0);
			findViewById(R.id.storyScrollView).setPadding(0,0,0,0);
			findViewById(R.id.story_headline_group_headline).setTag("");
			vis(VISIBLE,R.id.actionBar_shadow);
		}

		int catColor, main, secondary;

		LinearLayout headingGroup = (LinearLayout) findViewById(R.id.story_headline_group);
		View headingGroupHeading = findViewById(R.id.story_headline_group_headline);
//			FrameLayout imgFrame = (FrameLayout) findViewById(R.id.story_newImage_frame);
		TextView tvDate = (TextView) findViewById(R.id.story_date);
		TextView tv = (TextView) findViewById(R.id.story_headline);
		TextView tvAuthor = (TextView) findViewById(R.id.story_author);
		//-------------------------------------

		catColor = Category.getCategoryColourBar(category,getResources());

		main = getResources().getColor(R.color.white_90);
		secondary = getResources().getColor(R.color.white_70);

		//---------------------------

		headingGroup.setBackgroundColor(catColor);
		headingGroupHeading.setBackgroundColor(catColor);
//		tv.setTextColor(main);
//		tvAuthor.setTextColor(secondary);
//		tvDate.setTextColor(secondary);

	}

	private void populateArticle(Headline hl)
	{
		//----------------------------------Comment Test------------------------------------------

		final Map<String, Node> commentsTree = hl.getComments();
		new Handler().postDelayed(new Runnable() {
			public void run()
			{
				populateComments(commentsTree);
			}
		},1000);
		//----------------------------------------------------------------------------

//		storyScroll = (BoarMagicalScrollView) findViewById(R.id.storyScrollView);
		storyScroll.newImage();
		storyScroll.fullScroll(ScrollView.FOCUS_UP);
		HorizontalScrollView svTag = (HorizontalScrollView) findViewById(R.id.story_tab_scroll);
		svTag.fullScroll(HorizontalScrollView.FOCUS_LEFT);

		int maxWidth = setOrientationMargin(null,false) - (2 * CNS.getPXfromDP(10,context));

		ImageView iv = (ImageView) findViewById(R.id.story_newsImage);
		iv.setImageDrawable(null);

		String imageURL = hl.getImageUrl();
		if (imageURL != null) {
			iv.setVisibility(View.VISIBLE);
			int width = (int) (storyScroll.getWidth());
			ImageOptions options = new ImageOptions();
			options.targetWidth = width;
			options.fallback = R.drawable.fallback_img;
			aq.id(iv).progress(R.id.story_img_progress).image(imageURL,options);
//			storyScroll.ivHeight = hl.getImageDimensions()[1];
		} else {
			iv.setVisibility(View.GONE);
			currHeadlineImageSize = 0;
		}

		HashMap<Integer, String> categoriesList = hl.getCategoriesList();
		String catText = "";
		int i = 0;
		for (Entry<Integer, String> title : categoriesList.entrySet()) {
			catText += title.getValue();
			catText += i == categoriesList.size() - 1 ? "" : ", ";
			i++;
		}
		((TextView) findViewById(R.id.story_category_text)).setText(catText);
//				Category.getCategoryName(hl.getCategory(),true,false));
		String commentNum = Integer.toString(hl.getCommentsNum());
		((TextView) findViewById(R.id.story_comments_num)).setText(commentNum
				+ (commentNum.equals("1") ? " Comment" : " Comments"));

		TextView tv = (TextView) findViewById(R.id.story_headline);
		tv.setText(hl.getHeadline());

		TextView tvAuthor = (TextView) findViewById(R.id.story_author);
		tvAuthor.setText("By " + hl.getAuthor());

		TextView tvDate = (TextView) findViewById(R.id.story_date);
		String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm").format(hl.getDatePublished());
		tvDate.setText(date);
		//-------------------------------------------Tabs----------------------------------------
		LinearLayout tagsRoot = (LinearLayout) findViewById(R.id.story_tab_root);
		tagsRoot.removeAllViews();
		for (final String tag : hl.getTags()) {
			FrameLayout fmTemp = CNS.getTagBox(tag,context);
			fmTemp.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v)
				{
					Intent i = new Intent(context,BoarActivity.class);
					i.putExtra("forTag",true);
					i.putExtra("tag",tag);
					context.startActivity(i);

				}
			});
			tagsRoot.addView(fmTemp);
		}

		LinearLayout webViewLL = (LinearLayout) findViewById(R.id.story_ll_root);
		webViewLL.removeAllViews();
//		vis(VISIBLE,R.id.story_ll_progress);
		vis(VISIBLE,R.id.story_progress);

		WebView webview = (WebView) new WebView(context);
		webViewLL.addView(webview);
		webview.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		webview.setBackgroundColor(context.getResources().getColor(R.color.Transparent));
		webview.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
//				return super.shouldOverrideUrlLoading(view,url);
				Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				view.getContext().startActivity(i);
				return true;
			}

			public void onPageFinished(WebView view, String url)
			{
				vis(GONE,R.id.story_progress);
//				LinearLayout webViewLL = (LinearLayout) findViewById(R.id.story_ll_root);
//				webViewLL.addView(view);
//				view.setVisibility(View.VISIBLE);
//				vis(GONE,R.id.story_ll_progress);
//				view.startAnimation(CNS.Animate(view,CNS.FADE,0,1,1000,true));
			}

		});
		webview.getSettings().setAllowFileAccess(true);

		class MyRunnable implements Runnable
		{

			private WebView webview;
			private String html;
			private int maxWidth;

			public MyRunnable(WebView webView, int maxWidth, String html) {
				webview = webView;
				this.maxWidth = maxWidth;
				this.html = html;
			}

			public void run()
			{
				final String data = CNS.generateBetterHTML(maxWidth,html,context);
				runOnUiThread(new Runnable() {
					public void run()
					{
						webview.loadDataWithBaseURL(null,data,"text/html","UTF-8",null);
					}
				});
				return;
			}
		};

		MyRunnable runn = new MyRunnable(webview,maxWidth,hl.getArticleHTML());
		new Thread(runn).start();
	}

	private void populateComments(Map<String, Node> tree)
	{
		LinearLayout commRoot = (LinearLayout) findViewById(R.id.story_comment_root);
		commRoot.setVisibility(View.GONE);
		commRoot.removeAllViews();

		for (Map.Entry<String, Node> node : tree.entrySet()) {
			Node value = node.getValue();
			if (value.parent == null) {
				addCommentChildren(tree,commRoot,value,0);
			}
		}
		if (tree.size() == 0) {
			TextView txt = new TextView(context);
			txt.setText("No Comments");
			txt.setTextColor(getResources().getColor(R.color.black_45));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1);
			params.gravity = Gravity.CENTER;
			txt.setLayoutParams(params);
			commRoot.addView(txt);
		}

	}

	public void addCommentChildren(Map<String, Node> tree, LinearLayout root, Node childNode, int depth)
	{
		root.addView(CNS.getCommentView(childNode.name,
				childNode.comment,
				childNode.parent,
				depth,context));

		depth++;
		for (String childID : childNode.children) {
			addCommentChildren(tree,root,tree.get(childID),depth);
		}

	}

	private void startArticle(final Headline hl)
	{
		try {
			articleOpen = true;
			currHeadline = hl;

			setActionBarForCategory(hl.getCategory(),false);
			setArticleColors(hl.getCategory());
			populateArticle(hl);

			View articleView = vis(VISIBLE,R.id.news_story_frame);
			Animation anim = AnimationUtils.loadAnimation(context,R.anim.article_start);
			articleView.startAnimation(anim);

			ImageView favIcon = (ImageView) findViewById(R.id.drop_down_favourite_icon);
			if (hl.isFavourite(context)) {
				favIcon.setImageDrawable(getResources().getDrawable(R.drawable.fav_true_black));
				favIcon.setTag("true");
			} else {
				favIcon.setImageDrawable(getResources().getDrawable(R.drawable.fav_false_black));
				favIcon.setTag("false");
			}

			if (menu != null) menu.setSlidingEnabled(false);
		}
		catch (Exception e) {}
	}

	private void closeArticle()
	{
		View actionBar = findViewById(R.id.action_bar_main);
		if (actionBar.getVisibility() != View.VISIBLE) {
			showActionBar();
		}
		View dropDown = findViewById(R.id.drop_down);
		if (dropDown.getVisibility() == View.VISIBLE) {
			vis(GONE,R.id.drop_down,true);
		}

		View articleView = findViewById(R.id.news_story_frame);
		Animation anim = AnimationUtils.loadAnimation(context,R.anim.article_close);
		articleView.startAnimation(anim);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation)
			{}

			@Override
			public void onAnimationRepeat(Animation animation)
			{}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				findViewById(R.id.news_story_frame).setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.story_ll_root)).removeAllViews();
				((ImageView) findViewById(R.id.story_newsImage)).setImageDrawable(null);
				findViewById(R.id.drop_down_favourite).setVisibility(FrameLayout.GONE);
				findViewById(R.id.drop_down_share).setVisibility(FrameLayout.GONE);

				((LinearLayout) findViewById(R.id.story_comment_root)).removeAllViews();
				vis(GONE,R.id.story_comment_divider);
			}
		});

		if (menu != null) menu.setSlidingEnabled(true);
		this.articleOpen = false;
		setActionBarForCategory((REQUEST == SEARCH || REQUEST == TAG) ? -1 : currentCategory,true);
	}

	//----------------------------------Fetching Headlines------------------------------

	public void startCategory(int position, View view)
	{
		currentCategory = position;
		if (currPos == null || currPos != view) {
			setActionBarForCategory(position,true);
			findViewById(R.id.search_query).setVisibility(View.GONE);
			populateNews(false,true);

			if (currPos != null) {
				currPos.setBackgroundColor(getResources().getColor(R.color.Transparent));
			}
			if (view != null) {
				if (position != Category.HOME) {
					view.setBackgroundColor(getResources().getColor(R.color.black_05));
				}
				currPos = view;
			}
		}
	}

	private void populateNews(boolean useInternet, boolean loadingNew)
	{
		if (hat != null && hat.getStatus().equals(AsyncTask.Status.RUNNING)) {
			hat.cancel(true);
		}
		vis(GONE,R.id.main_toast_root,true);

		populating = true;
		newsStore = new NewsStore(context);

		l1 = (LinearLayout) findViewById(R.id.tablet_lld1);
		l2 = (LinearLayout) findViewById(R.id.tablet_lld2);
		l3 = (LinearLayout) findViewById(R.id.tablet_lld3);

		if (loadingNew) {
			ScrollView sv = (ScrollView) findViewById(R.id.scrollView_main);
//			sv.fullScroll(ScrollView.FOCUS_UP);

			loadingLayout(false,true,null);
			pageNum = 1;
			l1.removeAllViews();
			l2.removeAllViews();
			l3.removeAllViews();
			headlinesParsedSoFar = 0;
		}
		selectColumns();

		hat = new HeadlineAsyncTask(loadingNew,useInternet);
		hat.execute("default");

//		if (currentCategory == Category.HOME) {
//			HeadlineAsyncTask hatFeatured = new HeadlineAsyncTask(loadNew,useInternet);
//			hatFeatured.execute("featured");
//		}

	}

	private class HeadlineAsyncTask extends AsyncTask<String, Object, Void> implements
			IHeadlineListener
	{

		int count = 0;
		private String totalAvailablePages = "0";
		public boolean loadingNew;
		private String totalCount = "0";
		private boolean useInternet;
		private Map<Integer, Integer> catCountMap;
		private boolean forFeatured;
		boolean isFromCache;
		protected int wait = 0;

		public HeadlineAsyncTask(boolean loadingNew, boolean useInternet) {
			this.loadingNew = loadingNew;
			this.useInternet = useInternet;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			findViewById(R.id.tablet_l_root).setVisibility(View.VISIBLE);
			if (loadingNew) {
				if (REQUEST == SEARCH || REQUEST == TAG || currentCategory == Category.FAVOURITES) {
					loadingLayout(true,false,null);
				} else {
					loadingLayout(true,true,"Fetching news");
				}
			}

		}

		@Override
		protected Void doInBackground(String... params)
		{
			forFeatured = params[0].equals("featured");
			if (forFeatured) {
				//TODO if in the future we want featured news carousel.
				//newsStore.headlinesFromFeaturedNews(1,7,this);
			}
			else if (currentCategory == Category.FAVOURITES) {
				catCountMap = new HashMap<Integer, Integer>();
				newsStore.headlinesFromFavourites(this);
			}
			else if (REQUEST == SEARCH) {
				newsStore.headlinesFromSearch(query,pageNum,numCount,this);
			}
			else if (REQUEST == TAG) {
				newsStore.headlinesFromTag(tagSlug,pageNum,numCount,this);
			}
			else {
				//FIXME home always returns tru becuase miss patina is always on the top. has to be fixed from boar website. 
				newsStore.headlinesFromCategory(currentCategory,pageNum,numCount,this,useInternet);
			}
			return null;
		}

		@Override
		public void onHeadlineParsed(IHeadline hl, String[] msgs)
		{
			publishProgress(hl,msgs);
		}

		@Override
		protected void onProgressUpdate(Object... values)
		{
			super.onProgressUpdate(values);
			final Headline hl = (Headline) values[0];

			if (forFeatured) {} else {

				//hack to stop showing THAT post from february

				if (!missPatina(hl)) {
					addHeadlineToView(hl,wait);
					wait += 100;
				}

				String[] msgs = (String[]) values[1];

				isFromCache = msgs[0].equals("fromCache") ? true : false;
				totalAvailablePages = msgs[1];
				totalCount = msgs[2];
				count++;

				if (currentCategory == Category.FAVOURITES) {
					collectFavouriteInfo(hl.getCategory());
				}
			}
		}

		private void collectFavouriteInfo(int category)
		{
			if (catCountMap == null) {
				catCountMap = new HashMap<Integer, Integer>();
			}
			if (!catCountMap.containsKey(category)) {
				catCountMap.put(category,0);
			}
			catCountMap.put(category,catCountMap.get(category) + 1);
		}

		private boolean missPatina(Headline hl)
		{
			if (hl.getUniqueId().equals("37314") && count == 0 && pageNum == 1
					&& currentCategory == Category.HOME) { return true; }
			return false;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);
			populating = false;
			if (loadingNew) {
				ScrollView sv = (ScrollView) findViewById(R.id.scrollView_main);
				sv.fullScroll(ScrollView.FOCUS_UP);
			}

			loadingLayout(false,true,null);

			if (!forFeatured) setMessageText();

			if (currentCategory == Category.FAVOURITES) {
				addFavOptions();
			} else if (forFeatured) {
				//TODO add to layouts if we want to show featured!
			} else if (REQUEST != SEARCH && REQUEST != TAG && isFromCache) {
				checkAndNotifyForNew();
			}
		}

		private void setMessageText()
		{
			TextView txt = (TextView) vis(GONE,R.id.search_query);

			if (count == 0 || Integer.parseInt(totalAvailablePages) < pageNum) {
				//Search
				if (REQUEST == SEARCH) {
					txt.setVisibility(View.VISIBLE);
					if (pageNum == 1) {
						txt.setText("'" + query + "' returned no results");
					}
				}
				//Favourites
				else if (count == 0 && currentCategory == Category.FAVOURITES) {
					txt.setVisibility(View.VISIBLE);
					txt.setText("You have not added any articles to your favourites.");
				}
				//Tag
				else if (count == 0 && REQUEST == TAG) {
					txt.setVisibility(View.VISIBLE);
					txt.setText("There are no articles available");
				}
				//categories
				else if (count == 0) {
					txt.setVisibility(View.VISIBLE);
					txt.setText("There are no articles available");
				}
				pageNum = 0;
			} else {
				pageNum++;
				//Search
				if (REQUEST == SEARCH) {
					txt.setVisibility(View.VISIBLE);
					txt.setText(totalCount + " results returned for '" + query + "'");
				}
				//Tag
				else if (REQUEST == TAG) {
					txt.setVisibility(View.VISIBLE);
					txt.setText("Articles with tag: " + tagSlug);
				}
			}
		}

		private void addFavOptions()
		{
			LinearLayout llFavOpt = (LinearLayout) vis(VISIBLE,R.id.favourite_detail_root);
			llFavOpt.removeAllViews();
			catCountMap = CNS.sortByValue(catCountMap);
			for (Map.Entry<Integer, Integer> entry : catCountMap.entrySet()) {
				llFavOpt.addView(CNS.getFavsCountBox(entry.getKey(),entry.getValue(),context));
			}

		}

		private void checkAndNotifyForNew()
		{

			if (!useInternet && beenReloaded[currentCategory] == false
					&& CNS.isNetworkConnected(context)) {
				//if current category hasnt been checked already
				vis(VISIBLE,R.id.page_progress_forNew,true);
				Thread t = new Thread() {
					public void run()
					{
						final int categoryWas = currentCategory;
						final boolean newAvailable = newsStore.checkIfNewHeadlines(currentCategory);
						if (categoryWas == currentCategory) {
							runOnUiThread(new Runnable() {
								public void run()
								{
									vis(GONE,R.id.page_progress_forNew,true);
									if (newAvailable) {
										vis(VISIBLE,R.id.main_toast_root,true);
//										Log.d(CNS.LOGPRINT,"New Posts available!");
									} else {
										beenReloaded[currentCategory] = true;
//										Log.d(CNS.LOGPRINT,"No new Posts available. ");
									}
								}
							});
						}
					}
				};
				t.start();
			}
		}

		private void addHeadlineToView(IHeadline hl, int waitDuration)
		{
			try {
				ImageView iv = null;
				FrameLayout categoryBox;
				TextView authorName = null, newsDate = null, newsName = null;

				//---------------------------------------------------------
				final View newsItems = getLayoutInflater().inflate(R.layout.content_fragment,null,false);
				//---------------------------------News Type Colour---------------------------
				categoryBox = (FrameLayout) newsItems.findViewById(R.id.content_typecolor);
				categoryBox.setVisibility(View.VISIBLE);
				int catColor = Category.getCategoryColourText(hl.getCategory(),getResources());
				categoryBox.setBackgroundColor(catColor);
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					CNS.setAlpha(categoryBox,0.9f);
				}

				TextView categoryName = (TextView) newsItems.findViewById(R.id.category_name);
				categoryName.setText(Category.getCategoryName(hl.getCategory(),false,true).toUpperCase());
				//------------------------------------ IMAGE--------------------------------
				iv = (ImageView) newsItems.findViewById(R.id.content_newsImage);

				String imageURL = hl.getImageUrl();
				if (imageURL != null) {
					iv.setVisibility(View.VISIBLE);
					ImageOptions options = new ImageOptions();
					int width = (int) (l1.getWidth() -
							(2 * getResources().getDimension(R.dimen.tabMar)));
					options.targetWidth = width;
					options.fallback = R.drawable.fallback_img;
//				newsItems.findViewById(R.id.content_newsImage_progress).setVisibility(View.VISIBLE);

					int[] ivWH = hl.getImageDimensions();
					iv.getLayoutParams().height = getIdealHeight(ivWH[0],ivWH[1]);
					View progress = newsItems.findViewById(R.id.content_newsImage_progress);
					aq.id(iv).progress(progress).image(imageURL,options);
				} else {
					iv.setVisibility(View.GONE);

					FrameLayout.LayoutParams llp = (FrameLayout.LayoutParams) categoryBox.getLayoutParams();
					llp.topMargin = 0;
					iv.getLayoutParams().height = 0;
					newsItems.findViewById(R.id.content_picture).getLayoutParams().height = LayoutParams.WRAP_CONTENT;

					(newsItems.findViewById(R.id.topicname_divider)).setVisibility(View.INVISIBLE);

				}

				//--------------------------------News Title||-----------------------------
				newsName = (TextView) newsItems.findViewById(R.id.topicname);
				CharSequence headlineText = Html.fromHtml(hl.getHeadline());
				newsName.setText(headlineText);
				newsName.setTypeface(Typeface.createFromAsset(getAssets(),"Roboto-Regular.ttf"));
				newsName.setTextSize(17);
				//-----------------------------------------News Author------------------------------------
				authorName = (TextView) newsItems.findViewById(R.id.author_name);
				authorName.setText(hl.getAuthor());
				//-------------------------------------------Date----------------------------------------
				newsDate = (TextView) newsItems.findViewById(R.id.content_date);
//			String dateTimeString = DateFormat.getDateInstance().format(hl.getDatePublished());
				String dateTimeString = CNS.timeElapsed(hl.getDatePublished());
				newsDate.setText(dateTimeString);
				//-------------------------------Set On touch/Click-----------------------------------------
				class ArticleClickListener implements OnClickListener
				{
					private Headline head;

					public ArticleClickListener(IHeadline hl) {
						this.head = (Headline) hl;
					}

					@Override
					public void onClick(View v)
					{
//					populateArticle(head);
						startArticle(head);

					}
				}
				newsItems.setOnClickListener(new ArticleClickListener(hl));
				CNS.onTouchHighlight(context,newsItems,((FrameLayout) newsItems).getChildAt(0));
				//-------------------------------Finally Add View (Old Tablet Code)-----------------------------------------

				newsItems.setVisibility(View.INVISIBLE);
				int nextLayoutAdd = getNextLayoutAdd(headlinesParsedSoFar);
				switch (nextLayoutAdd)
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

				Animation anim = AnimationUtils.loadAnimation(context,R.anim.article_start);
				anim.setStartOffset(waitDuration);
				newsItems.startAnimation(anim);

				newsItems.setVisibility(View.VISIBLE);

			}
			catch (Exception e) {
				Log.e(CNS.LOGPRINT,"Error while populating article to view."
						+ e.toString() + e.getMessage());
			}
			headlinesParsedSoFar++;

		}
		private int getIdealHeight(int width, int height)
		{

			if (width > 500 && (width / height) < 1.2) return CNS.getPXfromDP(300,context);
			else if (width > 500 && (width / height) < 1.6)
				return CNS.getPXfromDP(200,context);

			return CNS.getPXfromDP(150,context);
		}
	}

	private void loadingLayout(boolean show, boolean showText, String text)
	{
		TextView txt = (TextView) findViewById(R.id.loading_text);
		txt.setVisibility(showText ? View.VISIBLE : View.GONE);
		txt.setText(text);

		View lay = findViewById(R.id.loading_layout_color);
		CNS.setAlpha(lay,0.7f);
		lay.setBackgroundColor(Category.getCategoryColourText
				(currentCategory,getResources()));
		vis(show ? VISIBLE : GONE,R.id.loading_layout,true);
//		View progRot = findViewById(R.id.progress_bottom);
		/*if (show) {
			Animation rot = AnimationUtils.loadAnimation(context,R.anim.refresh);
			rot.setRepeatCount(Animation.INFINITE);
			progRot.startAnimation(rot);
		} else {
			if (progRot.getAnimation() != null)
				progRot.getAnimation().cancel();
		}*/

	}

	//----------------------------------Others------------------------------	

	private void setImage(View menu, int id)
	{
		((ImageView) menu).setImageDrawable(getResources().getDrawable(id));
	}

	public View vis(String visibility, int id)
	{
		int vis = View.VISIBLE;
		if (visibility == INVISIBLE) vis = View.INVISIBLE;
		if (visibility == GONE) vis = View.GONE;

		View view = findViewById(id);
		view.setVisibility(vis);
		return view;
	}

	public void vis(String visibility, int id, boolean fadeAnim)
	{
		int vis = View.VISIBLE;
		if (visibility == GONE) vis = View.GONE;
		if (visibility == INVISIBLE) vis = View.INVISIBLE;

		View view = findViewById(id);
		if (!fadeAnim) {
			view.setVisibility(vis);
		} else {
			if (view.getVisibility() != vis) {
				view.startAnimation(CNS.Animate(view,CNS.FADE,visibility == VISIBLE ? 0 : 1,visibility == VISIBLE
						? 1 : 0,200,true));
			}
		}
	}

//	----------------------------------Inherited------------------------------	

	@Override
	public void onBottomReached()
	{
		if (populating == false) {
			if (pageNum > 0) {
				if (CNS.isNetworkConnected(this)) {
					populateNews(true,false);
					loadingLayout(true,false,null);
				} else {
					populating = true;
					new Handler().postDelayed(new Runnable() {
						public void run()
						{
							populating = false;
						}
					},5000);

					Toast.makeText(context,"Please check your internet connection",Toast.LENGTH_LONG).show();
				}
			}
		}

	}

}
