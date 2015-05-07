package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreen.FileHelper;
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
        blocks = srt.readFile(FileHelper.readFile(path + "srt/test.srt", null), "UTF-8");
        assertEquals(3160,blocks.get(0).getStartTime());
        assertEquals(681,blocks.size());
    }
    public void testSRTParseTime()
    {
        SrtFormat srt = new SrtFormat(null);
        int result = srt.parseTimeStamp("12:34:56,789");
        assertEquals(12*60*60*1000+34*60*1000+56*1000+789,result);
        result = srt.parseTimeStamp("12:34:56,78");
        assertEquals(12*60*60*1000+34*60*1000+56*1000+780,result);
        result = srt.parseTimeStamp("12:34:56,7");
        assertEquals(12*60*60*1000+34*60*1000+56*1000+700,result);
        result = srt.parseTimeStamp("12:34:56");
        assertEquals(12*60*60*1000+34*60*1000+56*1000,result);
    }
}