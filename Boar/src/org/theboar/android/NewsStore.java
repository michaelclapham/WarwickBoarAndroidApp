package org.theboar.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
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
		//RequestTask1 rt = new RequestTask1(list);
		//rt.execute("http://www.theboar.org/?json=1");
		
		// Load synchronously
		DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
		HttpPost httppost = new HttpPost("http://theboar.org/?json=1");
		// Depends on your web service
		httppost.setHeader("Content-type", "application/json");

		InputStream inputStream = null;
		String result = null;
		try {
		    HttpResponse response = httpclient.execute(httppost);           
		    HttpEntity entity = response.getEntity();

		    inputStream = entity.getContent();
		    // json is UTF-8 by default
		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
		    StringBuilder sb = new StringBuilder();

		    String line = null;
		    while ((line = reader.readLine()) != null)
		    {
		        sb.append(line + "\n");
		    }
		    result = sb.toString();
		} catch (Exception e) { 
		    // Oops
		}
		finally {
		    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
		}
		
		// Now parse JSON
		//Create JSON Object
		if(result != null){
	    	try {
				JSONObject jObject = new JSONObject(result);
				JSONArray jArray = jObject.getJSONArray("posts");
				for(int i = 0; i < jArray.length(); i++){
					JSONObject story = jArray.getJSONObject(i);
					Headline head = new Headline();
					String storyTitle = story.getString("title");
					// Replace HTML codes with correct characters
					// @TODO: More comprehensive code to do this
					storyTitle = storyTitle.replace("&#8217;", "’");
					storyTitle = storyTitle.replace("&#8216;", "‘");
					storyTitle = storyTitle.replace("&#8218;", "‚");
					storyTitle = storyTitle.replace("&#8220;", "“");
					storyTitle = storyTitle.replace("&#8221;", "”");
					storyTitle = storyTitle.replace("&#8222;", "„");
					head.setHeadlineTitle(storyTitle);
					//String imageURL = story.getJSONArray("attachments").getJSONObject(0).getJSONObject("images").getJSONObject("full").getString("url");
					String imageURL = story.getJSONObject("thumbnail_images").getJSONObject("medium").getString("url");
					//String imageURL = "http://theboar.org/wp-content/uploads/2014/02/Dating.jpg";
					head.setLowResImage(drawableFromUrl(imageURL));
					String dateTimeString = story.getString("date");
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss", Locale.ENGLISH);
					Date datePublished = df.parse(dateTimeString);
					head.setDatePublished(datePublished);
					head.setAuthor(story.getJSONObject("author").getString("name"));
					Log.v(this.toString(), "STORY: " + story.getString("title"));
					Log.v(this.toString(), "IMG URL: " + imageURL);
					head.setPageUrl(story.getString("url"));
					list.addHeadline(head);
				}
				list.setDoneLoading(true);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return list;
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
	
	public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }

}
