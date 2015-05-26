package com.subscreen.Subtitles;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.widget.TextView;

import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;


public class SrtFormat implements SubtitleFormat {

	public TextView writeTo;
    SubtitlePlayer playerInstance = null;
    public SrtFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }
	public ArrayList<TextBlock> readFile(InputStream data, String srcCharset)
	{
        try {
            ArrayList<TextBlock> blocks = new ArrayList<>();
			blocks.add(new TimeBlock(SubtitlePlayer.playString,0,-1,playerInstance));
			BufferedReader br = new BufferedReader(new InputStreamReader(data, srcCharset));
            readLines(br, blocks);
            return blocks;
        }
        catch (Exception e)
        {
            return null;
        }
	}
	public void readLines(BufferedReader in, ArrayList<TextBlock> blocks)
	{
		String buffer;
		int current = 1;
        String tmp;
		boolean lastLineWasEmpty;
		try {
			//Skip past any empty lines at the beginning of the file
			do {
				buffer = in.readLine();
			} while (buffer.length() == 0);
			while (true) {
				lastLineWasEmpty = false;
				do {
					tmp = in.readLine();
					if (tmp == null)
						break;
				} while(tmp.length() == 0);
				if (tmp == null)
					break;
				buffer = tmp.trim();
				String beginTimeString = buffer.substring(0,buffer.indexOf('-')).trim();
				//We need to check for a space after the second time string in order to avoid
				//reading in possible coordinate data following the times
				int beginIndex = buffer.lastIndexOf('>');
				while (buffer.charAt(++beginIndex) == ' ');
				int spaceIndex = buffer.indexOf(' ',beginIndex+1);
				if (spaceIndex == -1)
					spaceIndex = buffer.length();
				String endTimeString = buffer.substring(beginIndex,spaceIndex).trim();
				long beginTime = parseTimeStamp(beginTimeString);
				long endTime = parseTimeStamp(endTimeString);
				buffer = new String();
				while (true)
				{

					tmp = in.readLine();
					if (tmp == null)
						break;
					tmp = tmp.trim();
					if (isNumber(tmp) && lastLineWasEmpty)
						break;
					if (lastLineWasEmpty && buffer.length() > 0)
						buffer += "<br>";
					if (tmp.length() == 0) {
						lastLineWasEmpty = true;
						continue;
					}
                    if (tmp.length() > 0) {
						if (!lastLineWasEmpty && buffer.length() > 0)
							buffer += "<br>";
						lastLineWasEmpty = false;
						buffer += tmp;
					}
				}
				if (buffer.length() > 0)
					blocks.add(new TimeBlock(buffer, beginTime, endTime,playerInstance));
			}
		} catch (IOException e) {
		}
        catch (Exception e)
        {
            throw e;
        }
	}
	public boolean isNumber(String input) {
		//Don't count the empty string as a number
		if (input.length() == 0)
			return false;
		for (int i = 0; i < input.length(); i++)
		{
			if ((input.charAt(i) > '9' || input.charAt(i) < '0') && input.charAt(i) != '-')
				return false;
		}
		return true;
	}
	public int parseTimeStamp(String input)
	{
		if (input.length() == 1)
			return 0;
		//A timestamp should not be longer than this
		int maxLength = 12;
		int count = input.indexOf(':');
		int nextCount = -1;
		int hours = Integer.parseInt(input.substring(0,count));
		nextCount = input.indexOf(":",count+1);
		int minutes = Integer.parseInt(input.substring(count+1,nextCount));
		count = input.indexOf(':',nextCount);
		nextCount = input.indexOf(',',count);
		//If the comma character is missing, default to the typical time format:
		//00:00:00,000 by assuming that the nextCount index is the same as in this format
		if (nextCount == -1)
			nextCount = 8;
		int seconds = Integer.parseInt(input.substring(count+1,nextCount));
		//Don't use an input length greater than the max length, but do allow for one less than it
		//by using min
		int endIndex = Math.min(maxLength,input.length());
		int milliseconds;
		if (input.length() > 8) {
			milliseconds = Integer.parseInt(input.substring(nextCount + 1, endIndex));
			milliseconds *= Math.pow(10, ((nextCount + 1 + 3) - endIndex));
		}
		else {
			milliseconds = 0;
		}
		//If there were less than three digits of milliseconds, then we need to multiply by 10
		//to the power of the missing digits in order to convert to milliseconds
		return (hours*60*60*1000) + (minutes*60*1000) + (seconds*1000) + milliseconds;
	}

}
