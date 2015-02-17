package com.subscreen.Subtitles;

import java.io.IOException;
import java.util.ArrayList;

import android.widget.TextView;

import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;
import com.subscreen.UnicodeReader;

public class TmpFormat implements SubtitleFormat {

	public TextView writeTo;
    public SubtitlePlayer playerInstance = null;
    public TmpFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }
	public ArrayList<TextBlock> readFile(String path)
	{
		ArrayList<TextBlock> blocks = new ArrayList<>();
		UnicodeReader input = null;
		try {
			input = new UnicodeReader(path);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			return null;
		}
		readLines(input, blocks);
		return blocks;
	}
	void readLines(UnicodeReader in, ArrayList<TextBlock> blocks)
	{
        String[] replace = {"|"};
        String[] replaceWith = {"\n"};
		long time = 0;
		TimeBlock oldBlock = null;
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
                for (int i = 0; i < replace.length; i++)
                {
                    input = input.replace(replace[i],replaceWith[i]);
                }
				oldBlock = new TimeBlock(input,time,playerInstance);
			}
			if (oldBlock != null)
			{
				oldBlock.endTime = time+5*1000;
				blocks.add(oldBlock);
			}
		} catch (IOException e) {
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
