package com.subscreen.Subtitles;
import java.io.IOException;
import java.util.ArrayList;
import android.widget.TextView;

import com.subscreen.FileReaderHelper;
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
	public ArrayList<TextBlock> readFile(String path)
	{
		ArrayList<TextBlock> blocks = new ArrayList<>();
		FileReaderHelper br = new FileReaderHelper(path);
		readLines(br, blocks);
		return blocks;
	}
	public void readLines(FileReaderHelper in, ArrayList<TextBlock> blocks)
	{
		String buffer = new String();
		int current = 1;
		try {
			while (in.available() > 0)
			{	
				//Read the block number and throw a warning if it is not the expected one
				buffer =  new String(in.readLine()).trim();
				String tmp;
				if (Integer.parseInt(buffer) != current++)
				{
					System.out.println("Warning, block " + current + " was not found.");
				}
				buffer = new String(in.readLine()).trim();
				String beginTimeString = buffer.substring(0,buffer.indexOf('-')).trim();
				String endTimeString = buffer.substring(buffer.lastIndexOf('>')+2,buffer.length()).trim();
				long beginTime = parseTimeStamp(beginTimeString);
				long endTime = parseTimeStamp(endTimeString);
				tmp = buffer;
				buffer = new String();
				while (tmp.length() > 0)
				{
					tmp = new String(in.readLine()).trim();
					buffer += tmp + "\n";
				}
				blocks.add(new TimeBlock(buffer, beginTime, endTime,playerInstance));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        catch (Exception e)
        {
            throw e;
        }
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
