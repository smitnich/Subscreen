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

public class VTTFormat implements SubtitleFormat {

    public TextView writeTo;
    SubtitlePlayer playerInstance = null;
    public VTTFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }
    public ArrayList<TextBlock> readFile(InputStream data, String srcCharset)
    {
        ArrayList<TextBlock> blocks = new ArrayList<>();
        blocks.add(new TimeBlock(SubtitlePlayer.playString,0,-1,playerInstance));
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(data,srcCharset));
            readLines(br, blocks);
        }
        catch (Exception e)
        {
            return null;
        }
        return blocks;
    }
    public void readLines(BufferedReader in, ArrayList<TextBlock> blocks)
    {
        String buffer;
        char[] cbuf = new char[1024];
        int current = 1;
        try {
            //Skip ahead to the first actual line of text
            in.readLine();
            in.readLine();
            while (true)
            {
                //Read the block number and throw a warning if it is not the expected one
                String tmp = in.readLine();
                if (tmp == null)
                    break;
                buffer = tmp.trim();
                String beginTimeString = buffer.substring(0,buffer.indexOf('-')).trim();
                String endTimeString = buffer.substring(buffer.lastIndexOf('>')+2,buffer.length()).trim();
                long beginTime = parseTimeStamp(beginTimeString);
                long endTime = parseTimeStamp(endTimeString);
                tmp = buffer;
                buffer = "";
                while (tmp != null && tmp.length() > 0)
                {
                    tmp = in.readLine();
                    if (tmp == null) {
                        blocks.add(new TimeBlock(buffer,beginTime,endTime,playerInstance));
                        return;
                    }
                    tmp = tmp.trim();
                    if (tmp.length() > 0)
                        buffer += tmp + "<br>";
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
        nextCount = input.indexOf('.',count);
        int seconds = Integer.parseInt(input.substring(count+1,nextCount));
        int milliseconds = Integer.parseInt(input.substring(nextCount+1,input.length()));
        return (hours*60*60*1000) + (minutes*60*1000) + (seconds*1000) + milliseconds;
    }
}