package com.subscreenplus.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreenplus.FileHelper;
import com.subscreenplus.Subtitles.SubViewerTwoFormat;
import com.subscreenplus.TextBlock;

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
        FileHelper.EncodingWrapper wrapper = FileHelper.readFile(path + "txt/test.srt", null);
        blocks = sub.readFile(wrapper.data, wrapper.encoding);
        assertEquals(blocks.get(1).getStartTime(),26040);
        assertEquals(blocks.size(), 507);
    }
    public void testSubParseTime()
    {
        SubViewerTwoFormat sub = new SubViewerTwoFormat(null);
        long time = sub.parseTimeStamp("12:34:56.78");
        assertEquals(time,12*60*60*1000+34*60*1000+56*1000+78*10);
    }
}