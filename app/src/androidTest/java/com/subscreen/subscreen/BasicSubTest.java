package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreen.Subtitles.SubViewerTwoFormat;
import com.subscreen.TextBlock;

import java.util.ArrayList;

/**
 * Created by Nick on 4/21/2015.
 */
public class BasicSubTest extends ApplicationTestCase<Application> {
    public BasicSubTest() {
        super(Application.class);
    }
    public void testSub() {
        ArrayList<TextBlock> blocks = null;
        String path = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
        SubViewerTwoFormat sub = new SubViewerTwoFormat(null);
        blocks = sub.readFile(path+"txt/test.srt", "UTF-8");
        assertEquals(blocks.get(0).getStartTime(),26040);
        TextBlock tmpBlock = blocks.get(blocks.size()-1);
        assertEquals(blocks.size(), 506);
    }
    public void testSubParseTime()
    {
        SubViewerTwoFormat sub = new SubViewerTwoFormat(null);
        long time = sub.parseTimeStamp("12:34:56.78");
        assertEquals(time,12*60*60*1000+34*60*1000+56*1000+78*10);
    }
}