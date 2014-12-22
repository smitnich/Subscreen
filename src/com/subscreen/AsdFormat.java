package com.subscreen;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.widget.TextView;

public class AsdFormat implements SubtitleFormat  {

	public TextView writeTo;
	public ArrayList<TextBlock> readFile(String path)
	{
		String tmp = System.getenv("EXTERNAL_STORAGE") + "/" + path;
		ArrayList<TextBlock> blocks = new ArrayList<TextBlock>();
		UnicodeReader input = null;
		String test = "Œwi¹t";
		try {
			input = new UnicodeReader(tmp);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			return null;
		}
		readLines(input, blocks);
		return blocks;
	}
	@SuppressLint("NewApi")
	void readLines(UnicodeReader in, ArrayList<TextBlock> blocks)
	{
		long time = 0;
		TextBlock oldBlock = null;
		char[] charBuffer = new char[1024];
		long startTime = -1;
		try {
			while (in.available() > 0)
			{	
				charBuffer = in.readLine();
				System.out.println(charBuffer);
				String buffer = new String(charBuffer);
				int idx = 0;
				for (int i = 0; i < 3; i++)
				{
					idx = buffer.indexOf(':', idx+1);
				}
				time = parseTimeStamp(buffer.substring(0,idx+1));
				//Cut out everything after the timestamp and add it as the text string
				String input = buffer.substring(idx+1);
				if (oldBlock != null)
				{
					oldBlock.endTime = time;
					blocks.add(oldBlock);
				}
				oldBlock = new TextBlock(input,time);
			}
			if (oldBlock != null)
			{
				oldBlock.endTime = time+5*1000;
				blocks.add(oldBlock);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int parseTimeStamp(String input)
	{
		int hours = Integer.parseInt(input.substring(0,2))*60*60;
		int minutes = Integer.parseInt(input.substring(3,5))*60;
		int seconds = Integer.parseInt(input.substring(6,8));
		//Convert to milliseconds
		return (hours+minutes+seconds)*1000;
	}
}
