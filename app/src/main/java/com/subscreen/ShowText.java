package com.subscreen;

import android.view.View;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Button;

public class ShowText extends FragmentActivity {
    static Button pauseButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_text);
		TextView t = (TextView)findViewById(R.id.edit_message);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        Bundle b = getIntent().getExtras();
        String fileName = b.getString("fileName");
		Main.main(t,this.getApplicationContext(),fileName,this);
	}

    public void pause(View v)
    {
        Main.pause();
    }
    public static void setButton(String input){
        pauseButton.setText(input);
    }

}
