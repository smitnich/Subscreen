package com.subscreen;

import android.text.Html;
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
    String[] charsets = {"UTF-8","UTF-16BE","UTF-16LE","US-ASCII","ISO-8859-1"};
	AndroidOutput(Activity act, String charsetName, PopupMenu charsetMenu)
	{
        destCharsetString = charsetName;
        loadedCharset = Charset.forName("UTF-8");
        destCharset = Charset.forName(charsetName);
        activity = act;
        for (String charset : charsets)
        {
            charsetMenu.getMenu().add(charset);
        }
	}
	@Override
	public void outputText(final String text) {
        final String tmpText = new String(text.getBytes(loadedCharset),destCharset);
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
