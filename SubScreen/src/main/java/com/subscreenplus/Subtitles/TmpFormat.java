package com.subscreenplus.Subtitles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.subscreenplus.SubtitlePlayer;
import com.subscreenplus.TextBlock;
import com.subscreenplus.TimeBlock;

public class TmpFormat implements SubtitleFormat {

    public SubtitlePlayer playerInstance = null;
    public TmpFormat(SubtitlePlayer tmpPlayer)
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
	void readLines(BufferedReader in, ArrayList<TextBlock> blocks)
	{
        Pattern p = Pattern.compile("(\\d*):(\\d*):(\\d*)(?::|=)(.*)");
        String[] replace = {"|/","|"};
        String[] replaceWith = {"<br><i>","<br>"};
		long time = 0;
		TimeBlock oldBlock = null;
		String buffer;
        Matcher m;
        try {
			while (true)
			{	
				buffer = in.readLine();
				if (buffer == null)
					break;
                buffer = buffer.trim();
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
				String input = m.group(4).trim();
				if (oldBlock != null)
				{
				    oldBlock.endTime = time;
					blocks.add(oldBlock);
				}
                for (int i = 0; i < replace.length; i++)
                {
                    input = input.replace(replace[i],replaceWith[i]);
                }
				//MicroDVD format and TMP format share many of the same modifiers:
				//so use its string parsing function here
				input = MicroDVDFormat.buildOptions(input);
				if (input.length() > 0)
					oldBlock = new TimeBlock(input,time,playerInstance);
			}
			if (oldBlock != null)
			{
				oldBlock.endTime = time+5*1000;
				blocks.add(oldBlock);
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
        catch (Exception e)
        {
            //e.printStackTrace();
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
