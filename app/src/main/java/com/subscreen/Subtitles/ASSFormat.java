package com.subscreen.Subtitles;
import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;
import com.subscreen.UnicodeReader;

import java.io.IOException;
import java.util.ArrayList;


public class ASSFormat implements SubtitleFormat {

	//The string that begins any dialogue data
	//The number of commas before reaching the actual text to output
	static int lastCommaCount = 9;
	static String[] replace = {"\\N","{\\i0}","{\\i1}","{\\pub}"};
	static String[] replaceWith = {"\n","<i>","<i>",""};
    SubtitlePlayer playerInstance = null;
    public ASSFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }
	public ArrayList<TextBlock> readFile(String path)
	{
		ArrayList<TextBlock> blocks = new ArrayList<>();
		UnicodeReader br = new UnicodeReader(path);
		readLines(br, blocks);
		return blocks;
	}
	public void readLines(UnicodeReader in, ArrayList<TextBlock> blocks)
	{
        char[] startTag = "[Events]".toCharArray();
		int begin, end, i;
		long beginTime, endTime;
		int commasFound = 0;
		boolean matchComma = true;
		String buffer = new String();
		try {
            //Skip until the Events tag for now
            while (in.available() > 0)
            {
                char[] tmp = in.readLine();
                for (i = 0; i < startTag.length; i++) {
                    if (tmp[i] != startTag[i])
                        break;
                }
                if (i == startTag.length)
                    break;
            }
            //We want to ignore the format text for now
            in.readLine();
			while (in.available() > 0)
			{	
				//The vast majority of data in ASS files is of no use to us; just
				//parse the text after 'Dialogue:'
				buffer =  new String(in.readLine()).trim();
				if (buffer.length() == 0)
					continue;
				//Find the first and last commas and take the substring between them
				begin = buffer.indexOf(',')+1;
				end = buffer.indexOf(',',begin);
				//If we didn't find a comma just move to the next line
				if (begin == 0 || end == -1)
					continue;
				String beginTimeString = buffer.substring(begin,end);
				//Move forward to the next time stamp
				begin = buffer.indexOf(',',end)+1;
				end = buffer.indexOf(',',begin);
				if (begin == 0 || end == -1)
					continue;
				String endTimeString = buffer.substring(begin,end);
				beginTime = parseTimeStamp(beginTimeString);
				endTime = parseTimeStamp(endTimeString);
				commasFound = 0;
				matchComma = true;
				for (i = 0; i < buffer.length(); i++)
				{
					char tmp = buffer.charAt(i);
					if (buffer.charAt(i) == ',' && matchComma)
						commasFound++;
					if (commasFound >= lastCommaCount)
						break;
				}
				//Skip any format specifiers at the beginning of the line
				if (buffer.charAt(i+1) == '{')
				{
					while (buffer.charAt(++i) != '}');
				}
				buffer = buffer.substring(i+1);
				for (i = 0; i < replace.length; i++)
				{
					buffer = buffer.replace(replace[i], replaceWith[i]);
				}
				blocks.add(new TimeBlock(buffer, beginTime, endTime, playerInstance));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public int parseTimeStamp(String input)
	{
		int count = input.indexOf(':');
		int nextCount = -1;
		int hours = Integer.parseInt(input.substring(0,count));
		nextCount = input.indexOf(":",count+1);
		int minutes = Integer.parseInt(input.substring(count+1,nextCount));
		count = nextCount+1;
		nextCount = input.length();
		float seconds = Float.parseFloat(input.substring(count,nextCount));
		return (hours*60*60*1000) + (minutes*60*1000) + ((int) (seconds*1000));
	}
}
