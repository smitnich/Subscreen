package com.subscreen.Subtitles;

import android.widget.TextView;

import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Nick on 12/27/2014.
 */
public class SubViewerTwoFormat implements SubtitleFormat {
    public TextView writeTo;
    SubtitlePlayer playerInstance = null;
    public SubViewerTwoFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }
    public ArrayList<TextBlock> readFile(InputStream data, String srcCharset) {
        try {
            ArrayList<TextBlock> blocks = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(data, srcCharset));
            readLines(br, blocks);
            return blocks;
        }
        catch (Exception e)
        {
            return null;
        }
    }
    public void readLines(BufferedReader in,  ArrayList<TextBlock> blocks)
    {
        String[] replace = {"[br]"};
        String[] replaceWith = {"<br>"};
        try {
            String buffer;
            String str;
            long startTime, endTime;
            int pos = -1;
            String text;
            do {
                buffer = in.readLine();
            } while (buffer.length() == 0 || !(buffer.charAt(0) >= '0' && buffer.charAt(0) <= '9'));
            while (true) {
                if (blocks.size() > 0)
                    buffer = in.readLine();
                if (buffer == null)
                    break;
                str = buffer.trim();
                pos = str.indexOf(',');
                if (pos == -1)
                    continue;
                startTime = parseTimeStamp(str.substring(0,pos));
                endTime = parseTimeStamp(str.substring(pos+1));
                text = in.readLine();
                for (int i = 0; i < replace.length; i++)
                    text = text.replace(replace[i],replaceWith[i]);
                blocks.add(new TimeBlock(text,startTime,endTime,playerInstance));
            }
        }
        catch (IOException e)
        {
            return;
        }
    }
    public long parseTimeStamp(String input)
    {
        int count = input.indexOf(':');
        int nextCount = -1;
        int hours = Integer.parseInt(input.substring(0,count));
        nextCount = input.indexOf(":",count+1);
        int minutes = Integer.parseInt(input.substring(count+1,nextCount));
        count = input.indexOf(':',nextCount);
        nextCount = input.indexOf('.',count);
        int seconds = Integer.parseInt(input.substring(count+1,nextCount));
        //Multiply the milliseconds value by 10 because only 2 digits are used in this format,
        //whereas a three digits are necessary to represent one millisecond
        int milliseconds = Integer.parseInt(input.substring(nextCount+1,input.length()))*10;
        return (hours*60*60*1000) + (minutes*60*1000) + (seconds*1000) + milliseconds;
    }
}
