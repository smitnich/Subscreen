package com.subscreen.SubtitleFormats;
import java.io.IOException;
import java.util.ArrayList;

import com.subscreen.TextBlock;
import com.subscreen.UnicodeReader;


public class ASSFormat implements SubtitleFormat {

	//The string that begins any dialogue data
	static String dialogueString = "Dialogue:";
	//The number of commas before reaching the actual text to output
	static int lastCommaCount = 9;
	static String[] replace = {"\\N","{\\i0}","{\\i1}","{\\pub}"};
	static String[] replaceWith = {"\n","<i>","<i>",""};
	public ArrayList<TextBlock> readFile(String path)
	{
		String fullPath = System.getenv("EXTERNAL_STORAGE") + "/" + path;
		ArrayList<TextBlock> blocks = new ArrayList<TextBlock>();
		UnicodeReader br = new UnicodeReader(fullPath);
		readLines(br, blocks);
		return blocks;
	}
	public void readLines(UnicodeReader in, ArrayList<TextBlock> blocks)
	{
		int begin, end, i;
		long beginTime, endTime;
		int commasFound = 0;
		boolean matchComma = true;
		String buffer = new String();
		try {
			while (in.available() > 0)
			{	
				//The vast majority of data in ASS files is of no use to us; just
				//parse the text after 'Dialogue:'
				buffer =  new String(in.readLine()).trim();
				if (buffer.length() < dialogueString.length() || buffer.substring(0,dialogueString.length()).compareTo(dialogueString) != 0)
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
				begin = 0;
				commasFound = 0;
				matchComma = true;
				for (i = 0; i < buffer.length(); i++)
				{
					char tmp = buffer.charAt(i);
					System.out.println(tmp);
					tmp = tmp;
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
				blocks.add(new TextBlock(buffer, beginTime, endTime));
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
