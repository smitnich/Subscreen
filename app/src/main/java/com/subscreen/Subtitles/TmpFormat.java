package com.subscreen.Subtitles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.TextView;

import com.subscreen.FileReaderHelper;
import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

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
		FileReaderHelper input = null;
		try {
			input = new FileReaderHelper(path,"UTF-8");
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			return null;
		}
		readLines(input, blocks);
		return blocks;
	}
	void readLines(FileReaderHelper in, ArrayList<TextBlock> blocks)
	{
        //(:|=|,)(\d).")
        Pattern p = Pattern.compile("(\\d*):(\\d*):(\\d*)(?::|=)(.*)");
        String[] replace = {"|"};
        String[] replaceWith = {"\n"};
		long time = 0;
		TimeBlock oldBlock = null;
		char[] charBuffer = new char[1024];
		long startTime = -1;
        Matcher m;
        try {
			while (in.available() > 0)
			{	
				charBuffer = in.readLine();
				System.out.println(charBuffer);
                String buffer = new String(charBuffer).trim();
                if (buffer.length() == 0)
                    break;
                m = p.matcher(buffer);
                if (!m.find())
                    continue;
                long hours = Integer.parseInt(m.group(1));
                long minutes =  Integer.parseInt(m.group(2));
                long seconds = Integer.parseInt(m.group(3));
                time = (hours*60*60+minutes*60+seconds)*1000;
				//Cut out everything after the timestamp and add it as the text string
				String input = m.group(4);
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
        catch (Exception e)
        {
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
