package com.subscreen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.io.FileReader;
import java.util.concurrent.*;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.subscreen.Subtitles.ASSFormat;
import com.subscreen.Subtitles.MPLFormat;
import com.subscreen.Subtitles.MicroDVDFormat;
import com.subscreen.Subtitles.SMIFormat;
import com.subscreen.Subtitles.SrtFormat;
import com.subscreen.Subtitles.SubViewerTwoFormat;
import com.subscreen.Subtitles.SubtitleFormat;
import com.subscreen.Subtitles.TmpFormat;
import com.subscreen.Subtitles.VTTFormat;

import android.app.Activity;

public class SubtitlePlayer {
    long rootTime = -1;
    long offset = 0;
    boolean paused = false;
    boolean changeTextRequested = false;
    boolean playbackStarted = false;
    boolean isFrameBased = false;
    volatile int subCount = 0;
    Thread execThread = null;
    long pauseTime = -1;
    AndroidOutput outputTo = null;
    ArrayList<TextBlock> blocks = null;
    TextBlock text;
    ShowText parentActivity;
    Context context;
    String destCharset;
    String rootPath = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
	public void main(TextView toEdit, Context _context, String filePath, Activity activity) {
        context = _context;
        parentActivity = (ShowText) activity;
        SubtitleFormat subFile = pickFormat(filePath);
		Typeface test_font = Typeface.createFromAsset(context.getResources().getAssets(),"DejaVuSans.ttf");
		toEdit.setTypeface(test_font);
		outputTo = new AndroidOutput(activity,destCharset);
		outputTo.setTextView(toEdit);
        try {
            blocks = subFile.readFile(filePath);
        } catch (Exception e){
            parentActivity.displayBackMessage("Sorry, that doesn't seem to be a known subtitle format.","Sorry");
            //return;
            //e.printStackTrace();
        }
        initText();
        if (blocks.get(0).showFramerates())
        {
            parentActivity.convertFramerateButton.setEnabled(true);
            parentActivity.convertFramerateButton.setVisibility(View.VISIBLE);
            isFrameBased = true;
        }
        else
        {
            parentActivity.convertFramerateButton.setEnabled(false);
            parentActivity.convertFramerateButton.setVisibility(View.INVISIBLE);
        }
        pause();
	}
    //Initialize the text to the first line of dialog
    private void initText()
    {
        TextBlock firstBlock = blocks.get(0);
        firstBlock.addSyncMessage("Press play when this dialog begins:<br/>");
        firstBlock.getText(outputTo);
    }
    private void startThread()
    {
        execThread = new Thread(new Runnable() {
            public void run() {
                startSubtitles();
            }});
        execThread.start();
    }
    public void convertFramerate(double framerate)
    {

    }
    public void prevSubtitle()
    {
        if (!paused)
            pause();
        if (subCount <= 0)
            return;
        TextBlock prevBlock = blocks.get(--subCount);
        prevBlock.getText(outputTo);
        playbackStarted = false;
    }
    public void nextSubtitle()
    {
        if (!paused)
            pause();
        if (subCount+1 >= blocks.size())
            return;
        TextBlock nextBlock = blocks.get(++subCount);
        nextBlock.getText(outputTo);
        playbackStarted = false;
    }
    private void startSubtitles()
    {
        if (blocks == null)
            return;
        if (playbackStarted == false) {
            playbackStarted = true;
            Date rootDate = new Date();
            rootTime = rootDate.getTime();
            text = blocks.get(subCount);
            long firstTime = text.getStartTime();
            offset = -text.getStartTime();
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
        while (true) {
            try {
                if (blocks == null)
                    return;
                while (subCount < blocks.size()) {
                    text = blocks.get(subCount);
                    if (!changeTextRequested)
                        text.firstDelay();
                    else
                        changeTextRequested = false;
                    text.getText(outputTo);
                    text.secondDelay();
                    outputTo.clearText();
                    subCount++;
                }
                break;
            } catch (InterruptedException e) {
                if (!changeTextRequested)
                    pauseTime = new Date().getTime();
                else
                    continue;
                return;
            }
        }
        outputTo.outputText("Playback complete");
        try {
            Thread.sleep(30*1000);
        }
        //If we're interrupted, just go back as normal
        catch (InterruptedException e)
        {
        }
        parentActivity.returnToSelectScreen();
	}
    //Open the file, read the values, and convert them to positive binary values.
    //When opening a file, Java replaces the byte order mark that exists with the one that it
    //thinks should be there; this is not ideal so this stupid workaround is required in order
    //to detect it
    private String determineEncoding(String path) throws Exception {
        FileInputStream fis = new FileInputStream(path);
        byte[] tmpBuffer = new byte[5];
        int[] buffer = new int[5];
        fis.read(tmpBuffer);
        fis.close();
        for (int i = 0; i < 5; i++)
            buffer[i] = tmpBuffer[i] & 0xff;
        if (buffer[0] == 0xef && buffer[1] == 0xbb && buffer[2] == 0xbf)
            return "UTF-8";
        else if (buffer[0] == 0xfe && buffer[1] == 0xff)
            return "UTF-16BE";
        else if (buffer[0] == 0xff && buffer[1] == 0xfe)
            return "UTF-16LE";
        else if (buffer[0] == 0 && buffer[1] == 0 && buffer[2] == 0xfe && buffer[3] == 0xff)
            return "UTF_32";
        else if (buffer[0] == 0x2b && buffer[1] == 0x2f && buffer[2] == 0x76)
             return "US-ASCII";
        else
            return "ISO-8859-1";
    }
	private SubtitleFormat pickFormat(String path)
	{
        final int bufferLength = 128;
        FileReader fis = null;
        char[] buffer = new char[bufferLength];
        int i = 0;
        try {
            destCharset = determineEncoding(path);
            fis = new FileReader(path);
            fis.read(buffer,0,bufferLength);
            //Skip to the first actual text
            while (buffer[i] == '\r' || buffer[i] == '\n')
            {
                i++;
            }
            while (true) {
                //Convert to proper values
                switch (buffer[i]) {
                    case 'W':
                        return new VTTFormat(this);
                    case '{':
                        return new MicroDVDFormat(this);
                    case '[':
                        if (buffer[i+1] >= '0' && buffer[i+1] <= '9')
                            return new MPLFormat(this);
                        else {
                            while (buffer[i++] != '\n');
                            if (buffer[i] == '[')
                                return new SubViewerTwoFormat(this);
                            else
                                return new ASSFormat(this);
                        }
                        //Theoretically this could actually be a different format with the first text
                        //appearing 10 hours in, so double check just to be paranoid
                    case '1':
                        if (buffer[i+1] == '\r' || buffer[i+1] == '\n')
                            return new SrtFormat(this);
                        else
                            return new TmpFormat(this);
                    case '0':
                        return new TmpFormat(this);
                    case '<':
                        return new SMIFormat(this);
                    //Byte order mark, skip here
                    case 0xFFFD:
                    case 0xFFFE:
                    case 0xFEFF:
                        i++;
                        break;
                    default:
                        fis.close();
                        return null;
                }
            }
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
                //e.printStackTrace();
            }
        }
        return null;
    }
    public void pause()
    {
        long timeOffset = 0;
        if (!paused) {
            ShowText.setButton("▶");
            pauseTime = new Date().getTime();
            if (execThread != null)
                execThread.interrupt();
        }
        else {
            ShowText.setButton("▋▋");
            timeOffset = new Date().getTime() - pauseTime;
            offset += timeOffset;
            startThread();
        }
        paused = !paused;
    }
    public void setEncoding(String fileName)
    {
        outputTo.setDestCharset(fileName);
    }
    public long getOffset() {
        return offset;
    }
}