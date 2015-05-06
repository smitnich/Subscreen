package com.subscreen.Subtitles;

import android.widget.TextView;

import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.FrameBlock;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nick on 12/28/2014.
 */
public class MicroDVDFormat implements SubtitleFormat {
    public TextView writeTo;
    SubtitlePlayer playerInstance = null;
    public MicroDVDFormat(SubtitlePlayer tmpPlayer)
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
    void readLines(BufferedReader in, ArrayList<TextBlock> blocks) {
        int newLine = -1;
        int lastNewLine = -1;
        //Pattern p = Pattern.compile("\\[(\\d*)\\]\\[(\\d*)\\]");
        Pattern p = Pattern.compile("\\{(\\d*)\\}\\{(\\d*)\\}(.*)");
        StringBuilder allText = null;
        String buffer;
        Matcher m;
        String text;
        String options;
        FrameBlock lastBlock = null;
        long startFrame, endFrame;
        try {
            while (true) {
                allText = new StringBuilder();
                buffer = in.readLine();
                if (buffer == null)
                    break;
                buffer = buffer.trim();
                m = p.matcher(buffer);
                if (m.find())
                {
                    startFrame = Integer.parseInt(m.group(1));
                    try {
                        endFrame = Integer.parseInt(m.group(2));
                    } catch (NumberFormatException e)
                    {
                        //If we get a number format exception for the end time, set it equal to -1
                        //and later set it to the start of the next block
                        endFrame = -1;
                    }
                    if (lastBlock != null)
                        lastBlock.endFrame = startFrame;
                    text = m.group(3);
                    String[] textLines = text.split("\\|");
                    if (text.length() != 0) {
                        for (int i = 0; i < textLines.length; i++) {
                            allText.append(buildOptions(textLines[i]));
                            //Make sure we don't append a break tag on the last line
                            if (i < textLines.length - 1)
                                allText.append("<br>");
                        }
                        lastBlock = new FrameBlock(allText.toString(), startFrame, endFrame, playerInstance);
                        blocks.add(lastBlock);
                    }
                    //If the previous block did not have an end time, give it this block's starting
                    //time
                    if (lastBlock != null && lastBlock.endFrame != -1)
                        lastBlock = null;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //If the last end time tag wasn't closed, fill in a default end time for it
        if (lastBlock != null)
            lastBlock.endFrame = lastBlock.startFrame + 100;
    }
    //This format allows for a variety of options that need to be parsed in order to be converted
    //to proper HTML
    String buildOptions(String input)
    {
        if (input.charAt(0) != '{')
            return input;
        int i = 1;
        boolean doEndText = true;
        String text = input.substring(input.lastIndexOf('}')+1);
        StringBuilder startText = new StringBuilder("");
        StringBuilder endText = new StringBuilder("");
        boolean formatAllLines = false;
        boolean doOption = true;
        boolean doOptionBlock = true;
        while (doOptionBlock) {
            doEndText = true;
            switch (input.charAt(i)) {
                //If we have a capital control code, then it means that we shouldn't end the font
                //specification so that it carries onto the next line
                case 'Y':
                    doEndText = false;
                case 'y':
                    while (doOption) {
                        i += 2;
                        switch (input.charAt(i)) {
                            case 'i':
                                startText.append("<i>");
                                if (doEndText)
                                    endText.insert(0, "</i>");
                                break;
                            case 'b':
                                startText.append("<b>");
                                if (doEndText)
                                    endText.insert(0, "</b>");
                                break;
                            case 'u':
                                startText.append("<u>");
                                if (doEndText)
                                    endText.insert(0, "</u>");
                                break;
                            //These currently are not supported by HTML.fromHTML, but insert them
                            //anyways in case this ever changes
                            case 's':
                                startText.append("<s>");
                                if (doEndText)
                                    endText.insert(0, "</s>");
                                break;
                        }
                        if (input.length() <= i + 1 || input.charAt(i + 1) != ',')
                            if (input.charAt(i + 2) != '{')
                                doOption = false;
                            else
                                i++;
                    }
                    doOptionBlock = false;
                    break;
                case 'P':
                    //Position would have no relation to how the text oriented on the device
                case 'f':
                case 'F':
                    //We don't care about font name since they might not be installed, so ignore it
                case 's':
                case 'S':
                    //Font size
                    //Let's ignore this since the font size on a monitor is much different from a small
                    //phone, and probably will not appear as intended
                    break;
                case 'C':
                    doEndText = false;
                case 'c':
                    //Text color
                    //MicroDVD format uses BBGGRR color format, so we need to switch around some of
                    //the values
                    char[] colors = input.substring(i+3, i+9).toCharArray();
                    char tmp = colors[0];
                    colors[0] = colors[4];
                    colors[4] = tmp;
                    tmp = colors[1];
                    colors[1] = colors[5];
                    colors[5] = tmp;
                    startText.append("<font color=\"#");
                    startText.append(colors);
                    startText.append("\">");
                    if (doEndText)
                        endText.append("</font>");
                    i+=10;
                    if (input.charAt(i) != '{')
                        doOptionBlock = false;
                    break;
                default:
                    doOptionBlock = false;
            }
        }
        startText.append(text);
        if (doEndText)
            startText.append(endText.toString());
        return startText.toString();
    }
}
