package com.subscreen;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.io.FileReader;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
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
    volatile boolean threadKillRequested = false;
    volatile int subCount = 0;
    Thread execThread = null;
    long pauseTime = -1;
    long firstSubtitleStartTime = 0;
    AndroidOutput outputTo = null;
    ArrayList<TextBlock> blocks = null;
    TextBlock text;
    ShowText parentActivity;
    Context context;
    public String srcCharset;
    String rootPath = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
	public void main(TextView toEdit, Context _context, String filePath, Activity activity) {
        context = _context;
        parentActivity = (ShowText) activity;
        SubtitleFormat subFile = pickFormat(filePath);
        if (subFile == null){
            parentActivity.displayBackMessage(
                    context.getString(R.string.bad_format_message),context.getString(R.string.bad_format_title));
            return;
        }
        String playString = context.getString(R.string.begin_play);
		Typeface test_font = Typeface.createFromAsset(context.getResources().getAssets(),"DejaVuSans.ttf");
		toEdit.setTypeface(test_font);
		outputTo = new AndroidOutput(activity, srcCharset);
		outputTo.setTextView(toEdit);
        try {
            blocks = subFile.readFile(filePath, srcCharset);
            if (blocks.get(0).showFramerates()) {
                parentActivity.convertFramerateButton.setEnabled(true);
                parentActivity.convertFramerateButton.setVisibility(View.VISIBLE);
                isFrameBased = true;
                blocks.add(0, new FrameBlock(playString, 0, -1, this));
            } else {
                parentActivity.convertFramerateButton.setEnabled(false);
                parentActivity.convertFramerateButton.setVisibility(View.INVISIBLE);
                blocks.add(0, new TimeBlock(playString, 0, -1, this));
            }
        }
        catch (Exception e){
            parentActivity.displayBackMessage(
                    context.getText(R.string.bad_format_message).toString(),"Sorry");
            return;
        }
        initText();
        pause();
	}
    public double getCurrentFramerate() {
        return FrameBlock.currentFramerateMultiplier;
    }
    //Initialize the text to the first line of dialog
    private void initText() {
        TextBlock firstBlock = blocks.get(0);
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
    //Find the subtitle that will be playing at this frame
    public void findFrame(int frame)
    {
        int start = 0;
        int end = blocks.size()-1;
        int mid = (start+end)/2;
        FrameBlock block;
        block = (FrameBlock) blocks.get(blocks.size()-1);
        if (frame > block.endFrame)
            return;
        while (Math.abs(start-end) > 1) {
            block = (FrameBlock) blocks.get(mid);
            if (frame >= block.startFrame) {
                if (frame <= block.endFrame)
                    break;
                start = mid;
            } else {
                end = mid;
            }
            mid = (int) Math.ceil((start+end)/2.0);
        }
        subCount = mid;
        block = (FrameBlock) blocks.get(mid);
        block.getText(outputTo);
        if (!paused) {
            execThread.interrupt();
            startThread();
        }
    }
    public void convertFramerate(double framerate, int index)
    {
        try
        {
            FrameBlock block = (FrameBlock) blocks.get(subCount);
            int result = (int) block.convertFramerate(framerate, index);
            block.setFrameRate(index);
            findFrame(result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public long getLastFrame() {
        FrameBlock tmp = (FrameBlock) blocks.get(blocks.size()-1);
        return tmp.endFrame;
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
            firstSubtitleStartTime = text.getStartTime();
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
                if (threadKillRequested) {
                    threadKillRequested = false;
                    return;
                }
                if (!changeTextRequested)
                    pauseTime = new Date().getTime();
                else
                    continue;
                return;
            }
        }
        outputTo.outputText(context.getString(R.string.finish_play));
        try {
            Thread.sleep(30 * 1000);
        }
        //If we're interrupted, just go back as normal
        catch (InterruptedException e) {
        }
        parentActivity.returnToSelectScreen();
    }
    private String determineEncoding(String path) throws Exception {
        PushbackInputStream fis = new PushbackInputStream(new FileInputStream(path), 4);
        byte[] tmpBuffer = new byte[5];
        int[] buffer = new int[5];
        fis.read(tmpBuffer,0,4);
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
	public SubtitleFormat pickFormat(String path)
	{
        final int bufferLength = 128;
        InputStreamReader fis = null;
        char[] buffer = new char[bufferLength];
        int i = 0;
        try {
            srcCharset = determineEncoding(path);
            fis = new InputStreamReader(new FileInputStream(path), srcCharset);
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
                    //Unfortunately, SRT files sometimes begin with numbers other than 1; if it's
                    // followed by a newline or line feed then it should be an SRT file, otherwise
                    // it is a TMP file
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        //SRT files also occasionally begin with negative numbers...
                    case '-':
                        if (buffer.length > i+2 && buffer[i+2] == ':')
                            return new TmpFormat(this);
                        else
                            return new SrtFormat(this);
                    case '<':
                        return new SMIFormat(this);
                    //Byte order mark, skip here
                    case 0xFFFD:
                    case 0xFFFE:
                    case 0xFEFF:
                    //Also skip empty spaces
                    case ' ':
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
                return null;
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
    public long getOffset() {
        return offset;
    }
    public void cleanup() {
        try {
            if (execThread != null) {
                threadKillRequested = true;
                execThread.interrupt();
                execThread.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}