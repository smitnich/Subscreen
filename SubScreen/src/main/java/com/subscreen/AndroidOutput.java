package com.subscreen;

import android.text.Html;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.app.Activity;

import java.nio.charset.Charset;

public class AndroidOutput implements Output {

	TextView out;
    Activity activity;
    Charset loadedCharset;
    Charset destCharset;
    String destCharsetString;
    String lastText;
	public float textSize;
	final static float minSize = 4.0f;
	final static float maxSize = 49.0f;
	AndroidOutput(Activity act, float size)
	{
		textSize = size;
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
	}
    public void setDestCharset(String in)
    {
        destCharset = Charset.forName(in);
        resetText();
    }
    void resetText() {
        outputText(lastText);
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
