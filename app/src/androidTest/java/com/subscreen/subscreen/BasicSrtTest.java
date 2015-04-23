package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreen.Subtitles.SrtFormat;
import com.subscreen.TextBlock;

import java.util.ArrayList;

public class BasicSrtTest extends ApplicationTestCase<Application> {
    public BasicSrtTest() {
        super(Application.class);
    }
    public void testSrt()
    {
        ArrayList<TextBlock> blocks = null;
        String path = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
        SrtFormat srt = new SrtFormat(null);
        blocks = srt.readFile(path+"srt/test.srt", "UTF-8");
        assertEquals(blocks.get(0).getStartTime(),3160);
        assertEquals(blocks.size(),681);
    }
    public void testSRTParseTime()
    {
        SrtFormat srt = new SrtFormat(null);
        int result = srt.parseTimeStamp("12:34:56,789");
        assertEquals(result,12*60*60*1000+34*60*1000+56*1000+789);
    }
}