package com.subscreen.Subtitles;

import com.subscreen.SubtitlePlayer;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * Created by Nick on 12/26/2014.
 */
public class SMIFormat implements SubtitleFormat {

    SubtitlePlayer playerInstance = null;
    ArrayList<String> allLanguages = new ArrayList<String>();
    ArrayList<String> allIds = new ArrayList<String>();
    ArrayList<String> allNames = new ArrayList<String>();
    ArrayList<ArrayList<TextBlock>> allBlocks = new ArrayList<ArrayList<TextBlock>>();
    TimeBlock playBlock;
    public SMIFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }
    public ArrayList<TextBlock> readFile(InputStream data, String srcCharset) {
        try {
            playBlock =  new TimeBlock(SubtitlePlayer.playString,-1, playerInstance);
            ArrayList<TextBlock> blocks = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(data, srcCharset));
            readLines(br, blocks);
            for (String id : allIds) {
                for (int i = 0; i < allLanguages.size(); i++) {
                    String language = allLanguages.get(i);
                    if (id.compareTo(language) == 0) {
                        language = String.format("%s (%s)",allNames.get(i),language);
                        allLanguages.set(i,language);
                    }
                }
            }
            return allBlocks.get(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
                while (true) {
                    String tag = "Default";
                    StringBuilder text = new StringBuilder();
                    endTime = -1;
                    m = p.matcher(str);
                    if (m.find()) {
                        startTime = Integer.parseInt(m.group(1));
                        if (m.group(2) != null)
                            endTime = Integer.parseInt(m.group(2));
                        char[] origText = m.group(3).toCharArray();
                        char[] newText = new char[origText.length];
                        int j = 0;
                        int k = 0;
                        if (m.group(3).length() > 0) {
                            char[] tagBuffer = new char[128];
                            for (int i = 0; i < origText.length; i++) {
                                //If we find an opening tag, skip to the
                                //closing tag
                                if (origText[i] == '<') {
                                    boolean equalsFound = false;
                                    while (origText[i] != '>') {
                                        if (equalsFound)
                                            tagBuffer[k++] = origText[i];
                                        if (origText[i] == '=')
                                            equalsFound = true;
                                        i++;
                                    }
                                    tag = new String(tagBuffer).trim();
                                }
                                else
                                    newText[j++] = origText[i];
                            }
                            text.append(new String(newText).trim());
                        }
                        while (true) {
                            str = in.readLine().trim();
                            if (str == null || str.length() == 0)
                                break;
                            if (!str.toUpperCase().startsWith("<SYNC"))
                                    text.append(str);
                            else
                                break;
                        }
                        for (int i = 0; i < replaceString.length; i++) {
                            int index = text.indexOf(replaceString[i]);
                            while (index != -1) {
                                text.replace(index,replaceString[i].length(),replaceWith[i]);
                                index = text.indexOf(replaceString[i]);
                            }
                        }
                        if (prevBlock != null && prevBlock.endTime == -1)
                            prevBlock.endTime = startTime;
                        String finalText = text.toString();
                        finalText = finalText.trim();
                        //If we have an empty line of text, it will appear when using the prev/next
                        //buttons to move through the text blocks. Thus we should not add any empty
                        //lines, but instead use their starting time as the previous blocks ending time
                        if (finalText.length() > 0)
                            newBlock = new TimeBlock(finalText, startTime, playerInstance);
                        else
                            continue;
                        newBlock.endTime = endTime;
                        addBlock(newBlock,tag);
                        prevBlock = newBlock;
                    } else {
                        str = in.readLine().trim();
                        if (str.length() > 0 && str.charAt(0) == '.')
                            checkLanguageNames(str);
                        if (str == null)
                            break;
                    }
                }
            //If we didn't get an end time on the last block, set it to display for 5 seconds by default
            if (prevBlock != null && prevBlock.endTime == -1){
                prevBlock.endTime = prevBlock.startTime + 5*1000;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private void checkLanguageNames(String input) {
        int braceLocation = input.indexOf('{');
        if (braceLocation == -1)
            return;
        String id = input.substring(1,braceLocation).trim();
        int start = input.toLowerCase().indexOf("name");
        if (start == -1)
            return;
        while ((start < input.length()) && input.charAt(start++) != ':');
        int end = input.indexOf(';',start);
        String name = input.substring(start,end).trim();
        allIds.add(id);
        allNames.add(name);
    }
    public void addBlock(TimeBlock block, String name) {
        for (int i = 0; i < allBlocks.size(); i++) {
            if (name.compareTo(allLanguages.get(i)) == 0) {
                ArrayList<TextBlock> tmpBlocks = allBlocks.get(i);
                tmpBlocks.add(block);
                return;
            }
        }
        allLanguages.add(name);
        ArrayList<TextBlock> newBlock = new ArrayList<TextBlock>();
        newBlock.add(playBlock);
        newBlock.add(block);
        allBlocks.add(newBlock);
    }
    public ArrayList<String> getAvailableLanguages() {
        return allLanguages;
    }
    public ArrayList<TextBlock> getLanguage(long id) {
        return allBlocks.get((int) id);
    }
}