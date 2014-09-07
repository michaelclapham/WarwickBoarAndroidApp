package org.theboar.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class WarwickBoarActivity extends Activity implements BottomReachedListener
{

	public static Activity activity;
	// Layout for each column of news items
	private LinearLayout l1, l2, l3;

	// Object used to access cache of news articles, download news articles, and associated images
	private NewsStore newsStore;

	// True if populating news and not yet finished
	private boolean populating = false;

	private int currentCategory;
	private SlidingMenu menu;
	protected boolean articleOpen = false;
	// Increases everytime we parse and add a headline to the view
	private int headlinesParsedSoFar = 0;

	protected View currPos;
	public static final String VISIBLE = "vis", GONE = "gone", INVISIBLE = "inv";
	public String currentURL;
	private boolean forSearch;

	protected String query;
	public int pageNum;
	int lastN = 10;
	public BottomReachedListener btmListener;
	private AQuery aq;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_tablet);
		activity = this;
		btmListener = this;
		pageNum = 1;
		aq = new AQuery(this);
		AQUtility.setCacheDir(getCacheDir());

		Bundle bundle = getIntent().getExtras();
		forSearch = false;
		if (bundle != null && bundle.getBoolean("forSearch",false)) {
			forSearch = true;
		} else {
			setSlidingMenu();
			String catName = CNS.getSharedPreferences(this).getString("default_category","Home");
			currentCategory = Category.getCategoryIDFromString(catName);
			startCategory(Category.categoryToMenuPosition(currentCategory),null);
		}
		setOnClickListeners();
	}

	@Override
	public void onBackPressed()
	{
		if (articleOpen) {
			closeArticle();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		Log.d("PRINT","CONFIFURATION CHANGED!");
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
		},l1List.size() * 500);
	}

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
		View scroll = findViewById(R.id.storyScrollView);
		FrameLayout.LayoutParams llp = (FrameLayout.LayoutParams) scroll.getLayoutParams();
		int marginHor = (int) getResources().getDimension(R.dimen.articleMargHor);
		llp.leftMargin = marginHor;
		llp.rightMargin = marginHor;
		scroll.setLayoutParams(llp);
	}

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

	public void startCategory(int position, View view)
	{
		if (currPos == null || currPos != view) {
			int heading = Category.menuPositionToCategory(position);
			String categoryName = Category.getCategoryName(heading,true);
			int colorBar = Category.menuPositionToTopColour(position,getResources());
			int colorText = Category.getCategoryColour(heading,getResources());

			setHeadingAndColor(categoryName,colorBar,colorText);

			currentCategory = Category.menuPositionToCategory(position);
			populateNews(true,true);

			if (currPos != null) {
				currPos.setBackgroundColor(getResources().getColor(R.color.Transparent));
			}
			if (view != null) {
				if (position == 0) {
//					LinearLayout fml = (LinearLayout) view.findViewById(R.id.menu_item_home_Text);
//					fml.setBackgroundColor(getResources().getColor(R.color.black_05));
				} else {
					view.setBackgroundColor(getResources().getColor(R.color.black_05));
				}
				currPos = view;
			}
		}
	}

	public void setHeadingAndColor(String name, int colorBar, int colorText)
	{
		findViewById(R.id.top_bar_layout).setBackgroundColor(colorBar);

		TextView heading = (TextView) findViewById(R.id.page_heading);
		heading.setText(name);
		heading.setTextColor(colorText);
	}

	private void setOnClickListeners()
	{

		findViewById(R.id.close_button).setOnClickListener(clickEvent);
		findViewById(R.id.back_dark_underlay).setOnClickListener(clickEvent);
		findViewById(R.id.refresh_button).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_contact_us).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_settings).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_browser).setOnClickListener(clickEvent);
		findViewById(R.id.more_button).setOnClickListener(clickEvent);

		findViewById(R.id.dummy_actionBar_underlay).setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event)
			{
				if (findViewById(R.id.drop_down).getVisibility() == View.VISIBLE) {
					vis(R.id.drop_down,GONE,true);
				}
				return false;
			}
		});

		ScrollViewExt smartScroll = (ScrollViewExt) findViewById(R.id.scrollView_main);
		smartScroll.setBottomListener(btmListener);

		if (forSearch) {
			findViewById(R.id.back_button).setVisibility(View.VISIBLE);
			findViewById(R.id.back_button).setOnClickListener(clickEvent);
			TextView title = (TextView) findViewById(R.id.actionbar_title);
			title.setText("Search");

			findViewById(R.id.menu_button).setVisibility(View.GONE);
			findViewById(R.id.actionbar_logo).setVisibility(View.INVISIBLE);
			findViewById(R.id.page_heading).setVisibility(View.GONE);

			findViewById(R.id.drop_down_contact_us).setVisibility(View.GONE);
			findViewById(R.id.drop_down_settings).setVisibility(View.GONE);

			findViewById(R.id.search_query).setVisibility(View.VISIBLE);
			final EditText textarea = (EditText) findViewById(R.id.search_area);
			textarea.setVisibility(View.VISIBLE);
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

		} else {
			TextView heading = (TextView) findViewById(R.id.page_heading);
			heading.setTypeface(Typeface.createFromAsset(getAssets(),"Lato-Regular.ttf"));

			findViewById(R.id.menu_button).setOnClickListener(clickEvent);

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
				menu.toggle();
				break;
			case R.id.more_button:
				View dropDown = findViewById(R.id.drop_down);
				if (dropDown.getVisibility() == View.GONE | dropDown.getVisibility() == View.INVISIBLE) {
					vis(R.id.drop_down,VISIBLE,true);
//					dropDown.setVisibility(View.VISIBLE);
				} else {
					vis(R.id.drop_down,GONE,true);
//					dropDown.setVisibility(View.GONE);
				}
				break;
			case R.id.refresh_button:
				vis(R.id.drop_down,GONE,true);
//				findViewById(R.id.drop_down).setVisibility(View.GONE);
				if (articleOpen) closeArticle();
				populateNews(true,true);
				break;
			case R.id.close_button:
			case R.id.back_dark_underlay:
				closeArticle();
				break;
			case R.id.drop_down_contact_us:
				vis(R.id.drop_down,GONE,true);
				Intent intent1 = new Intent(WarwickBoarActivity.this,ContactActivity.class);
				startActivity(intent1);
				break;
			case R.id.drop_down_settings:
				vis(R.id.drop_down,GONE,true);
				Intent intent2 = new Intent(WarwickBoarActivity.this,Preferences.class);
				startActivity(intent2);
				break;
			case R.id.drop_down_browser:
				vis(R.id.drop_down,GONE,true);
				String url = currentURL;
				if (!articleOpen) {
					if (forSearch) {
						url = "http://theboar.org/page/" + pageNum + "/?s=" + query;
					} else {
						url = "http://theboar.org/" + Category.getCategoryName(currentCategory,false) + "/";
					}
				}
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				break;

			default:
				break;
			}
		}
	};

	private void populateNews(boolean useInternet, boolean loadNew)
	{

		if (!populating) {
			populating = true;
			newsStore = new NewsStore(getApplicationContext());

			l1 = (LinearLayout) findViewById(R.id.tablet_lld1);
			l2 = (LinearLayout) findViewById(R.id.tablet_lld2);
			l3 = (LinearLayout) findViewById(R.id.tablet_lld3);

			if (loadNew) {
				pageNum = 1;
				l1.removeAllViews();
				l2.removeAllViews();
				l3.removeAllViews();
				headlinesParsedSoFar = 0;
			}

			selectColumns();

			HeadlineAsyncTask hat = new HeadlineAsyncTask(loadNew);
			hat.execute("default");
		}

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

	public void vis(int id, String visibility, boolean fadeAnim)
	{
		int vis = View.VISIBLE;
		if (visibility == GONE) vis = View.GONE;
		if (visibility == INVISIBLE) vis = View.INVISIBLE;

		View view = findViewById(id);
		if (!fadeAnim) {
			view.setVisibility(vis);
		} else {
			view.startAnimation(CNS.Animate(view,CNS.FADE,visibility == VISIBLE ? 0 : 1,visibility == VISIBLE ? 1 : 0,200));
		}
	}

	private class HeadlineAsyncTask extends AsyncTask<String, Object, Void> implements IHeadlineListener
	{

		int count = 0;
		private String totalAvailablePages = "0";
		public boolean loadingNew;
		private String totalCount = "0";

		public HeadlineAsyncTask(boolean loadingNew) {
			this.loadingNew = loadingNew;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			if (loadingNew) {
				LinearLayout loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
				loadingLayout.setVisibility(LinearLayout.VISIBLE);
				TextView loadingTV = (TextView) findViewById(R.id.loading_text);
				loadingTV.setText("Loading ");
			}
		}

		@Override
		protected Void doInBackground(String... params)
		{
			if (forSearch) {
				newsStore.getHeadlinesFromSearch(query,pageNum,lastN,this);
			}
			else {
				newsStore.getHeadlinesFromCategory(currentCategory,pageNum,lastN,this);
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
			Headline hl = (Headline) values[0];
			addHeadlineToView(hl);

			String[] msgs = (String[]) values[1];
			if (loadingNew) {
				TextView loadingTV = (TextView) findViewById(R.id.loading_text);
				loadingTV.setText(msgs[0]);
			}
			totalAvailablePages = msgs[1];
			totalCount = msgs[2];
			Log.d("PRINT","TOTAL COUNT ASYNC: " + msgs[2]);
			count++;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);
			LinearLayout loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
			loadingLayout.setVisibility(LinearLayout.GONE);
			populating = false;
			TextView txt = (TextView) findViewById(R.id.search_query);

			findViewById(R.id.progress_bottom).setVisibility(View.GONE);
			Log.d("PRINT","count current: " + count);
			Log.d("PRINT","page Num: " + pageNum);
			if (count == 0 || Integer.parseInt(totalAvailablePages) <= pageNum) {
				pageNum = 0;
				txt.setText("'" + query + "' returned no results");
			} else {
				pageNum++;
			}
			txt.setText(totalCount + " results returned for '" + query + "'");
		}
	}

	private int getNextLayoutAdd(int i)
	{
		int L = 1;
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
		Log.d("PRINT","L: " + L);
		return L;
	}

	private class ArticleClickListener implements OnClickListener
	{
		private Headline hl;

		public ArticleClickListener(IHeadline hl) {
			this.hl = (Headline) hl;
		}

		@Override
		public void onClick(View v)
		{

			currentURL = hl.getPageUrl();
//			ScrollView sv = (ScrollView) findViewById(R.id.storyScrollView);
//			sv.fullScroll(ScrollView.FOCUS_UP);

			ImageView iv = (ImageView) findViewById(R.id.story_newsImage);
			String imageURL = hl.getImageUrl();
			if (imageURL != null) {
				iv.setVisibility(View.VISIBLE);
				aq.id(iv).image(imageURL);
			} else {
				iv.setVisibility(View.GONE);
			}
			TextView tv = (TextView) findViewById(R.id.story_headline);
			tv.setText(hl.getHeadline());

			TextView tvAuthor = (TextView) findViewById(R.id.story_author);
			tvAuthor.setText("By " + hl.getAuthor());

			TextView tvDate = (TextView) findViewById(R.id.story_date);
			Date datePublished = hl.getDatePublished();
			String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm").format(datePublished);
			tvDate.setText(date);

			LinearLayout webViewLL = (LinearLayout) findViewById(R.id.story_ll_root);
			webViewLL.removeAllViews();

			WebView webview = (WebView) new WebView(getApplicationContext());
			webview.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
			webview.setWebViewClient(new WebViewClient() {

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url)
				{
//					return super.shouldOverrideUrlLoading(view,url);
					Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					view.getContext().startActivity(i);
					return true;
				}

				@Override
				public void onPageFinished(WebView view, String url)
				{
//					startArticle();
				}

			});
//			webview.setWebChromeClient(new WebChromeClient());
			webview.getSettings().setPluginState(PluginState.ON);
//			webview.getSettings().setJavaScriptEnabled(true);
			webview.getSettings().setAllowFileAccess(true);

			startArticle();

			int width = webViewLL.getMeasuredWidth() / 2 - CNS.getDPfromPX(10,getApplicationContext());

			String s = "<html><head><meta name=\"viewport\"\"content=\"width=" + width + "\" /></head>";
			s += "<body>";
			String sEnd = "</body></html>";
			String cleanUp = CNS.changeHTMLattr(hl.getArticleHTML(),"width",Integer.toString(width));
			cleanUp = CNS.changeHTMLattr(cleanUp,"height","auto");
			cleanUp = CNS.correctYouTube(cleanUp);
			webview.loadDataWithBaseURL(null,s + cleanUp + sEnd,"text/html","UTF-8",null);

			webViewLL.addView(webview);

		}

	}

	private void startArticle()
	{
		View articleView = findViewById(R.id.news_story_frame);
		Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.article_start);
		articleView.setVisibility(View.VISIBLE);
		articleView.startAnimation(anim);

		if (forSearch) {
			findViewById(R.id.back_button).setVisibility(View.GONE);
		} else {
			findViewById(R.id.menu_button).setVisibility(FrameLayout.GONE);
		}
		findViewById(R.id.close_button).setVisibility(FrameLayout.VISIBLE);

		if (menu != null) menu.setSlidingEnabled(false);
		articleOpen = true;

	}
	private void closeArticle()
	{
		View articleView = findViewById(R.id.news_story_frame);
		Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.article_close);
		articleView.startAnimation(anim);
		articleView.setVisibility(View.GONE);
