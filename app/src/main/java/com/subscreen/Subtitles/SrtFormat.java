package com.subscreen.Subtitles;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
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
	public ArrayList<TextBlock> readFile(String path, String srcCharset)
	{
        try {
            ArrayList<TextBlock> blocks = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), srcCharset));
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
		try {
			in.readLine();
			while (true)
			{	
				tmp = in.readLine();
				if (tmp == null)
					break;
				buffer = tmp.trim();
				String beginTimeString = buffer.substring(0,buffer.indexOf('-')).trim();
				//We need to check for a space after the second time string in order to avoid
				//reading in possible coordinate data following the times
				int spaceIndex = buffer.indexOf(' ',buffer.lastIndexOf('>')+2);
				if (spaceIndex == -1)
					spaceIndex = buffer.length();
				String endTimeString = buffer.substring(buffer.lastIndexOf('>')+2,spaceIndex).trim();
				long beginTime = parseTimeStamp(beginTimeString);
				long endTime = parseTimeStamp(endTimeString);
				tmp = buffer;
				buffer = new String();
				while (true)
				{
					tmp = in.readLine();
					if (tmp == null)
						break;
					if (isNumber(tmp))
						break;
					if (tmp.length() == 0)
						continue;
					tmp = tmp.trim();
                    if (tmp.length() > 0)
					    buffer += tmp + "<br>";
				}
				blocks.add(new TimeBlock(buffer, beginTime, endTime,playerInstance));
			}
		} catch (IOException e) {
			e.printStackTrace();
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
			if (input.charAt(i) > '9' || input.charAt(i) < '0')
				return false;
		}
		return true;
	}
	public int parseTimeStamp(String input)
	{
		int count = input.indexOf(':');
		int nextCount = -1;
		int hours = Integer.parseInt(input.substring(0,count));
		nextCount = input.indexOf(":",count+1);
		int minutes = Integer.parseInt(input.substring(count+1,nextCount));
		count = input.indexOf(':',nextCount);
		nextCount = input.indexOf(',',count);
		int seconds = Integer.parseInt(input.substring(count+1,nextCount));
		int milliseconds = Integer.parseInt(input.substring(nextCount+1,input.length()));
		return (hours*60*60*1000) + (minutes*60*1000) + (seconds*1000) + milliseconds;
	}

}
