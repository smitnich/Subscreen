package com.subscreen;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Button;
import android.view.MenuItem;

public class ShowText extends FragmentActivity {
    static Button pauseButton;
    static Button backButton;
    PopupMenu encodingMenu;
    SubtitlePlayer playerInstance = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_text);
		TextView t = (TextView)findViewById(R.id.edit_message);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        initMenu();
        encodingMenu = new PopupMenu(ShowText.this, (Button) findViewById(R.id.encodingButton));
        Bundle b = getIntent().getExtras();
        String fileName = b.getString("fileName");
        playerInstance = new SubtitlePlayer();
        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToSelectScreen();
            }
        });

        playerInstance.main(t, this.getApplicationContext(), fileName, this,encodingMenu);
	}
    void displayBackMessage(String message, String title)
    {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        returnToSelectScreen();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
    public void returnToSelectScreen(){
        Intent intent = new Intent(ShowText.this, SelectFile.class);
        startActivity(intent);
        finish();
    }
    void initMenu()
    {
        final Button charset = (Button) findViewById(R.id.encodingButton);
        charset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                //Inflating the Popup using xml file
                encodingMenu.getMenuInflater()
                        .inflate(R.menu.menu_encoding, encodingMenu.getMenu());

                //registering popup with OnMenuItemClickListener
                encodingMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        setEncoding(item.getTitle());
                        return true;
                    }
                });

                encodingMenu.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method

    }
    public void setEncoding(CharSequence title)
    {
        playerInstance.outputTo.setDestCharset(title.toString());
    }
    public void pause(View v)
    {
        playerInstance.pause();
    }
    public static void setButton(String input){
        pauseButton.setText(input);
    }

}
