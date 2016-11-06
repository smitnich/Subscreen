package com.subscreen;

import android.text.Html;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.app.Activity;

import java.nio.charset.Charset;

public class AndroidOutput implements Output {

	TextView out = null;
    Activity activity;
    String lastText;
	public static float textSize = 0.0f;
	static float minSize = 4.0f;
	static float maxSize = 49.0f;
	AndroidOutput(Activity act, float size)
	{
		//Only update text size if it hasn't been set this run
		if (textSize == 0.0f) {
			textSize = size;
			maxSize = size*3;
		}
        activity = act;
	}
	@Override
	public void outputText(final String text) {
        final String tmpText = text;
        lastText = text;
		activity.runOnUiThread (new Runnable()
		{
			public void run() {
				out.setText(Html.fromHtml(tmpText));
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
		out.setTextSize(textSize);
	}
	public void zoomIn() {
		textSize += 3;
		if (textSize > maxSize)
			textSize = maxSize;
		out.setTextSize(textSize);
	}
	public void zoomOut() {
		textSize -= 3;
		if (textSize < minSize)
			textSize = minSize;
		out.setTextSize(textSize);
	}
}
