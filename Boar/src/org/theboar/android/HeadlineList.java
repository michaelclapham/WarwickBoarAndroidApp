package org.theboar.android;

import java.util.ArrayList;
import java.util.List;

public class HeadlineList implements IHeadlineList {

	private ArrayList<IHeadline> list;
	private ArrayList<IHeadline> readyList;
	private int numLoaded = 0;
	private int numTotal;
	private boolean doneLoading;
	
	public HeadlineList(int size) {
		list = new ArrayList<IHeadline>(size);
		readyList = new ArrayList<IHeadline>(size);
	}
	
	public void addHeadline(Headline hl){
		list.add(hl);
		readyList.add(hl);
		numLoaded++;
	}
	
	@Override
	public List<IHeadline> getList() {
		return list;
	}

	@Override
	public String getLoadingString() {
		return "Loading";
	}

	@Override
	public boolean doneLoading() {
		return doneLoading;
	}

	@Override
	public int numLoaded() {
		return numLoaded;
	}

	@Override
	public int numTotal() {
		return numTotal;
	}

	public void setDoneLoading(boolean b) {
		doneLoading = b;
		
	}

	@Override
	public List<IHeadline> getReadyList() {
		return readyList;
	}

}
