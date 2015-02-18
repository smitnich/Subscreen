package com.subscreen.Subtitles;

import android.widget.TextView;

import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.FrameBlock;
import com.subscreen.UnicodeReader;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nick on 12/28/2014.
 */
public class SUBFormat implements SubtitleFormat {
    public TextView writeTo;
    SubtitlePlayer playerInstance = null;
    public SUBFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }
    public ArrayList<TextBlock> readFile(String path) {
        ArrayList<TextBlock> blocks = new ArrayList<>();
        UnicodeReader br = new UnicodeReader(path);
        readLines(br, blocks);
        return blocks;
    }
    void readLines(UnicodeReader in, ArrayList<TextBlock> blocks) {
        String[] replace = {"|"};
        String[] replaceWith = {"\n"};
        //Pattern p = Pattern.compile("\\[(\\d*)\\]\\[(\\d*)\\]");
        Pattern p = Pattern.compile("\\{(\\d*)\\}\\{(\\d*)\\}(.*)");
        String buffer;
        Matcher m;
        String text;
        String options;
        long startFrame, endFrame;
        try {
            while (in.available() > 0) {
                buffer = new String(in.readLine());
                m = p.matcher(buffer);
                if (m.find())
                {
                    startFrame = Integer.parseInt(m.group(1));
                    endFrame = Integer.parseInt(m.group(2));
                    text = m.group(3);
                    if (text.charAt(0) == '{')
                    {
                        text = buildOptions(text.substring(1, text.indexOf('}')),text.substring(text.indexOf('}') + 1));
                    }
                    for (int i = 0; i < replace.length; i++)
                        text = text.replace(replace[i],replaceWith[i]);
                    blocks.add(new FrameBlock(text,startFrame,endFrame,playerInstance));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //This format allows for a variety of options that need to be parsed in order to be converted
    //to proper HTML
    String buildOptions(String input, String text)
    {
        StringBuilder startText = new StringBuilder("<font color=\"");
        switch(input.charAt(0))
        {
            case 'y':
            case 'Y':
                startText.append("yellow\">");
                break;
            case 'r':
            case 'R':
                startText.append("red\">");
                break;
            case 'b':
            case 'B':
                startText.append("blue\">");
                break;
            default:
                startText.append("white\">");
                break;
        }
        switch (input.charAt(2))
        {
            case 'i':
                startText.append("<i>");
                break;
            case 'b':
                startText.append("<b>");
                break;
            case 'u':
                startText.append("<u>");
                break;
        }
        return startText.toString();
    }
}
