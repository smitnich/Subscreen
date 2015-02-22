package com.subscreen;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.widget.TextView;

import com.subscreen.Subtitles.ASSFormat;
import com.subscreen.Subtitles.MPLFormat;
import com.subscreen.Subtitles.MicroDVDFormat;
import com.subscreen.Subtitles.SMIFormat;
import com.subscreen.Subtitles.SrtFormat;
import com.subscreen.Subtitles.SubViewerTwoFormat;
import com.subscreen.Subtitles.SubtitleFormat;
import com.subscreen.Subtitles.TmpFormat;

import android.app.Activity;

public class SubtitlePlayer {
    long rootTime = -1;
    long offset = 0;
    boolean paused = false;
    int subCount = -1;
    Thread execThread = null;
    long pauseTime = -1;
    AndroidOutput outputTo = null;
    ArrayList<TextBlock> blocks = null;
    TextBlock text;
    ShowText parentActivity;
    Context context;
    String rootPath = System.getenv("EXTERNAL_STORAGE") + "/" + "Subtitles/";
	public void main(TextView toEdit, Context _context, String fileName, Activity activity) {
        context = _context;
        parentActivity = (ShowText) activity;
        SubtitleFormat subFile = pickFormat(rootPath+fileName);
		Typeface test_font = Typeface.createFromAsset(context.getResources().getAssets(),"DejaVuSans.ttf");
		toEdit.setTypeface(test_font);
		outputTo = new AndroidOutput(activity);
		outputTo.setTextView(toEdit);
        try {
            blocks = subFile.readFile(rootPath + fileName);
        } catch (Exception e){
            parentActivity.displayBackMessage("Sorry, that doesn't seem to be a known subtitle format.","Sorry");
            //return;
            //e.printStackTrace();
        }
        startThread();
	}

    private void startThread()
    {
        execThread = new Thread(new Runnable() {
            public void run() {
                startSubtitles();
            }});
        execThread.start();
    }
    private void startSubtitles()
    {
        if (blocks == null)
            return;
        if (subCount == -1) {
            subCount = 0;
            Date rootDate = new Date();
            rootTime = rootDate.getTime();
            text = blocks.get(subCount);
            long firstTime = text.getStartTime();
            rootTime -= firstTime;
            text.getText(outputTo);
            try {
                text.secondDelay();
                outputTo.clearText();
                subCount++;
            } catch (InterruptedException e) {
                pauseTime = new Date().getTime();
                return;
            }
        }
        playSubtitles(blocks,outputTo);
    }
	private void playSubtitles(ArrayList<TextBlock> blocks, Output outputTo) {
        try {
            if (blocks == null)
                return;
            while (subCount < blocks.size()) {
                text = blocks.get(subCount);
                text.firstDelay();
                text.getText(outputTo);
                text.secondDelay();
                outputTo.clearText();
                subCount++;
            }
        } catch (InterruptedException e)
        {
            pauseTime = new Date().getTime();
            return;
        }
        outputTo.outputText("Playback complete");
	}
	private SubtitleFormat pickFormat(String path)
	{
        FileInputStream fis = null;
        byte[] buffer = new byte[1024];
        int count = 0;
        try {
            fis = new FileInputStream(path);
            fis.read(buffer);
            switch(buffer[0])
            {
                case '{':
                    return new MicroDVDFormat(this);
                case '[':
                    if (buffer[1] >= '0' && buffer[1] <= '9')
                        return new MPLFormat(this);
                    else
                    {
                        while (buffer[count++] != '\n');
                        if (buffer[count] == '[')
                            return new SubViewerTwoFormat(this);
                        else
                            return new ASSFormat(this);
                    }
                //Theoretically this could actually be a different format with the first text
                //appearing 10 hours in, so double check just to be paranoid
                case '1':
                    if (buffer[1] == '\r' || buffer[1] == '\n')
                        return new SrtFormat(this);
                    else
                    //FixMe
                        return new TmpFormat(this);
                case '0':
                    return new TmpFormat(this);
                case '<':
                    return new SMIFormat(this);
            }
            fis.close();
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
        finally
        {
            try {
                fis.close();
            }
            catch (Exception e)
            {
                //e.printStackTrace();
            }
        }
        return null;
    }
    public void pause()
    {
        long timeOffset = 0;
        if (!paused) {
            ShowText.setButton(">");
            pauseTime = new Date().getTime();
            execThread.interrupt();
        }
        else {
            ShowText.setButton("| |");
            timeOffset = new Date().getTime() - pauseTime;
            offset += timeOffset;
            startThread();
        }
        paused = !paused;
    }
    public long getOffset() {
        return offset;
    }
}