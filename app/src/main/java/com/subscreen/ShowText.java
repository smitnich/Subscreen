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

import java.util.ArrayList;
import java.util.List;

public class ShowText extends FragmentActivity {
    static Button pauseButton;
    static Button backButton;
    static Button nextButton;
    static Button prevButton;
    static Button convertFramerateButton;
    SubtitlePlayer playerInstance = null;
    ListView frameRateListView;
    String[] charsets = {"UTF-8","UTF-16BE","UTF-16LE","US-ASCII","ISO-8859-1","windows-1252"};
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_text);
		TextView t = (TextView)findViewById(R.id.edit_message);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        convertFramerateButton = (Button) findViewById(R.id.setFrameButton);
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
    public void setAdapterItems(long currentFrame, long maxFrame, int frameIndex)
    {
        double currentFrameRate = FrameBlock.frameRates[frameIndex];
        ArrayList<String> validFrameRates = new ArrayList<String>();
        for (int i = 0; i < FrameBlock.frameRates.length; i++)
        {
            double frameRate = FrameBlock.frameRates[i];
            double speedModifier = frameRate/currentFrameRate;
            if (currentFrame * speedModifier <= maxFrame)
                validFrameRates.add(0,FrameBlock.frameRateStrings[i]);
        }
        frameRateListView.setAdapter(new ArrayAdapter(this, R.layout.menu_encoding, validFrameRates));
        frameRateListView.invalidateViews();
    }
    void initMenu()
    {
        final Dialog framerateDialog = new Dialog(this);
        framerateDialog.setTitle("Choose Video Framerate");
        framerateDialog.setContentView(R.layout.menu_encoding_choice);
        frameRateListView = (ListView) framerateDialog.findViewById(R.id.choices);
        frameRateListView.setAdapter(new ArrayAdapter(this, R.layout.menu_encoding, FrameBlock.frameRateStrings));
        convertFramerateButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            framerateDialog.show();
            }
        });
        frameRateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                double frameRate = FrameBlock.frameRates[position];
                playerInstance.convertFramerate(frameRate, position);
                framerateDialog.hide();
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
