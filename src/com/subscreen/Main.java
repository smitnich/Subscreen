package com.subscreen;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;


public class Main {
	String rawData = null;
	/**
	 * @param args
	 */
	public static void main(TextView toEdit, Context context) {
		// TODO Auto-generated method stub
		String fileNames[] = {"Subtitles/test.txt", "Subtitles/test.srt","Subtitles/test.ass"};
		String fileName = fileNames[2];
		SubtitleFormat subFile = pickFormat(fileName);
		//srtFile.writeTo = toEdit;
		Typeface test_font = Typeface.createFromAsset(context.getResources().getAssets(),"DejaVuSans.ttf");
		toEdit.setTypeface(test_font); 
		final AndroidOutput outputTo = new AndroidOutput();
		outputTo.setTextView(toEdit);
		final ArrayList<TextBlock> blocks = subFile.readFile(fileName);
		new Thread(new Runnable() {
	        public void run() {
	            playSubtitles(blocks,outputTo);
	        }}).start();
		//playSubtitles(blocks,outputTo);
	}
	private static void playSubtitles(ArrayList<TextBlock> blocks, Output outputTo) {
		// TODO Auto-generated method stub
		if (blocks == null)
			return;
		Date rootTime = new Date();
		TextBlock.rootTime = rootTime.getTime()-blocks.get(0).startTime;
		while (!blocks.isEmpty())
		{
			TextBlock tmp = blocks.remove(0);
			tmp.FirstDelay();
			tmp.getText(outputTo);
			tmp.secondDelay();
			outputTo.clearText();
		}
	}
	private static SubtitleFormat pickFormat(String path)
	{
		String extension = path.substring(path.lastIndexOf('.')+1, path.length());
		if (extension.equalsIgnoreCase("srt"))
			return new SrtFormat();
		else if (extension.equalsIgnoreCase("txt"))
			return new AsdFormat();
		else if (extension.equalsIgnoreCase("ass"))
			return new ASSFormat();
		return null;
	}
}