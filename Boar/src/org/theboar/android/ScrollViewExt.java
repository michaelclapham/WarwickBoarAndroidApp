package org.theboar.android;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

public class ScrollViewExt extends ScrollView
{

	BottomReachedListener bttm = null;

	public ScrollViewExt(Context context) {
		super(context);
	}

	public ScrollViewExt(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
	}

	public ScrollViewExt(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

	public void setBottomListener(BottomReachedListener btn)
	{
		this.bttm = btn;
	}
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		View view = (View) getChildAt(getChildCount() - 1);
		int diff = (view.getBottom() - (getHeight() + getScrollY()));
		if (diff < 20)
		{
//			Log.d(CV.LogTAG,"Bottom Reached!");
			if (bttm != null)
				bttm.onBottomReached();
			// notify that we have reached the bottom
		}
		super.onScrollChanged(l,t,oldl,oldt);
	}
}
