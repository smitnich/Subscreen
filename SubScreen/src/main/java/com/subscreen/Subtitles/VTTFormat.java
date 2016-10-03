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

    private static String[] toSkip = {"NOTE", "REGION", "STYLE"};
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
        fixupTimeStamps(blocks);
        return blocks;
    }
    public void readLines(BufferedReader in, ArrayList<TextBlock> blocks) throws Exception{
        int i = 0;
        // Skip the header
        in.readLine();
        while (in.ready()) {
                TimeBlock block = parseBlock(in);
                if (block != null)
                    blocks.add(block);
            }
    }
    public int parseTimeStamp(String input)
    {
        int count = input.indexOf(':');
        int nextCount = 0;
        input = input.replace(".",",");
        int hours = 0;
        // Check if we have two :s in the input by finding the first one, then checking if one
        // exists beyond that one
        if (input.indexOf(':', input.indexOf(':')+1) != -1) {
            hours = Integer.parseInt(input.substring(0, count).trim());
            nextCount = input.indexOf(":", count + 1);
            count += 1;
        }
        else
        {
            nextCount = input.indexOf(":");
            count = 0;
        }
        int minutes = Integer.parseInt(input.substring(count, nextCount).trim());
        count = input.indexOf(':',nextCount);
        nextCount = input.indexOf(',',count);
        if (nextCount == -1)
            nextCount = input.length();
        int seconds = Integer.parseInt(input.substring(count+1,nextCount).trim());
        int milliseconds = 0;
        if (input.length() > nextCount + 1)
            milliseconds = Integer.parseInt(input.substring(nextCount+1).trim());
        return (hours*60*60*1000) + (minutes*60*1000) + (seconds*1000) + milliseconds;
    }
    // We don't want to deal with the voice tags for now...
    String stripVoice(String input) {
        if (input.startsWith("<v"))
            return input.substring(input.indexOf('>')+1);
        else
            return input;
    }

    TimeBlock parseBlock(BufferedReader in) {
        try {
            String input = in.readLine();
            if (input.length() == 0)
                return null;
            // If we find a block that we don't process, skip ahead to the next empty line
            for (String skip : toSkip) {
                if (input.startsWith(skip)) {
                    while (in.readLine().length() > 0) ;
                    return null;
                }
            }
            // Skip past the optional block numbers, we don't need them
            while (isInt(input.toCharArray()) || input.length() == 0)
            {
                input = in.readLine();
            }
            input = input.trim();
            String beginTimeString = input.substring(0,input.indexOf('-')).trim();
            int tmpNum =  input.indexOf(' ', input.lastIndexOf('>')+2);
            if (tmpNum == -1)
                tmpNum = input.length();
            String endTimeString = input.substring(input.lastIndexOf('>')+2, tmpNum).trim();
            long beginTime = parseTimeStamp(beginTimeString);
            long endTime = parseTimeStamp(endTimeString);
            input = in.readLine();
            String text = "";
            while (input != null && !(isInt(input.toCharArray()) || input.length() == 0))
            {
                text += stripVoice(input.trim());
                input = in.readLine();
            }
            if (text.length() > 0)
                return new TimeBlock(text, beginTime, endTime, playerInstance);

        } catch (Exception e)
        {
            return null;
        }
        return null;
    }
    private boolean isInt(char[] input) {
        for (int i = 0; i < input.length; i++)
        {
            if ((input[i] >= '0' && input[i] <= '9') || (i == 0 && input[i] == '-'))
                continue;
            else
                return false;
        }
        return true;
    }
    // Check for any overlapping timestamps, as VTT allows for multiple cues to be showing at once
    // We can't support this, so just make the first cue stop when the second one starts
    public void fixupTimeStamps(ArrayList<TextBlock> blocks) {
        int i;
        for (i = 0; i < blocks.size()-1; i++)
        {
            if (blocks.get(i).getEndValue() > blocks.get(i+1).getStartValue())
            {
                blocks.get(i).setEndValue(blocks.get(i+1).getStartValue());
            }
        }
    }
}