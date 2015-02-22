package com.subscreen;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Button;

public class ShowText extends FragmentActivity {
    static Button pauseButton;
    static Button backButton;
    SubtitlePlayer playerInstance = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_text);
		TextView t = (TextView)findViewById(R.id.edit_message);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        Bundle b = getIntent().getExtras();
        String fileName = b.getString("fileName");
        playerInstance = new SubtitlePlayer();
		playerInstance.main(t, this.getApplicationContext(), fileName, this);
        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToSelectScreen();
            }
        });
	}
    public void returnToSelectScreen(){
        Intent intent = new Intent(ShowText.this, SelectFile.class);
        startActivity(intent);
        finish();
    }

    public void pause(View v)
    {
        playerInstance.pause();
    }
    public static void setButton(String input){
        pauseButton.setText(input);
    }

}
