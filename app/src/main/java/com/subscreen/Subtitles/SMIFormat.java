package com.subscreen.Subtitles;

import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * Created by Nick on 12/26/2014.
 */
public class SMIFormat implements SubtitleFormat {

    SubtitlePlayer playerInstance = null;
    public SMIFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }
    public ArrayList<TextBlock> readFile(String path, String srcCharset) {
        try {
            ArrayList<TextBlock> blocks = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), srcCharset));
            readLines(br, blocks);
            return blocks;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public void readLines(BufferedReader in, ArrayList<TextBlock> blocks) {
        try {
            String replaceString[] = {"&nbsp;"};
            String replaceWith[] = {" "};
            String regexPattern = "<SYNC Start=(\\d*)(?:\\s)*(?:End=(\\d*))*>(.*)";
            Pattern p = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            String str = null;
            long startTime, endTime;
            TimeBlock prevBlock = null;
            TimeBlock newBlock = null;
            str = in.readLine();
            Matcher m;
                do {
                    String text = new String();
                    endTime = -1;
                    m = p.matcher(str);
                    if (m.find()) {
                        startTime = Integer.parseInt(m.group(1));
                        if (m.group(2) != null)
                            endTime = Integer.parseInt(m.group(2));
                        char[] origText = m.group(3).toCharArray();
                        char[] newText = new char[origText.length];
                        int j = 0;
                        if (m.group(3).length() > 0) {
                            for (int i = 0; i < origText.length; i++) {
                                //If we find an opening tag, skip to the
                                //closing tag
                                if (origText[i] == '<')
                                    while (origText[++i] != '>') ;
                                else
                                    newText[j++] = origText[i];
                            }
                            text = new String(newText);
                        }
                        while (true) {
                            str = in.readLine();
                            if (str == null || str.length() == 0)
                                break;
                            if (!str.startsWith("<"))
                                text = text + str;
                            else
                                break;
                        }
                        for (int i = 0; i < replaceString.length; i++)
                            text = text.replace(replaceString[0], replaceWith[i]);
                        if (prevBlock != null && prevBlock.endTime == -1)
                            prevBlock.endTime = startTime;
                        //If we have an empty line of text, it will appear when using the prev/next
                        //buttons to move through the text blocks. Thus we should not add any empty
                        //lines, but instead use their starting time as the previous blocks ending time
                        if (text.trim().length() > 0)
                            newBlock = new TimeBlock(text, startTime, playerInstance);
                        else
                            continue;
                        newBlock.endTime = endTime;
                        blocks.add(newBlock);
                        prevBlock = newBlock;
                    } else {
                        str = in.readLine();
                        if (str == null)
                            break;
                    }
                } while (true);
            if (prevBlock != null && prevBlock.endTime == -1){
                prevBlock.endTime = prevBlock.startTime + 5*1000;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //If we didn't get an end time on the last block, set it to display for 5 seconds by default
    }
}