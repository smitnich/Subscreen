package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

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
        blocks = mpl.readFile(path+"mpl/testMpl.txt", "UTF-8");
        TimeBlock firstBlock = (TimeBlock) blocks.get(0);
        assertEquals(firstBlock.getStartTime(), 376*100);
        assertEquals("Begin Text.", firstBlock.text);
        TimeBlock lastBlock = (TimeBlock) blocks.get(blocks.size()-1);
        assertEquals("End Text.",lastBlock.text);
    }
}