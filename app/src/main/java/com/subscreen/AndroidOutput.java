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
	AndroidOutput(Activity act, String charsetName)
	{
        MenuItem tmpItem;
        destCharsetString = charsetName;
        loadedCharset = Charset.forName("UTF-8");
        destCharset = Charset.forName(charsetName);
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
}
