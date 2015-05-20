package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreen.FileHelper;
import com.subscreen.Subtitles.MPLFormat;
import com.subscreen.Subtitles.VTTFormat;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

import java.util.ArrayList;

/**
 * Created by Nick on 4/22/2015.
 */
public class BasicMPLTest extends ApplicationTestCase<Application> {
    public BasicMPLTest() {
        super(Application.class);
    }
    public void testMPL()
    {
        ArrayList<TextBlock> blocks = null;
        String path = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
        MPLFormat mpl = new MPLFormat(null);
        FileHelper.EncodingWrapper wrapper = FileHelper.readFile(path + "mpl/testMpl.txt", null);
        blocks = mpl.readFile(wrapper.data, wrapper.encoding);
        TimeBlock firstBlock = (TimeBlock) blocks.get(1);
        assertEquals(firstBlock.getStartTime(), 376*100);
        assertEquals("Begin Text.", firstBlock.text);
        TimeBlock lastBlock = (TimeBlock) blocks.get(blocks.size()-1);
        assertEquals("End Text.",lastBlock.text);
    }
}