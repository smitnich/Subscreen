package com.subscreen.Subtitles;

import android.widget.TextView;

import com.subscreen.FileReaderHelper;
import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

import java.io.IOException;
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
    public ArrayList<TextBlock> readFile(String path) {
        ArrayList<TextBlock> blocks = new ArrayList<>();
        FileReaderHelper br = new FileReaderHelper(path);
        readLines(br, blocks);
        return blocks;
    }
    public void readLines(FileReaderHelper in,  ArrayList<TextBlock> blocks)
    {
        String[] replace = {"[br]"};
        String[] replaceWith = {"\n"};
        try {
            char[] buffer;
            String str;
            long startTime, endTime;
            int pos = -1;
            String text;
            do {
                buffer = in.readLine();
            } while (!(buffer[0] >= '0' && buffer[0] <= '9'));
            while (in.available() > 0) {
                if (blocks.size() > 0)
                    buffer = in.readLine();
                str = new String(buffer).trim();
                pos = str.indexOf(',');
                if (pos == -1)
                    continue;
                startTime = parseTimeStamp(str.substring(0,pos));
                endTime = parseTimeStamp(str.substring(pos+1));
                text = new String(in.readLine());
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
    long parseTimeStamp(String input)
    {
        int count = input.indexOf(':');
        int nextCount = -1;
        int hours = Integer.parseInt(input.substring(0,count));
        nextCount = input.indexOf(":",count+1);
        int minutes = Integer.parseInt(input.substring(count+1,nextCount));
        count = input.indexOf(':',nextCount);
        nextCount = input.indexOf('.',count);
        int seconds = Integer.parseInt(input.substring(count+1,nextCount));
        int milliseconds = Integer.parseInt(input.substring(nextCount+1,input.length()));
        return (hours*60*60*1000) + (minutes*60*1000) + (seconds*1000) + milliseconds;
    }
}
