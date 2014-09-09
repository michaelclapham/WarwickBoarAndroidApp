package org.theboar.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class ContactActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_contact);

		View backButton = findViewById(R.id.back_button);
		backButton.setVisibility(View.VISIBLE);
		backButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		TextView title = (TextView) findViewById(R.id.actionbar_title);
		title.setText("About");

		findViewById(R.id.menu_button).setVisibility(View.GONE);
		findViewById(R.id.more_button).setVisibility(View.GONE);
		findViewById(R.id.drop_down_contact_us).setVisibility(View.GONE);
		findViewById(R.id.drop_down_favourite_icon).setVisibility(View.GONE);
		findViewById(R.id.drop_down_settings).setVisibility(View.GONE);
		findViewById(R.id.refresh_button).setVisibility(View.GONE);
		findViewById(R.id.actionbar_logo).setVisibility(View.INVISIBLE);

		findViewById(R.id.contact_email_value).setOnClickListener(onclick);
		findViewById(R.id.contact_about_link).setOnClickListener(onclick);
		findViewById(R.id.contact_social_fb).setOnClickListener(onclick);
		findViewById(R.id.contact_social_twit).setOnClickListener(onclick);

	}

	OnClickListener onclick = new OnClickListener() {

		@Override
		public void onClick(View v)
		{
			String url = null;
			switch (v.getId()) {
			case R.id.contact_email_value:
				String email = (String) ((TextView) findViewById(R.id.contact_email_value)).getText();
				url = "mailto: " + email;
				break;
			case R.id.contact_about_link:
				url = "http://theboar.org/about/";
				break;
			case R.id.contact_social_fb:
				url = "https://www.facebook.com/warwickboar";
				break;
			case R.id.contact_social_twit:
				url = "https://twitter.com/warwickboar";
				break;
			}
			if (url != null) {
				Intent i;
				i = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			} else if (v.getId() == R.id.more_button) {

			}

		}
	};

}
