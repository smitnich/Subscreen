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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShowText extends FragmentActivity {
    static Button pauseButton;
    static Button backButton;
    static Button nextButton;
    static Button prevButton;
    static Button convertFramerateButton;
    SubtitlePlayer playerInstance = null;
    ListView frameRateListView;
    ArrayList<String> validFrameRates;
    ArrayList<Integer> indices;
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
        String zipFileName = b.getString("zipFileName");
        BufferedInputStream fileData = new BufferedInputStream(FileHelper.readFile(fileName,zipFileName));
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
        playerInstance.main(t, this.getApplicationContext(), fileData, this);
	}
    void displayBackMessage(String message, String title)
    {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(this.getString(R.string.exit_text), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        returnToSelectScreen();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
    public void returnToSelectScreen(){
        Intent intent = new Intent(ShowText.this, SelectFile.class);
        playerInstance.cleanup();
        startActivity(intent);
        finish();
    }
    void initMenu()
    {
        validFrameRates = new ArrayList<String>(Arrays.asList(FrameBlock.frameRateStrings));
        final Dialog framerateDialog = new Dialog(this);
        framerateDialog.setTitle(this.getString(R.string.framerate_dialog_title));
        framerateDialog.setContentView(R.layout.menu_encoding_choice);
        frameRateListView = (ListView) framerateDialog.findViewById(R.id.choices);
        frameRateListView.setAdapter(new ArrayAdapter(this, R.layout.menu_encoding, validFrameRates));
        convertFramerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFramerates(framerateDialog);
                framerateDialog.show();
            }
        });
        frameRateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Take the value from the indices since otherwise positions will be off due
                double frameRate = FrameBlock.frameRates[indices.get(position)];
                playerInstance.convertFramerate(frameRate, indices.get(position));
                framerateDialog.hide();
            }
        });
    }
    public void chooseFramerates(Dialog framerateDialog) {
        FrameBlock currentBlock = (FrameBlock) playerInstance.blocks.get(playerInstance.subCount);
        double currentModifier = playerInstance.getCurrentFramerate();
        indices = new ArrayList<Integer>();
        long maxFrame = playerInstance.getLastFrame();
        validFrameRates.clear();
        for (int i = 0; i < FrameBlock.frameRateMultipliers.length; i++)
        {
            if (FrameBlock.frameRateMultipliers[i] == currentModifier)
                continue;
            long currentFrame = currentBlock.convertFramerate(FrameBlock.frameRateMultipliers[i],i);
            if (currentFrame > maxFrame)
                continue;
            validFrameRates.add(FrameBlock.frameRateStrings[i]);
            //Since not all framerates will be added to the list of available framerates, we need
            //to make sure that we keep track of the true index by storing the corresponding index
            //of each frame rate
            indices.add(i);
        }
        frameRateListView.setAdapter(new ArrayAdapter(this, R.layout.menu_encoding, validFrameRates));
        frameRateListView.invalidateViews();
    }
    public void pause(View v)
    {
        playerInstance.pause();
    }
    public static void setButton(String input){
        pauseButton.setText(input);
    }

}
