package org.theboar.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
		findViewById(R.id.more_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.actionbar_logo).setVisibility(View.GONE);

		findViewById(R.id.contact_email_value).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				String email = (String) ((TextView) findViewById(R.id.contact_email_value)).getText();

				Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse("mailto: " + email));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);

//				Intent intent = new Intent(Intent.ACTION_SEND);
//				intent.setType("plain/text");
//				intent.putExtra(Intent.EXTRA_EMAIL,new String[] { email });
//				intent.putExtra(Intent.EXTRA_TEXT," \r\n ----------------------\r\n Sent from The Boar Android App.");
//				startActivity(Intent.createChooser(intent,"Send Email"));

			}
		});

	}

}
