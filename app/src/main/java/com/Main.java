package com.subscreen;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import com.subscreen.SubtitleFormats.*;

public class Main {
	String rawData = null;
	public static void main(TextView toEdit, Context context) {
		String fileNames[] = {"Subtitles/test.txt", "Subtitles/test.srt","Subtitles/test.ass","Subtitles/test.smi", "Subtitles/testMPL.txt", "Subtitles/testTXT.srt", "Subtitles/testSUB.txt"};
		String fileName = fileNames[6];
		SubtitleFormat subFile = pickFormat(fileName);
		Typeface test_font = Typeface.createFromAsset(context.getResources().getAssets(),"DejaVuSans.ttf");
		toEdit.setTypeface(test_font); 
		final AndroidOutput outputTo = new AndroidOutput();
		outputTo.setTextView(toEdit);
		final ArrayList<TextBlock> blocks = subFile.readFile(fileName);
		new Thread(new Runnable() {
	        public void run() {
	            playSubtitles(blocks,outputTo);
	        }}).start();
	}
	private static void playSubtitles(ArrayList<TextBlock> blocks, Output outputTo) {
		if (blocks == null)
			return;
		Date rootTime = new Date();
		TextBlock.rootTime = rootTime.getTime();//-blocks.get(0).startTime;
        TextBlock tmp;
        while (!blocks.isEmpty())
		{
            tmp = blocks.remove(0);
            tmp.FirstDelay();
			tmp.getText(outputTo);
			tmp.secondDelay();
			outputTo.clearText();
		}
	}
	private static SubtitleFormat pickFormat(String path)
	{
        path = System.getenv("EXTERNAL_STORAGE") + "/" + path;
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