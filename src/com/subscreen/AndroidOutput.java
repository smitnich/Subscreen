package com.subscreen;

import android.widget.TextView;

public class AndroidOutput implements Output {

	TextView out;
	AndroidOutput()
	{
	}
	@Override
	public void outputText(final String text) {
		// TODO Auto-generated method stub
		out.post(new Runnable()
		{
			public void run() {
				out.setText(text);
			}
		});

		
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
