package com.subscreen;

import android.text.Html;
import android.widget.TextView;

public class AndroidOutput implements Output {

	TextView out;
	AndroidOutput()
	{
	}
	@Override
	public void outputText(final String text) {
		// TODO Auto-generated method stub
        String tmpText = text;
		boolean res = out.post(new Runnable()
		{
			public void run() {
				out.setText(Html.fromHtml(text));
			}
		});
        if (!res)
        {
            System.out.println("Failed to send message");
        }
		
		//out.setText(text);
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
