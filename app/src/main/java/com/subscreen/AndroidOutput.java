package com.subscreen;

import android.text.Html;
import android.widget.TextView;
import android.app.Activity;

public class AndroidOutput implements Output {

	TextView out;
    Activity activity;
	AndroidOutput(Activity act)
	{
        activity = act;
	}
	@Override
	public void outputText(final String text) {
        String tmpText = text;
		activity.runOnUiThread (new Runnable()
		{
			public void run() {
				out.setText(Html.fromHtml(text));
			}
		});
	}

	@Override
	public void clearText() {
		outputText("");
	}
	public void setTextView(TextView t)
	{
		out = t;
	}
}
