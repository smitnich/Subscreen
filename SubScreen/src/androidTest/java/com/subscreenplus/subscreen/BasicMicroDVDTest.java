package com.subscreenplus.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreenplus.FileHelper;
import com.subscreenplus.FrameBlock;
import com.subscreenplus.Subtitles.MicroDVDFormat;
import com.subscreenplus.TextBlock;

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
        FileHelper.EncodingWrapper wrapper = FileHelper.readFile(path + "sub/testSub.txt", null);
        blocks = mdvd.readFile(wrapper.data, wrapper.encoding);
        FrameBlock firstBlock = (FrameBlock) blocks.get(1);
        assertEquals(firstBlock.startFrame,512);
        assertEquals(firstBlock.endFrame,613);
        assertEquals(firstBlock.text,"Begin Text");
        FrameBlock lastBlock = (FrameBlock) blocks.get(blocks.size()-1);
        assertEquals(lastBlock.text, "End Text");
    }
}