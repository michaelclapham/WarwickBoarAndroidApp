package org.theboar.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MySimpleArrayAdapter extends ArrayAdapter<String> {
  private final Context context;
  private final String[] values;

  public MySimpleArrayAdapter(Context context, String[] values) {
    super(context, R.layout.menu_element, values);
    this.context = context;
    this.values = values;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.menu_element, parent, false);
    TextView tv = (TextView) rowView.findViewById(R.id.menu_item_text);
    tv.setText(values[position]);
    LinearLayout ll = (LinearLayout) rowView.findViewById(R.id.menu_item_colour);
    ll.setBackgroundColor(Category.getCategoryColour(Category.getCategoryIDFromString(values[position]),
    		context.getResources()));
    
    return rowView;
  }
} 