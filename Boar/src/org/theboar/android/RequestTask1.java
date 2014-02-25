package org.theboar.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class RequestTask1 extends AsyncTask<String, String, String>{
	
	private HeadlineList list;
	
	public RequestTask1(HeadlineList list){
		this.list = list;
	}

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
    }

    private int printCount = 0;
    
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(result != null){
        	//Log.v(this.toString(), result);
        	printCount++;
        	Log.v(this.toString(), "Print Count: " + printCount);
        	
        	//Create JSON Object
        	try {
				JSONObject jObject = new JSONObject(result);
				JSONArray jArray = jObject.getJSONArray("posts");
				for(int i = 0; i < jArray.length(); i++){
					JSONObject story = jArray.getJSONObject(i);
					Headline head = new Headline();
					head.setHeadlineTitle(story.getString("title"));
					//String imageURL = story.getJSONArray("attachments").getJSONObject(0).getJSONObject("images").getJSONObject("full").getString("url");
					String imageURL = story.getJSONObject("thumbnail_images").getJSONObject("full").getString("url");
					//String imageURL = "http://theboar.org/wp-content/uploads/2014/02/Dating.jpg";
					head.setLowResImage(drawableFromUrl(imageURL));
					Log.v(this.toString(), "STORY: " + story.getString("title"));
					Log.v(this.toString(), "IMG URL: " + imageURL);
					list.addHeadline(head);
				}
				list.setDoneLoading(true);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
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