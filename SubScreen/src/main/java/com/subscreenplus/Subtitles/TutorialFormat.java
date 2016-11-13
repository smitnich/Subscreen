package com.subscreenplus.Subtitles;

import com.subscreenplus.SubtitlePlayer;
import com.subscreenplus.TextBlock;
import com.subscreenplus.TimeBlock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Nick on 5/24/2015.
 */
public class TutorialFormat implements SubtitleFormat {
    SubtitlePlayer playerInstance = null;
    public TutorialFormat(SubtitlePlayer tmpPlayer)
    {
        playerInstance = tmpPlayer;
    }

    public ArrayList<TextBlock> readFile(InputStream data, String srcCharset) {
        SrtFormat srtHelper = new SrtFormat(playerInstance);
        String folderPath = System.getenv("EXTERNAL_STORAGE") + "/" + "Subtitles/";
        ArrayList<TextBlock> blocks = null;
        try {
            data.skip("SUBSCREEN_TUTORIAL\r\n".length());
            blocks = srtHelper.readFile(data, srcCharset);
            //Remove the play block so that the first instruction will automatically appear
            blocks.remove(0);
            //Replace the placeholder string with the actual path
            TimeBlock block = (TimeBlock) blocks.get(10);
            block.text = block.text.replace("$SUBTITLE_FOLDER_PATH$",folderPath);
        } catch (IOException e) {
            return null;
        }
        return blocks;
    }
}
