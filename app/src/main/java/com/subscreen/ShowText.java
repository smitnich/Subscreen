package com.subscreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Button;
import android.view.MenuItem;

public class ShowText extends FragmentActivity {
    static Button pauseButton;
    static Button backButton;
    static Button nextButton;
    static Button prevButton;
    SubtitlePlayer playerInstance = null;
    String[] charsets = {"UTF-8","UTF-16BE","UTF-16LE","US-ASCII","ISO-8859-1"};
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_text);
		TextView t = (TextView)findViewById(R.id.edit_message);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        initMenu();
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
        prevButton = (Button) findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerInstance.prevSubtitle();
            }
        });
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerInstance.nextSubtitle();
            }
        });

        playerInstance.main(t, this.getApplicationContext(), fileName, this);
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
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.menu_encoding_choice);
        ListView lv = (ListView ) dialog.findViewById(R.id.choices);
        lv.setAdapter(new ArrayAdapter(this, R.layout.menu_encoding, charsets));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String encodingName = charsets[position];
                playerInstance.setEncoding(encodingName);
                dialog.hide();
            }
        });
        dialog.setTitle("Choose Encoding");
        dialog.setCancelable(true);
        final Button charset = (Button) findViewById(R.id.encodingButton);
        charset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }
    public void pause(View v)
    {
        playerInstance.pause();
    }
    public static void setButton(String input){
        pauseButton.setText(input);
    }

}
