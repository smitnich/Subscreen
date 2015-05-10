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
        int forcedFramerate = -1;
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
            //Check if the first line is a value specifying the framerate
            in.mark(128);
            buffer = in.readLine();
            buffer = buffer.substring(buffer.lastIndexOf('}')+1);
            try {
                double textNum = Double.parseDouble(buffer);
                for (int i = 0; i < FrameBlock.frameRates.length; i++) {
                    double frameRate = FrameBlock.frameRates[i];
                    if (Math.abs(frameRate - textNum) < 0.05) {
                        forcedFramerate = i;
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                //Not a valid number, so just ignore it and continue as normal
                in.reset();
            }
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
        if (forcedFramerate != -1 && blocks.size() > 0) {
            FrameBlock.setFrameRate(forcedFramerate);
        }
        //If the last end time tag wasn't closed, fill in a default end time for it
        if (lastBlock != null)
            lastBlock.endFrame = lastBlock.startFrame + 100;
    }
    //This format allows for a variety of options that need to be parsed in order to be converted
    //to proper HTML
    //Also works for TMP format, so it is declared static for use in TmpFormat.java
    static String buildOptions(String input)
    {
        if (input.charAt(0) == '/')
            return "<i>" + input.substring(1);
        else if (input.charAt(0) != '{')
            return input;
        boolean doEndText = true;
        StringBuilder startText = new StringBuilder("");
        StringBuilder endText = new StringBuilder("");
        String[] options = input.split("\\}");
        for (String option : options) {
            if (option.charAt(0) != '{')
                continue;
            int i = 1;
            doEndText = true;
            switch (option.charAt(i)) {
                //If we have a capital control code, then it means that we shouldn't end the font
                //specification so that it carries onto the next line
                case 'Y':
                    doEndText = false;
                case 'y':
                    while (i + 2 < option.length()) {
                        i += 2;
                        switch (option.charAt(i)) {
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
                            default:
                                //Move back one space so that we will end up moving forward by one
                                //once we add two to i
                                i--;
                                break;
                        }
                    }
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
                    //Fall through
                case 'c':
                    //Text color
                    //MicroDVD format uses BBGGRR color format, so we need to switch around some of
                    //the values
                    char[] colors = option.substring(i+3, i+9).toCharArray();
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
                    break;
            }
        }
        startText.append(options[options.length-1]);
        if (doEndText)
            startText.append(endText.toString());
        return startText.toString();
    }
}
