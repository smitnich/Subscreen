package com.subscreen;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import com.subscreen.Subtitles.ASSFormat;
import com.subscreen.Subtitles.MPLFormat;
import com.subscreen.Subtitles.SMIFormat;
import com.subscreen.Subtitles.SUBFormat;
import com.subscreen.Subtitles.SrtFormat;
import com.subscreen.Subtitles.SubtitleFormat;
import com.subscreen.Subtitles.TXTFormat;
import com.subscreen.Subtitles.TmpFormat;
import android.widget.Button;
import android.app.Activity;

public class Main {
    public static long rootTime = -1;
    static long offset = 0;
    static boolean paused = false;
    static int subCount = -1;
    static Thread execThread = null;
    static long pauseTime = -1;
    static AndroidOutput outputTo = null;
    static ArrayList<TextBlock> blocks = null;
    static TextBlock text;
    static String rootPath = System.getenv("EXTERNAL_STORAGE") + "/" + "Subtitles/";
	public static void main(TextView toEdit, Context context, String fileName, Activity activity) {
		SubtitleFormat subFile = pickFormat(rootPath+fileName);
		Typeface test_font = Typeface.createFromAsset(context.getResources().getAssets(),"DejaVuSans.ttf");
		toEdit.setTypeface(test_font);
		outputTo = new AndroidOutput(activity);
		outputTo.setTextView(toEdit);
		blocks = subFile.readFile(rootPath+fileName);
        startThread();
	}
    private static void startThread()
    {
        execThread = new Thread(new Runnable() {
            public void run() {
                startSubtitles();
            }});
        execThread.start();
    }
    private static void startSubtitles()
    {
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
	private static void playSubtitles(ArrayList<TextBlock> blocks, Output outputTo) {
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
	}
	private static SubtitleFormat pickFormat(String path)
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
                    return new SUBFormat();
                case '[':
                    if (buffer[1] >= '0' && buffer[1] <= '9')
                        return new MPLFormat();
                    else
                    {
                        while (buffer[count++] != '\n');
                        if (buffer[count] == '[')
                            return new TXTFormat();
                        else
                            return new ASSFormat();
                    }
                //Theoretically this could actually be a different format with the first text
                //appearing 10 hours in, so double check just to be paranoid
                case '1':
                    if (buffer[1] == '\r' || buffer[1] == '\n')
                        return new SrtFormat();
                    else
                    //FixMe
                        return new TmpFormat();
                case '0':
                    return new TmpFormat();
                case '<':
                    return new SMIFormat();
            }
            fis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally
        {
            try {
                fis.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static void pause()
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
    public static long getOffset() {
        return offset;
    }
}