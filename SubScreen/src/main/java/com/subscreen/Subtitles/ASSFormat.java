package com.subscreen.Subtitles;
import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class ASSFormat implements SubtitleFormat {

	//The string that begins any dialogue data
	//The number of commas before reaching the actual text to output
	final static int lastCommaCount = 9;
	final static String[] replaceTags = {"i0","i1","b0","b1","s0","s1"};
    final static String[] replaceTagsWith = {"</i>","<i>","</b>","<b>","</strike>","<strike>"};
    final static String[] replaceText = {"\\N",};
	final static String[] replaceTextWith = {"<br>","<i>","<i>",""};
    SubtitlePlayer playerInstance = null;
    public ASSFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }
	public ArrayList<TextBlock> readFile(InputStream path, String srcCharset)
	{
        try {
            ArrayList<TextBlock> blocks = new ArrayList<>();
            blocks.add(new TimeBlock(SubtitlePlayer.playString,0,-1,playerInstance));
            BufferedReader br = new BufferedReader(new InputStreamReader(path, srcCharset));
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
        char[] startTag = "[Events]".toCharArray();
		int begin, end, i;
		long beginTime, endTime;
		int commasFound = 0;
		boolean matchComma = true;
        double multiplier = 1;
		String buffer;
		try {
            //Skip until the Events tag for now
            while (true)
            {
                String tmp = in.readLine();
                if (tmp == null)
                    return;
                if (tmp.length() > 7 && tmp.substring(0,6).compareTo("Timer:") == 0)
                {
                    String percentage = tmp.substring(7);
                    percentage = percentage.replace(",",".");
                    multiplier = Float.parseFloat(percentage)/100.0;
                }
                if (tmp.length() < startTag.length)
                    continue;
                for (i = 0; i < startTag.length; i++) {
                    if (tmp.charAt(i) != startTag[i])
                        break;
                }
                if (i == startTag.length)
                    break;
            }
            //We want to ignore the format text for now
            in.readLine();
			while (true)
			{	
				//The vast majority of data in ASS files is of no use to us; just
				//parse the text after 'Dialogue:'
                buffer = in.readLine();
                if (buffer == null)
                    break;
				buffer =  buffer.trim();
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
				beginTime = (long) (parseTimeStamp(beginTimeString)*multiplier);
				endTime = (long) (parseTimeStamp(endTimeString)*multiplier);
				commasFound = 0;
				matchComma = true;
                int c;
				for (c = 0; c < buffer.length(); c++)
				{
					if (buffer.charAt(c) == ',' && matchComma)
						commasFound++;
					if (commasFound >= lastCommaCount)
						break;
				}
                //Have to skip over all text within curly braces
				char newBuffer[] = new char[buffer.length()];
                int newBufferCount = 0;
                for (i = c+1; i < buffer.length(); i++)
                {
                    if (buffer.charAt(i) == '{') {
                        int j = i;
                        while (buffer.charAt(++j) != '}') ;
                        String tag = buffer.substring(i+1, j);
                        String tags[] = tag.split("\\\\");
                        String htmlString = constructHTMLString(tags);
                        for (char htmlChar : htmlString.toCharArray())
                            newBuffer[newBufferCount++] = htmlChar;
                        i = j;
                    }
                    else
                        newBuffer[newBufferCount++] = buffer.charAt(i);
                }
                buffer = new String(newBuffer).trim();
				for (i = 0; i < replaceText.length; i++)
					buffer = buffer.replace(replaceText[i], replaceTextWith[i]);
                if (buffer.length() > 0)
				    blocks.add(new TimeBlock(buffer.trim(), beginTime, endTime, playerInstance));
			}
		} catch (IOException e) {
            return;
		}
	}
    //SSA format stores colors in hex BBGGRR hex format; this is the opposite order of HTML, which
    //uses RRGGBB; thus we should reverse the color order
    public String parseColors(String tag)
    {
        //Make sure we don't try and go beyond the index of the string; a length of < 9 means the
        //tag is not valid anyways
        if (tag.length() <= 9)
            return "";
        int offset = 1;
        StringBuilder out = new StringBuilder("<font color=#");
        //If we don't have a number as the first character, we need to move the offset back one
        //in order to get the proper values
        if (tag.charAt(0) == 'c')
            offset = 0;
        //Append the text in the order Red, Green, Blue
        //Red
        out.append(tag.substring(7+offset,7+offset+2));
        //Green
        out.append(tag.substring(5+offset,5+offset+2));
        //Blue
        out.append(tag.substring(3+offset,3+offset+2));
        out.append(">");
        return out.toString();
    }
    public String constructHTMLString(String[] tags)
    {
        StringBuilder htmlString = new StringBuilder();
        for (String tag : tags) {
            if (tag.length() > 4 && ((tag.substring(0, 4).compareTo("1c&H") == 0)
                    || (tag.substring(0, 3).compareTo("c&H")) == 0))
                        htmlString.append(parseColors(tag));
                for (int i = 0; i < replaceTags.length; i++)
                    if (tag.compareTo(replaceTags[i]) == 0)
                        htmlString.append(replaceTagsWith[i]);
        }
        return htmlString.toString();
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
