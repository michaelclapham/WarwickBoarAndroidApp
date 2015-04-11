package org.theboar.android;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
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
		if (diff < 520)
		{
//			Log.d(CV.LogTAG,"Bottom Reached!");
			if (bttm != null)
				bttm.onBottomReached();
			// notify that we have reached the bottom
		}
		super.onScrollChanged(l,t,oldl,oldt);
	}

	private boolean mScrollable = true;

	public void setScrollingEnabled(boolean enabled)
	{
		mScrollable = enabled;
	}

	public boolean isScrollable()
	{
		return mScrollable;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// if we can scroll pass the event to the superclass
			if (mScrollable) return super.onTouchEvent(ev);
			// only continue to handle the touch event if scrolling enabled
			return mScrollable; // mScrollable is always false at this point
		default:
			return super.onTouchEvent(ev);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		// Don't do anything with intercepted touch events if 
		// we are not scrollable
		if (!mScrollable) return false;
		else return super.onInterceptTouchEvent(ev);
	}

	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
			int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY,
			boolean isTouchEvent)
	{
		return super.overScrollBy(deltaX,deltaY,scrollX,scrollY,scrollRangeX,scrollRangeY,
				maxOverScrollX,40,isTouchEvent);
	}
}
