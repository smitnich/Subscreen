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
import android.app.Activity;

public class Main {
    public static long rootTime = -1;
	String rawData = null;
    static String rootPath = System.getenv("EXTERNAL_STORAGE") + "/" + "Subtitles/";
	public static void main(TextView toEdit, Context context, String fileName, Activity activity) {
		SubtitleFormat subFile = pickFormat(rootPath+fileName);
		Typeface test_font = Typeface.createFromAsset(context.getResources().getAssets(),"DejaVuSans.ttf");
		toEdit.setTypeface(test_font);
        //final Output outputTo = new TextOutput();
		final AndroidOutput outputTo = new AndroidOutput(activity);
		outputTo.setTextView(toEdit);
		final ArrayList<TextBlock> blocks = subFile.readFile(rootPath+fileName);
		new Thread(new Runnable() {
	        public void run() {
	            playSubtitles(blocks,outputTo);
	        }}).start();
	}
	private static void playSubtitles(ArrayList<TextBlock> blocks, Output outputTo) {
		if (blocks == null)
			return;
        int i = 0;
		Date rootDate = new Date();
		rootTime = rootDate.getTime();//-blocks.get(0).startTime;
        TextBlock tmp;
        tmp = blocks.get(i++);
        long firstTime = tmp.getStartTime();
        rootTime -= firstTime;
        tmp.getText(outputTo);
        tmp.secondDelay();
        outputTo.clearText();
        while (i < blocks.size())
		{
            tmp = blocks.get(i++);
            tmp.firstDelay();
			tmp.getText(outputTo);
			tmp.secondDelay();
			outputTo.clearText();
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
}