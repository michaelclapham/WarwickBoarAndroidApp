package org.theboar.android;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.theboar.android.Headline.Node;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class CNS
{

	public static final String FADE = "fd", ROTATE = "rt", SCALE = "sc";
	public static final String LOGPRINT = "Boar";
	public static final int TRANSPARENT = 0x00000000;

	public static SharedPreferences getSharedPreferences(Context cntxt)
	{
		return PreferenceManager.getDefaultSharedPreferences(cntxt);
	}

	public static File getExternalDir()
	{
		File externalDir = new File(Environment.getExternalStorageDirectory(),"Boar");

		if (!externalDir.exists()) {
			if (!externalDir.mkdirs()) {
				Log.e(CNS.LOGPRINT,"Failed to create directory");
				return null;
			}
		}
		return externalDir;
	}

	public static File getFavouriteFile()
	{
		return new File(getExternalDir().getAbsoluteFile() + File.separator + "favourite.json");
	}
	public static File getCategoryCacheFile(Context context, String categoryName)
	{
//		String categoryName = Category.getCategoryName(categoryId,false,false);
		String path = context.getCacheDir().getAbsolutePath();
		return new File(path + categoryName + ".json");
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static int getActionBarHeight(Activity act)
	{
		TypedValue tv = new TypedValue();
		act.getTheme().resolveAttribute(android.R.attr.actionBarSize,tv,true);
		return act.getResources().getDimensionPixelSize(tv.resourceId);
	}

	public static int getPXfromDP(int dp, Context cntx)
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dp,cntx.getResources().getDisplayMetrics());
	}

	public static int getDPfromPX(int dp, Context cntx)
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dp,cntx.getResources().getDisplayMetrics());
	}

	public static boolean isTablet(Context context)
	{
		return (context.getResources().getConfiguration().screenLayout
		& Configuration.SCREENLAYOUT_SIZE_MASK)
		>= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static boolean isPortrait(Context context)
	{
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) return true;
		else return false;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("InlinedApi")
	public static int getFillParent()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) { return FrameLayout.LayoutParams.MATCH_PARENT; }
		return FrameLayout.LayoutParams.FILL_PARENT;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void setAlpha(View v, float value)
	{
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			v.setAlpha(value);
		}
	}

	public static FrameLayout getTagBox(String tag, Context context)
	{
		FrameLayout fmTemp = new FrameLayout(context);
		int dpS = CNS.getDPfromPX(3,context);
		int dpL = CNS.getDPfromPX(8,context);
		fmTemp.setPadding(dpL,dpS,dpL,dpS);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT,Gravity.CENTER_VERTICAL);
		params.setMargins(dpL,0,0,0);
		fmTemp.setBackgroundColor(context.getResources().getColor(R.color.black_05));

		fmTemp.setLayoutParams(params);
		TextView tvTemp = new TextView(context);
		tvTemp.setText(tag);
		tvTemp.setTextColor(context.getResources().getColor(R.color.black_45));
