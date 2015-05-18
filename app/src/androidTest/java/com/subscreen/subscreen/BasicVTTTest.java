package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreen.FileHelper;
import com.subscreen.Subtitles.SrtFormat;
import com.subscreen.Subtitles.VTTFormat;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

import java.util.ArrayList;

/**
 * Created by Nick on 4/22/2015.
 */
public class BasicVTTTest  extends ApplicationTestCase<Application> {
    public BasicVTTTest() {
        super(Application.class);
    }
    public void testVTT()
    {
        ArrayList<TextBlock> blocks = null;
        String path = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
        VTTFormat vtt = new VTTFormat(null);
        FileHelper.EncodingWrapper wrapper = FileHelper.readFile(path + "vtt/testVtt.txt", null);
        blocks = vtt.readFile(wrapper.data, wrapper.encoding);
        TimeBlock firstBlock = (TimeBlock) blocks.get(0);
        assertEquals(firstBlock.getStartTime(), 4190);
        assertEquals("Begin Text.<br>", firstBlock.text);
        TimeBlock lastBlock = (TimeBlock) blocks.get(blocks.size()-1);
        assertEquals("End Text.<br>",lastBlock.text);
    }
    public void testVTTParseTime()
    {
        VTTFormat vtt = new VTTFormat(null);
        int result = vtt.parseTimeStamp("12:34:56.789");
        assertEquals(result,12*60*60*1000+34*60*1000+56*1000+789);
    }
}