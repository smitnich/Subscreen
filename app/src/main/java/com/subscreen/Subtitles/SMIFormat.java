package com.subscreen.Subtitles;

import android.widget.TextView;

import com.subscreen.FileReaderHelper;
import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * Created by Nick on 12/26/2014.
 */
public class SMIFormat implements SubtitleFormat {

    public TextView writeTo;
    SubtitlePlayer playerInstance = null;
    public SMIFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }
    public ArrayList<TextBlock> readFile(String path) {
        ArrayList<TextBlock> blocks = new ArrayList<>();
        FileReaderHelper br = new FileReaderHelper(path,"UTF-8");
        readLines(br, blocks);
        return blocks;
    }

    public void readLines(FileReaderHelper in, ArrayList<TextBlock> blocks) {
        String replaceString[] = {"&nbsp;"};
        String replaceWith[] = {" "};
        String regexPattern = "<SYNC Start=(\\d*)(?:\\s)*(?:End=(\\d*))*>(.*)";
        Pattern p = Pattern.compile(regexPattern,Pattern.CASE_INSENSITIVE);
        char[] buffer = null;
        String str = null;
        long startTime, endTime;
        TimeBlock prevBlock = null;
        TimeBlock newBlock = null;
        buffer = in.readLine();
        str = new String(buffer);
        Matcher m;
        try {
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
                    if (m.group(3).length() > 0)
                    {
                        for (int i = 0; i < origText.length; i++)
                        {
                            //If we find an opening tag, skip to the
                            //closing tag
                            if (origText[i] == '<')
                                while (origText[++i] != '>');
                            else
                                newText[j++] = origText[i];
                        }
                        text = new String(newText);
                    }
                    while (in.available() > 0) {
                        str = new String(in.readLine());
                        if (str.length() == 0)
                            break;
                        if (!str.startsWith("<SYNC"))
                            text = text + str;
                        else
                            break;
                    }
                    for (int i = 0; i < replaceString.length; i++)
                        text = text.replace(replaceString[0],replaceWith[i]);
                    if (prevBlock != null && prevBlock.endTime == -1)
                        prevBlock.endTime = startTime;
                    //If we have an empty line of text, it will appear when using the prev/next
                    //buttons to move through the text blocks. Thus we should not add any empty
                    //lines, but instead use their starting time as the previous blocks ending time
                    if (text.trim().length() > 0)
                       newBlock = new TimeBlock(text, startTime,playerInstance);
                    else
                        continue;
                    newBlock.endTime = endTime;
                    blocks.add(newBlock);
                    prevBlock = newBlock;
                }
                else
                {
                    str = new String(in.readLine());
                }
            } while (in.available() > 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //If we didn't get an end time on the last block, set it to display for 5 seconds by default
        if (prevBlock != null && prevBlock.endTime == -1){
            prevBlock.endTime = prevBlock.startTime + 5*1000;
        }
    }
}