//		articleView.startAnimation(CNS.Animate(articleView,CNS.FADE,1,0,400));

		if (forSearch) {
			findViewById(R.id.back_button).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.menu_button).setVisibility(FrameLayout.VISIBLE);
		}
		findViewById(R.id.close_button).setVisibility(FrameLayout.GONE);

		if (menu != null) menu.setSlidingEnabled(true);
		articleOpen = false;
	}

	private void addHeadlineToView(IHeadline hl)
	{
		View newsItems = null;
		ImageView iv = null;
		LinearLayout categoryBox;
		TextView authorName = null, newsDate = null, newsName = null;

		//---------------------------------------------------------
		int imageHeight = CNS.getPXfromDP(150,this);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(CNS.getFillParent(),imageHeight);
		newsItems = getLayoutInflater().inflate(R.layout.content_fragment,null,false);

		//---------------------------------News Type Colour---------------------------
		categoryBox = (LinearLayout) newsItems.findViewById(R.id.content_typecolor);
		categoryBox.setBackgroundColor(Category.getCategoryColour(hl.getCategory(),getResources()));

		TextView categoryName = (TextView) newsItems.findViewById(R.id.category_name);
		categoryName.setText(Category.getCategoryNameShort(hl.getCategory()).toUpperCase());

		//------------------------------------ IMAGE--------------------------------
		iv = (ImageView) newsItems.findViewById(R.id.content_newsImage);

		String imageURL = hl.getImageUrl();
		if (imageURL != null) {
			iv.setVisibility(View.VISIBLE);
			aq.id(iv).image(imageURL);
			iv.setLayoutParams(params);
		} else {
			iv.setVisibility(View.GONE);
			FrameLayout.LayoutParams llp = (FrameLayout.LayoutParams) categoryBox.getLayoutParams();
			llp.topMargin = 0;
		}

		//--------------------------------News Title||-----------------------------
		newsName = (TextView) newsItems.findViewById(R.id.topicname);
		CharSequence headlineText = Html.fromHtml(hl.getHeadline());
		newsName.setText(headlineText);
		//-----------------------------------------News Author------------------------------------
		authorName = (TextView) newsItems.findViewById(R.id.author_name);
		authorName.setText(hl.getAuthor());
		//-------------------------------------------Date----------------------------------------
		newsDate = (TextView) newsItems.findViewById(R.id.content_date);
//		String dateTimeString = DateFormat.getDateInstance().format(hl.getDatePublished());
		String dateTimeString = CNS.timeElapsed(hl.getDatePublished());
		newsDate.setText(dateTimeString);
		//-------------------------------Set On touch/Click-----------------------------------------
		newsItems.setOnClickListener(new ArticleClickListener(hl));
		CNS.onTouchHighlight(getApplicationContext(),newsItems,((FrameLayout) newsItems).getChildAt(0));
		//-------------------------------Finally Add View (Old Tablet Code)-----------------------------------------
		switch (getNextLayoutAdd(headlinesParsedSoFar))
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
		headlinesParsedSoFar++;

	}

	@Override
	public void onBottomReached()
	{
		if (populating == false) {
			if (pageNum > 0) {
				if (CNS.isNetworkConnected(this)) {
					Log.d("PRINT","Bottom Reached Loading More!");
					populateNews(true,false);
					findViewById(R.id.progress_bottom).setVisibility(View.VISIBLE);
				} else {
					populating = true;
					new Handler().postDelayed(new Runnable() {
						public void run()
						{
							populating = false;
						}
					},5000);

					Toast.makeText(getApplicationContext(),"Please check your internet connection",Toast.LENGTH_LONG).show();
				}
			}
		}

	}

}