//			tvTemp.setTypeface(null,Typeface.BOLD);
		fmTemp.addView(tvTemp);
		return fmTemp;
	}

	/**@param type
	 * 	one of FADE, ROTATE, SCALE
	 * @param from
	 * 	current - alpha, angle, scale 0:invisible
	 * @param to
	 * 	resulting - alpha, angle, scale
	 * @param duration
	 * 	Duration Ideally 400 for Fade and 1000 for rotate**/
	public static Animation Animate(View view, String type, float from, float to,
			int duration, boolean fillAfter)
	{
		Animation anim = null;
		if (type == FADE)
		{
			anim = new AlphaAnimation(from,to);
			if (fillAfter) {
				if (to == 1) view.setVisibility(View.VISIBLE);
				else if (to == 0) view.setVisibility(View.GONE);
				else anim.setFillAfter(true);
			}
		}
		else if (type == ROTATE)
		{
			anim = new RotateAnimation(from,to,Animation.RELATIVE_TO_SELF,
					0.5f,Animation.RELATIVE_TO_SELF,0.5f);

		}
		else if (type == SCALE)
		{
			anim = new ScaleAnimation(from,to,from,to,Animation.RELATIVE_TO_SELF,
					0.5f,Animation.RELATIVE_TO_SELF,0.5f);
			anim.setInterpolator(new DecelerateInterpolator());
		}
		else {
			return null;
		}
		anim.setDuration(duration);
//		if (fillAfter) anim.setFillAfter(true);
		return anim;
	}

	public static void onTouchHighlight(final Context ctx, View v, final View animView)
	{
		v.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{

				switch (event.getAction())
				{
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_OUTSIDE:
				case MotionEvent.ACTION_UP:
				{
					if (animView instanceof FrameLayout) {
						ColorDrawable trans = new ColorDrawable(
								ctx.getResources().getColor(R.color.Transparent));
						((FrameLayout) animView).setForeground(trans);
					}
					break;
				}
				case MotionEvent.ACTION_DOWN:
				{
					if (animView instanceof FrameLayout) {
						ColorDrawable highlight = new ColorDrawable(
								ctx.getResources().getColor(R.color.black_10));
						((FrameLayout) animView).setForeground(highlight);
					}
					break;
				}
				}

				// if you reach here, you have consumed the event
				return false;
			}
		});
	}

	public static boolean isNetworkConnected(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else return true;
	}

	public static String timeElapsed(Date date)
	{
		long epoch = date.getTime();
		String timePassedString = (String) DateUtils.getRelativeTimeSpanString
				(epoch,System.currentTimeMillis(),DateUtils.SECOND_IN_MILLIS);
		return timePassedString;
	}

	public static String generateBetterHTML(int maxWidth, String articleHTML, Context context)
	{
		int width = maxWidth / 2 - CNS.getDPfromPX(10,context);
		String s = "<html><head><meta name=\"viewport\"\"content=\"width=" + width + "\" /></head>";
		s += "<body style='width=" + width + "'>";
		String sEnd = "</body></html>";
		String str = articleHTML;
		str = correctBoarHTML(str,width);
		String data = s + str + sEnd;
		return data;
	}

	public static String correctBoarHTML(String html, int parentWidth)
	{
		Document doc = Jsoup.parse(html);

		//-----------------------for img tags---------------------------------
		if (html.contains("<img")) {
			Elements links = doc.getElementsByTag("img");
			for (Element link : links) {
				try {
					String classes = link.attr("class");

					if (matchesPattern(classes,"alignright|alignleft")) {
						if (classes.contains("alignright")) {
							link.attr("style","float:right");
						}
						String width = link.attr("width");
						if (Integer.parseInt(width) > parentWidth / 3) {
							link.attr("height","auto");
							link.attr("width","100%");
						}
					} else {
						link.attr("height","auto");
						link.attr("width","100%");
					}
				}
				catch (Exception e) {}
			}
			Elements caption = doc.getElementsByAttributeValueContaining("class","wp-caption");
			for (Element div : caption) {
				div.attr("style","width:100%");
			}
		}
		//-----------------------for iFrame tags---------------------------------
		if (html.contains("<iframe")) {
			Elements linkYouT = doc.getElementsByTag("iframe");
			for (Element link : linkYouT) {
				try {
					String url = link.attr("src");
					if (url.contains("?")) {
						url = url.substring(0,url.indexOf("?"));
					}
					String playButtonUrl = "https://www.youtube.com/yt/advertise/medias/images/yt-advertise-whyitworks-playbutton.png";
					String videoID = url.substring(url.indexOf("/embed/") + 7,url.length());
					String imgUrl = "http://img.youtube.com/vi/" + videoID + "/0.jpg";
					String div = "<div style='position:relative'>"
							+ "<img style='position: absolute;left: 0;right: 0;margin: auto;bottom: 0;width: 60px;top: 0;' src='"
							+ playButtonUrl + "'/>" + "<a href='" + url + "'>" +
							"<img src='" + imgUrl + "' style='width:100%'/>" + "</a></div>";

					link.replaceWith(new DataNode(div,""));
				}
				catch (Exception e) {}
			}
		}
//		link.replaceWith(new DataNode("<div></div>",width));
		return doc.toString();
	}

	public static boolean matchesPattern(String str, String pattern)
	{
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		if (m.find()) { return true; }
		return false;
	}

	public static View getFavsCountBox(int catID, int count, Context context)
	{
		int dpS = CNS.getPXfromDP(3,context);
		int dpL = CNS.getPXfromDP(8,context);
		LinearLayout.LayoutParams params;

		params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,CNS.getPXfromDP(20,context));
		params.setMargins(0,0,dpL,0);
		LinearLayout fmTemp = new LinearLayout(context);
		fmTemp.setBackgroundColor(Category.getCategoryColourBar(catID,context.getResources()));
		fmTemp.setLayoutParams(params);
		//----------------------count num--------------------------
		params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.setMargins(dpL,0,dpL,0);
		params.gravity = Gravity.CENTER_VERTICAL;

		TextView tvCount = new TextView(context);
		tvCount.setText(Integer.toString(count));
		tvCount.setTextColor(context.getResources().getColor(R.color.white_90));
		tvCount.setTypeface(null,Typeface.BOLD);
		tvCount.setLayoutParams(params);

		//---------------------------CatName---------------------------------------

		TextView tvName = new TextView(context);
		tvName.setText(Category.getCategoryName(catID,false,true));
		tvName.setTextColor(context.getResources().getColor(R.color.black_45));
		tvName.setLayoutParams(params);
		//------------------------------------greybox-----------------------------
		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

		FrameLayout fmgrey = new FrameLayout(context);
		fmgrey.setLayoutParams(params);
		fmgrey.setPadding(dpL,0,dpL,0);
		fmgrey.setBackgroundColor(context.getResources().getColor(R.color.fav_box));//grey 0xFFe5e5e5

		//----------------------------------------------------------------------------

		fmTemp.addView(tvCount);
		fmgrey.addView(tvName);
		fmTemp.addView(fmgrey);
		return fmTemp;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
	{
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
			{
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(),entry.getValue());
		}
		return result;
	}

	public static View getCommentView(String name, String comment, Node parent, int depth, Context context)
	{
		View fm = BoarActivity.activity.getLayoutInflater().inflate(
				context.getResources().getLayout(R.layout.comment_box),null,false);
		LinearLayout ll = (LinearLayout) fm.findViewById(R.id.comment_single_root);
		int lPadding = getPXfromDP(10 * depth,context);
		ll.setPadding(lPadding,0,0,0);

		TextView txtName = (TextView) fm.findViewById(R.id.comment_name);
		if (depth > 0) {
			txtName.setText(Html.fromHtml(name) + "  >  " + Html.fromHtml(parent.name));

		} else {

			txtName.setText(Html.fromHtml(name));
		}

		TextView txtComment = (TextView) fm.findViewById(R.id.comment_content);
		txtComment.setText(Html.fromHtml(comment));
		return fm;
	}

}
