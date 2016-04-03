package com.subscreen;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.subscreen.Subtitles.ASSFormat;
import com.subscreen.Subtitles.MPLFormat;
import com.subscreen.Subtitles.MicroDVDFormat;
import com.subscreen.Subtitles.SMIFormat;
import com.subscreen.Subtitles.SrtFormat;
import com.subscreen.Subtitles.SubViewerTwoFormat;
import com.subscreen.Subtitles.SubtitleFormat;
import com.subscreen.Subtitles.TmpFormat;
import com.subscreen.Subtitles.TutorialFormat;
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
    boolean loaded = false;
    public static String playString;
    //SMI allows for multiple languages in one file; check if the format is SMI and if so allow
    //for selecting languages
    public SMIFormat smiSub = null;
    public ArrayList<String> languages = null;
    public String fileName;
    private boolean resumed = false;
	public void main(TextView toEdit, Context _context, BufferedInputStream fileData, Activity activity, String encoding, String _fileName) {
        context = _context;
        fileName = _fileName;
        playString = context.getString(R.string.begin_play);
        parentActivity = (ShowText) activity;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.screenBrightness = 0.1f;
        activity.getWindow().setAttributes(params);
        SubtitleFormat subFile = pickFormat(fileData, encoding);
        if (subFile == null){
            parentActivity.displayBackMessage(
                    context.getString(R.string.bad_format_message), context.getString(R.string.bad_format_title));
            return;
        }
		Typeface test_font = Typeface.createFromAsset(context.getResources().getAssets(),"DejaVuSans.ttf");
		toEdit.setTypeface(test_font);
		outputTo = new AndroidOutput(activity,context.getResources().getDimension(R.dimen.activity_text_size));
		outputTo.setTextView(toEdit);
        try {
            blocks = subFile.readFile(fileData, srcCharset);
            if (blocks.get(0).showFramerates()) {
                parentActivity.convertFramerateButton.setEnabled(true);
                parentActivity.convertFramerateButton.setVisibility(View.VISIBLE);
                isFrameBased = true;
            } else {
                parentActivity.convertFramerateButton.setEnabled(false);
                parentActivity.convertFramerateButton.setVisibility(View.INVISIBLE);
            }
        }
        catch (Exception e){
            parentActivity.displayBackMessage(
                    context.getText(R.string.bad_format_message).toString(),"Sorry");
            return;
        }
        if (smiSub != null) {
            languages = smiSub.getAvailableLanguages();
        }
        if (smiSub == null || languages.size() <= 1) {
            parentActivity.languageButton.setVisibility(View.GONE);
        }
        parentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // Check if this was already loaded beforehand
        initText();
        pause();
        loaded = true;
    }
    public void resume(TextView toEdit, Context _context,
                       BufferedInputStream fileData, Activity activity, String encoding) {
        context = _context;
        playString = context.getString(R.string.begin_play);
        parentActivity = (ShowText) activity;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.screenBrightness = 0.1f;
        activity.getWindow().setAttributes(params);
        SubtitleFormat subFile = pickFormat(fileData, encoding);
        if (subFile == null){
            parentActivity.displayBackMessage(
                    context.getString(R.string.bad_format_message), context.getString(R.string.bad_format_title));
            return;
        }
        Typeface test_font = Typeface.createFromAsset(context.getResources().getAssets(),"DejaVuSans.ttf");
        toEdit.setTypeface(test_font);
        outputTo = new AndroidOutput(activity,context.getResources().getDimension(R.dimen.activity_text_size));
        outputTo.setTextView(toEdit);
        try {
            blocks = subFile.readFile(fileData, srcCharset);
            if (blocks.get(0).showFramerates()) {
                parentActivity.convertFramerateButton.setEnabled(true);
                parentActivity.convertFramerateButton.setVisibility(View.VISIBLE);
                isFrameBased = true;
            } else {
                parentActivity.convertFramerateButton.setEnabled(false);
                parentActivity.convertFramerateButton.setVisibility(View.INVISIBLE);
            }
        }
        catch (Exception e){
            parentActivity.displayBackMessage(
                    context.getText(R.string.bad_format_message).toString(),"Sorry");
            return;
        }
        if (smiSub != null) {
            languages = smiSub.getAvailableLanguages();
        }
        if (smiSub == null || languages.size() <= 1) {
            parentActivity.languageButton.setVisibility(View.GONE);
        }
        parentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        loaded = true;
        paused = true;
        resumed = true;
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
    public void startThread()
    {
        execThread = new Thread(new Runnable() {
            public void run() {
                startSubtitles();
            }});
        execThread.start();
    }
    //Find the subtitle that will be playing at this frame
    public void findTime(int time)
    {
        int start = 0;
        int end = blocks.size()-1;
        int mid = (start+end)/2;
        TextBlock block =  blocks.get(blocks.size()-1);
        if (time > block.getEndValue() && block.getEndValue() != -1)
            return;
        while (Math.abs(start-end) > 1) {
            block = blocks.get(mid);
            if (time >= block.getStartValue()) {
                if (time <= block.getEndValue())
                    break;
                start = mid;
            } else {
                end = mid;
            }
            mid = (int) Math.ceil((start+end)/2.0);
        }
        subCount = mid;
        block = blocks.get(mid);
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
            int result = (int) block.checkFramerate(framerate, index);
            FrameBlock.setFrameRate(index);
            if (playbackStarted == true)
                findTime(result);
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
        parentActivity.updateButtons(subCount,blocks.size());
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
        parentActivity.updateButtons(subCount,blocks.size());
    }
    private void startSubtitles()
    {
        if (blocks == null)
            return;
        final int size = blocks.size();
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                parentActivity.updateButtons(subCount, size);
            }
        });
        if (!playbackStarted) {
            outputTo.out.setTextSize(outputTo.textSize);
            playbackStarted = true;
            if (!resumed) {
                Date rootDate = new Date();
                rootTime = rootDate.getTime();
            }
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
        playSubtitles(blocks, outputTo);
    }
    //If using SMI files, multiple languages can be included in one file
    //This swaps to the language stored in the array with index of id
    public void switchLanguage(long id) {
        TimeBlock tmpBlock = (TimeBlock) blocks.get(subCount);
        int time = (int) tmpBlock.getStartTime();
        blocks = smiSub.getLanguage(id);
        if (subCount > 0)
            findTime(time);
    }
    //The loop used to play subtitles, incrementing subcount throughout until we reach
    //the last block
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
        final int size = blocks.size();
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                parentActivity.updateButtons(subCount, size);
            }
        });
        outputTo.outputText(context.getString(R.string.finish_play));
        try {
            Thread.sleep(30 * 1000);
        }
        //If we're interrupted, just go back as normal
        catch (InterruptedException e) {
        }
        parentActivity.returnToSelectScreen();
    }
	public SubtitleFormat pickFormat(BufferedInputStream fileData, String encoding)
	{
        String tutorialId = "SUBSCREEN_TUTORIAL";
        final int bufferLength = 128;
        InputStreamReader fis = null;
        fileData.mark(bufferLength + 1);
        srcCharset = encoding;
        char[] buffer = new char[bufferLength];
        int i = 0;
        try {
            fis = new InputStreamReader(fileData, encoding);
            fis.read(buffer,0,bufferLength);
            fileData.reset();
            //Skip to the first actual text
            while (buffer[i] == '\r' || buffer[i] == '\n')
            {
                i++;
            }
            while (true) {
                //Convert to proper values
                switch (buffer[i]) {
                    case 'S':
                        if (new String(buffer).substring(0,tutorialId.length()).compareTo(tutorialId) == 0)
                            return new TutorialFormat(this);
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
                        if (buffer.length > i+2 && (buffer[i+1] == ':' || buffer[i+2] == ':'))
                            return new TmpFormat(this);
                        else
                            return new SrtFormat(this);
                    case '<':
                        smiSub = new SMIFormat(this);
                        return smiSub;
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
        }
    }
}