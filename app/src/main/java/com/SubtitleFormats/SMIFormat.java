package com.subscreen.SubtitleFormats;

import android.widget.TextView;

import com.subscreen.TextBlock;
import com.subscreen.UnicodeReader;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * Created by Nick on 12/26/2014.
 */
public class SMIFormat implements SubtitleFormat {

    public TextView writeTo;

    public ArrayList<TextBlock> readFile(String path) {
        String fullPath = System.getenv("EXTERNAL_STORAGE") + "/" + path;
        ArrayList<TextBlock> blocks = new ArrayList<TextBlock>();
        UnicodeReader br = new UnicodeReader(fullPath);
        readLines(br, blocks);
        return blocks;
    }

    public void readLines(UnicodeReader in, ArrayList<TextBlock> blocks) {
        Pattern p = Pattern.compile("<SYNC Start=(\\d*) (?:End=(\\d*))*");
        char[] buffer = null;
        String str = null;
        long startTime, endTime;
        TextBlock prevBlock = null;
        Matcher m;
        try {
            do {
                endTime = -1;
                buffer = in.readLine();
                str = new String(buffer);
                m = p.matcher(str);
                if (m.find()) {
                    startTime = Integer.parseInt(m.group(1));
                    if (prevBlock != null) {
                        prevBlock.endTime = startTime;
                        blocks.add(prevBlock);
                    }
                    if (m.groupCount() > 1)
                        endTime = Integer.parseInt(m.group(2));
                    buffer = in.readLine();
                    prevBlock = new TextBlock(new String(buffer), startTime);
                    //If we didn't find an end time, we need to use the start time of the next block
                    //as the end time
                    if (m.groupCount() > 1) {
                        prevBlock.endTime = endTime;
                        blocks.add(prevBlock);
                        prevBlock = null;
                    }
                }
            } while (in.available() > 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //If we didn't get an end time on the last block, set it to display for 5 seconds by default
        if (prevBlock != null){
            prevBlock.endTime = prevBlock.startTime + 5*1000;
        }
    }
}