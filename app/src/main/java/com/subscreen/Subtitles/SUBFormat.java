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
        int newLine = -1;
        int lastNewLine = -1;
        //Pattern p = Pattern.compile("\\[(\\d*)\\]\\[(\\d*)\\]");
        Pattern p = Pattern.compile("\\{(\\d*)\\}\\{(\\d*)\\}(.*)");
        StringBuilder allText = null;
        String buffer;
        Matcher m;
        String text;
        String options;
        long startFrame, endFrame;
        try {
            while (in.available() > 0) {
                allText = new StringBuilder();
                buffer = new String(in.readLine()).trim();
                m = p.matcher(buffer);
                if (m.find())
                {
                    newLine = -1;
                    startFrame = Integer.parseInt(m.group(1));
                    endFrame = Integer.parseInt(m.group(2));
                    text = m.group(3);
                    String[] textLines = text.split("\\|");
                    for (String tmpStr : textLines)
                    {
                        allText.append(buildOptions(tmpStr));
                        allText.append('\n');
                    }
                    blocks.add(new FrameBlock(allText.toString(),startFrame,endFrame,playerInstance));
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
    String buildOptions(String input)
    {
        if (input.charAt(0) != '{')
            return input;
        int i = 1;
        boolean doEndText = true;
        String text = input.substring(input.indexOf('}')+1);
        StringBuilder startText = new StringBuilder("");
        StringBuilder endText = new StringBuilder("");
        boolean formatAllLines = false;
        boolean doOption = true;
        switch(input.charAt(i))
        {
            //If we have a capital control code, then it means that we shouldn't end the font
            //specification so that it carries onto the next line
            case 'Y':
                doEndText = false;
            case 'y':
            while(doOption) {
                i += 2;
                switch (input.charAt(i)) {
                    case 'i':
                        startText.append("<i>");
                        endText.insert(0, "</i>");
                        break;
                    case 'b':
                        startText.append("<b>");
                        endText.insert(0, "</b>");
                        break;
                    case 'u':
                        startText.append("<u>");
                        endText.insert(0, "</u>");
                        break;
                    case 's':
                        startText.append("<s>");
                        endText.insert(0, "</s>");
                        break;
                }
                if (input.length() <= i+1 || input.charAt(i+1) != ',')
                    doOption = false;
            }
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
                /*i = 2;
                while (input.charAt(i++) != '}');
                startText.append("<font size=\"" + input.substring(2,i) +  "\">");
                endText.append("</font>");*/
                break;
            case 'C':
                doEndText = false;
            case 'c':
                //Text color
                //MicroDVD format uses BBGGRR color format, so we need to switch around some of
                //the values
                char[] colors = input.substring(3,8).toCharArray();
                endText.append("</font>");
                break;
        }
        startText.append(text);
        if (doEndText)
            startText.append(endText.toString());
        return startText.toString();
    }
}
