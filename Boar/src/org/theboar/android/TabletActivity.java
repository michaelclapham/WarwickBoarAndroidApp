package org.theboar.android;

import java.text.DateFormat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class TabletActivity extends Activity
{
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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_tablet);
		setSlidingMenu();
		setAndroidVersionChanges();

		String catName = CNS.getSharedPreferences(this).getString("default_category","Home");
//		catName = "Home";
		currentCategory = Category.getCategoryIDFromString(catName);

		setOnClickListeners();
		startCategory(Category.categoryToMenuPosition(currentCategory),null);
	}
	private void setAndroidVersionChanges()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			int actionBarSize = CNS.getActionBarHeight(this);

//			findViewById(R.id.main_container).setPadding(0,actionBarSize,0,0);
//			findViewById(R.id.top_bar_layout).getLayoutParams().height = 
//			findViewById(R.id.menu_item_home).setPadding(0,actionBarSize,0,0);
		}
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
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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
//		Log.i(this.toString(),"POSITION: " + id);

//		ListView lv = (ListView) findViewById(R.id.menu_list);
//		View view = lv.getChildAt(position - lv.getFirstVisiblePosition());
		if (currPos == null || currPos != view) {
			int heading = Category.menuPositionToCategory(position);
			String categoryName = Category.getCategoryName(heading,true);
			int colorBar = Category.menuPositionToTopColour(position,getResources());
			int colorText = Category.getCategoryColour(heading);

			setHeadingAndColor(categoryName,colorBar,colorText);

			currentCategory = Category.menuPositionToCategory(position);
			populateNews();

			if (currPos != null) {
				currPos.setBackgroundColor(getResources().getColor(R.color.Transparent));
			}
			if (view != null) {
				view.setBackgroundColor(getResources().getColor(R.color.black_05));
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
		TextView heading = (TextView) findViewById(R.id.page_heading);
		heading.setTypeface(Typeface.createFromAsset(getAssets(),"Lato-Regular.ttf"));

		findViewById(R.id.menu_button).setOnClickListener(clickEvent);
		findViewById(R.id.more_button).setOnClickListener(clickEvent);

		findViewById(R.id.close_button).setOnClickListener(clickEvent);
		findViewById(R.id.back_dark_underlay).setOnClickListener(clickEvent);

		findViewById(R.id.refresh_button).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_contact_us).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_settings).setOnClickListener(clickEvent);
		findViewById(R.id.drop_down_browser).setOnClickListener(clickEvent);

		findViewById(R.id.dummy_actionBar_underlay).setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event)
			{
//				findViewById(R.id.drop_down).setVisibility(View.GONE);
				if (findViewById(R.id.drop_down).getVisibility() == View.VISIBLE) {
					vis(R.id.drop_down,GONE,true);
				}
				return false;
			}
		});

	}

	public OnClickListener clickEvent = new OnClickListener() {
		@Override
		public void onClick(View v)
		{
			switch (v.getId()) {
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
				populateNews(true);
				break;
			case R.id.close_button:
			case R.id.back_dark_underlay:
				closeArticle();
				break;
			case R.id.drop_down_contact_us:
				vis(R.id.drop_down,GONE,true);
				Intent intent1 = new Intent(TabletActivity.this,ContactActivity.class);
				startActivity(intent1);
				break;
			case R.id.drop_down_settings:
				vis(R.id.drop_down,GONE,true);
				Intent intent2 = new Intent(TabletActivity.this,Preferences.class);
				startActivity(intent2);
				break;
			case R.id.drop_down_browser:
				vis(R.id.drop_down,GONE,true);
				String url = currentURL;
				if (!articleOpen) {
					url = "http://theboar.org/" + Category.getCategoryName(currentCategory,false) + "/";
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

	private void populateNews()
	{
		populateNews(false);
	}

	private void populateNews(boolean useInternet)
	{

		if (!populating) {
			populating = true;
			newsStore = new NewsStore(getApplicationContext());

			l1 = (LinearLayout) findViewById(R.id.tablet_lld1);
			l2 = (LinearLayout) findViewById(R.id.tablet_lld2);
			l3 = (LinearLayout) findViewById(R.id.tablet_lld3);

			l1.removeAllViews();
			l2.removeAllViews();
			l3.removeAllViews();

			// Decide which layouts are present based on wether this is a phone or tablet
			// and wether we are in portrait or landscape mode.
			//			l3.setVisibility(LinearLayout.GONE); l3 is GONE by default
			boolean isPortrait = CNS.isPortrait(this);
			boolean isTablet = CNS.isTablet(this);

			Log.d("Print","isPortrait:" + isPortrait);
			if (!isTablet) {
				if (isPortrait) l2.setVisibility(LinearLayout.GONE);
				else if (!isPortrait) l2.setVisibility(LinearLayout.VISIBLE);
			}
			else if (isTablet) {
				l3.setVisibility(LinearLayout.VISIBLE);
				if (isPortrait) l3.setVisibility(LinearLayout.GONE);
				else if (!isPortrait) l3.setVisibility(LinearLayout.VISIBLE);
			}

			HeadlineAsyncTask hat = new HeadlineAsyncTask();
			hat.execute("home");
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

		@Override
		protected void onPreExecute()
		{
			// TODO Auto-generated method stub
			super.onPreExecute();
			LinearLayout loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
			loadingLayout.setVisibility(LinearLayout.VISIBLE);
			TextView loadingTV = (TextView) findViewById(R.id.loading_text);
			loadingTV.setText("Loading ");
			//ScrollView sv = (ScrollView) findViewById(R.id.scrollView1);
			//sv.setPadding(0, (int)(sv.getPaddingTop()*0.28f), 0, 0);
		}

		@Override
		protected Void doInBackground(String... params)
		{
			newsStore.getHeadlinesFromCategory(currentCategory,10,this);
			return null;
		}

		@Override
		public void onHeadlineParsed(IHeadline hl, String loadingMessage)
		{
			publishProgress(hl,loadingMessage);
		}

		@Override
		protected void onProgressUpdate(Object... values)
		{
			super.onProgressUpdate(values);
			Headline hl = (Headline) values[0];
			String loadingMessage = (String) values[1];
			// Do stuff on UI thread
			addHeadlineToView(hl);
			TextView loadingTV = (TextView) findViewById(R.id.loading_text);
			loadingTV.setText(loadingMessage);
		}

		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);
			LinearLayout loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
			loadingLayout.setVisibility(LinearLayout.GONE);
			populating = false;
		}

	}

	private int getNextLayoutAdd(int i, boolean largeScreen)
	{
		int L = 1;
		if (largeScreen == false) {
			if (CNS.isPortrait(this)) {
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
			ScrollView sv = (ScrollView) findViewById(R.id.storyScrollView);
//			sv.fullScroll(ScrollView.FOCUS_UP);
			ImageView iv = (ImageView) findViewById(R.id.story_newsImage);
			Drawable headlineImage = hl.getImage();
			if (headlineImage != null) {
				iv.setImageDrawable(headlineImage);
			}
			TextView tv = (TextView) findViewById(R.id.story_headline);
			tv.setText(hl.getHeadline());

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

			startArticle();

			int width = webViewLL.getMeasuredWidth() / 2 - CNS.getDPfromPX(10,getApplicationContext());

			String s = "<html><head><meta name=\"viewport\"\"content=\"width=" + width + "\" /></head>";
			s += "<body>";
			String sEnd = "</body></html>";
			String cleanUp = changeHTMLattr(hl.getArticleHTML(),"width",Integer.toString(width));
			cleanUp = changeHTMLattr(cleanUp,"height","auto");

			webview.loadDataWithBaseURL(null,s + cleanUp + sEnd,"text/html","UTF-8",null);

			webViewLL.addView(webview);

		}

	}

	private String changeHTMLattr(String html, String attr, String value)
	{
//		html = html.replaceAll("\\s*(?:width)\\s*=\\s*\"[^\"]*\"\\s*"," max-width=\"100%\" ");
		html = html.replaceAll("\\s*(?:" + attr + ")\\s*=\\s*\'[^\']*\'\\s*"," " + attr + "=\"" + value + "\" ");
		html = html.replaceAll("\\s*(?:" + attr + ")\\s*=\\s*\"[^\"]*\"\\s*"," " + attr + "=\"" + value + "\" ");
		return html;
	}

	private void startArticle()
	{
		View articleView = findViewById(R.id.news_story_frame);
		Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.article_start);
		articleView.setVisibility(View.VISIBLE);
		articleView.startAnimation(anim);

		findViewById(R.id.menu_button).setVisibility(FrameLayout.GONE);
		findViewById(R.id.close_button).setVisibility(FrameLayout.VISIBLE);

		menu.setSlidingEnabled(false);
		articleOpen = true;

	}
	private void closeArticle()
	{
		View articleView = findViewById(R.id.news_story_frame);
		Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.article_close);
		articleView.startAnimation(anim);
		articleView.setVisibility(View.GONE);
//		articleView.startAnimation(CNS.Animate(articleView,CNS.FADE,1,0,400));

		findViewById(R.id.menu_button).setVisibility(FrameLayout.VISIBLE);
		findViewById(R.id.close_button).setVisibility(FrameLayout.GONE);

		menu.setSlidingEnabled(true);
		articleOpen = false;
	}

	private void addHeadlineToView(IHeadline hl)
	{
		View newsItems = null;
		ImageView iv = null;
		LinearLayout categoryColor;
		TextView authorName = null, newsDate = null, newsName = null;

		//--------------------calculating the height of content-------------------------
		//In case of some error--------------------------
		Drawable d = hl.getImage();
		if (d != null)
		{
			int hMin = CNS.getPXfromDP(150,this);
			int hMax = CNS.getPXfromDP(700,this);
			int hReal = d.getMinimumHeight();
			int hFinal = hMin;
			if (hReal > hMax)
				hFinal = hMax;

			//---------------------------------------------------------

			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(CNS.getFillParent(),hFinal);
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

			categoryColor = (LinearLayout) newsItems.findViewById(R.id.content_typecolor);
			categoryColor.setBackgroundColor(Category.getCategoryColour(hl.getCategory(),getResources()));

			TextView categoryName = (TextView) newsItems.findViewById(R.id.category_name);
			categoryName.setText(Category.getCategoryNameShort(hl.getCategory()).toUpperCase());
			//--------------------------------News Title||-----------------------------
			newsName = (TextView) newsItems.findViewById(R.id.topicname);
			newsName.setText(hl.getHeadline());
			//-----------------------------------------News Author------------------------------------
			authorName = (TextView) newsItems.findViewById(R.id.author_name);
			authorName.setText(hl.getAuthor());
			//-------------------------------------------Date----------------------------------------
			newsDate = (TextView) newsItems.findViewById(R.id.content_date);
			String dateTimeString = DateFormat.getDateInstance().format(hl.getDatePublished());
//			Log.i(this.toString(),"DATE IS: " + hl.getDatePublished().toString());
			newsDate.setText(dateTimeString);
			//-------------------------------Set On touch/Click-----------------------------------------
			newsItems.setOnClickListener(new ArticleClickListener(hl));
			CNS.onTouchHighlight(getApplicationContext(),newsItems,((FrameLayout) newsItems).getChildAt(0));
			//-------------------------------Finally Add View (Old Tablet Code)-----------------------------------------
			headlinesParsedSoFar++;
			switch (getNextLayoutAdd(headlinesParsedSoFar,CNS.isTablet(this)))
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
