package org.theboar.android;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Preferences extends PreferenceActivity
{
	private SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		addPreferencesFromResource(R.layout.preferences);

		settings = CNS.getSharedPreferences(getApplicationContext());

		actionBarTweak();
		setPreferenceOptions();
	}

	private void actionBarTweak()
	{
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
		title.setText("Settings");

		findViewById(R.id.menu_button).setVisibility(View.GONE);
		findViewById(R.id.more_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.actionbar_logo).setVisibility(View.GONE);
	}

	@SuppressWarnings("deprecation")
	private void setPreferenceOptions()
	{

		String version;
		try {
			version = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
			findPreference("version").setTitle("Version " + version);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		final ListPreference defCat = (ListPreference) findPreference("default_category");
		defCat.setTitle("Default Category: " + settings.getString("default_category","Not Set"));
		defCat.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				preference.setTitle("Default Category: " + (String) newValue);
				return true;
			}
		});

		Preference buttonRate = (Preference) findPreference("rate");
		buttonRate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0)
			{
				Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
				Intent goToMarket = new Intent(Intent.ACTION_VIEW,uri);
				goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					getApplicationContext().startActivity(goToMarket);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(getApplicationContext(),"Cant launch Market",Toast.LENGTH_LONG).show();
				}
				return true;
			}
		});

		Preference clearCache = (Preference) findPreference("clear_cache");
		clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0)
			{
				//TODO
				Toast.makeText(getApplicationContext(),"Cache Cleared!",Toast.LENGTH_LONG).show();
				return true;
			}
		});

		Preference maxCache = (Preference) findPreference("max_cache");
		maxCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0)
			{
				showAlertBox();
				return true;
			}

			protected void showAlertBox()
			{
				final AlertDialog.Builder alert = new AlertDialog.Builder(Preferences.this);

				alert.setTitle("Select Max Cache size");

				LinearLayout linear = new LinearLayout(Preferences.this);
				linear.setOrientation(1);

				int preSize = settings.getInt("max_cache",20);

				final TextView text = new TextView(Preferences.this);
				text.setPadding(10,10,10,10);
				text.setText(preSize + " MB");
				text.setTextSize(25);
				text.setGravity(Gravity.CENTER_HORIZONTAL);

				SeekBar seek = new SeekBar(Preferences.this);
//				seek.setPadding(10,10,10,10);
				seek.setMax(10);
				Log.d("PRINT","Size:" + preSize);
				seek.setProgress(preSize / 20);
				seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					int size;

					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
					{
						size = progress * 20;
						text.setText(Integer.toString(size) + " MB");
					}

					public void onStartTrackingTouch(SeekBar seekBar)
					{}

					public void onStopTrackingTouch(SeekBar seekBar)
					{
						SharedPreferences.Editor editor = settings.edit();
						editor.putInt("max_cache",size);
						editor.commit();
						Log.d("PRINT","COMMITTED " + size);
					}
				});

				linear.addView(text);
				linear.addView(seek);

				alert.setView(linear);

				alert.setPositiveButton("Set",new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						int maxCacheSize = settings.getInt("max_cache",20);
						//TODO change max cache from here. maxCacheSize
						Toast.makeText(getApplicationContext(),"Max Cache Set",Toast.LENGTH_LONG).show();
					}
				});

				alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{}
				});

				alert.show();

			}

		});

		Preference credits = (Preference) findPreference("credits");
		credits.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0)
			{
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(Preferences.this);
				alertDialog.setTitle("Credits");
				alertDialog.setMessage("Snehil, Michael, George, Kate");
				alertDialog.setPositiveButton("OK",null);
				alertDialog.show();
				return true;
			}
		});

	}

}
