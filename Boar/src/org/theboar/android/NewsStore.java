package org.theboar.android;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;

import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class NewsStore implements INewsStore {

	public NewsStore(){
		//
	}
	
	public HeadlineList getStubHeadlines(){
		File DataStorage = new File(
				Environment.getExternalStorageDirectory(),"Boar News");
		if (!DataStorage.exists()) {
			if (!DataStorage.mkdirs())
				Log.d("Print","failed to create directory");
		} else Log.d("Print","Directory Exists");

		File[] files = DataStorage.listFiles();

//		for (int i = 0; i < files.length; i++) Log.d("Print","" + files[i].getName());
		//-------------------------Start Searching and adding image files----------------------------
		//-------------------------MAKE decoding ASYNCTASK----------------------------
		
		HeadlineList headlines = new HeadlineList(files.length);
		
		for (int i = 0; i < files.length; i++) {
			// Load image
			String fileName = files[i].getName();
			Drawable d = null;
			try {
				FileInputStream fis = new FileInputStream(DataStorage
						+ File.separator + fileName);
				if (fileName.endsWith(".jpg")) {
					d = Drawable.createFromStream(fis,"news");
				}
			}
			catch (Exception e) {
				Log.d("Print","ERROR ERROR");
			}
			// Create new headline
			Headline headline = new Headline();
			headline.setHeadlineTitle(files[i].getName());
			headline.setLowResImage(d);
			// Add headline to list
			headlines.addHeadline(headline);
		}
		return headlines;
		
	}
	
	@Override
	public int requestRefresh() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean storeHeadline(IHeadline newHeadline) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IHeadlineList getHeadlines(Date dateFrom, Date dateTo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHeadlineList getHeadlines(int lastN) {
		HeadlineList list = new HeadlineList(lastN);
		new RequestTask1(list).execute("http://www.theboar.org/?json=1");
		return getStubHeadlines();
		//return list;
	}

	@Override
	public IHeadlineList getFavourites(int lastN) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHeadlineList getNew(int lastN) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHeadlineList getHeadlines(String category, int lastN) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHeadlineList basicSearch(String query, int lastN) {
		// TODO Auto-generated method stub
		return null;
	}

}
