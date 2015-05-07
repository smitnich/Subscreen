package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreen.FileHelper;
import com.subscreen.FrameBlock;
import com.subscreen.Subtitles.MicroDVDFormat;
import com.subscreen.Subtitles.SubViewerTwoFormat;
import com.subscreen.TextBlock;

import java.util.ArrayList;

/**
 * Created by Nick on 4/21/2015.
 */
public class BasicMicroDVDTest extends ApplicationTestCase<Application> {
    public BasicMicroDVDTest() {
        super(Application.class);
    }
    public void testMicroDVD() {
        ArrayList<TextBlock> blocks = null;
        String path = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
        MicroDVDFormat mdvd = new MicroDVDFormat(null);
        blocks = mdvd.readFile(FileHelper.readFile(path + "sub/testSub.txt", null), "UTF-8");
        FrameBlock firstBlock = (FrameBlock) blocks.get(0);
        assertEquals(firstBlock.startFrame,512);
        assertEquals(firstBlock.endFrame,613);
        assertEquals(firstBlock.text,"Begin Text");
        FrameBlock lastBlock = (FrameBlock) blocks.get(blocks.size()-1);
        assertEquals(lastBlock.text, "End Text");
    }
}