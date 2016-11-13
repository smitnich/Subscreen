package com.subscreenplus.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreenplus.FileHelper;
import com.subscreenplus.Subtitles.TmpFormat;
import com.subscreenplus.TextBlock;
import com.subscreenplus.TimeBlock;

import java.util.ArrayList;

/**
 * Created by Nick on 4/21/2015.
 */
    public class BasicTMPTest extends ApplicationTestCase<Application> {
        public BasicTMPTest() {
            super(Application.class);
        }
        public void testTmp() {
            ArrayList<TextBlock> blocks = null;
            String path = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
            TmpFormat tmp = new TmpFormat(null);
            FileHelper.EncodingWrapper wrapper = FileHelper.readFile(path + "/TMP/testTMP.txt", null);
            blocks = tmp.readFile(wrapper.data, wrapper.encoding);
            assertEquals(blocks.get(1).getStartTime(),2000);
            TextBlock tmpBlock = blocks.get(blocks.size()-1);
            assertEquals(blocks.size(), 440);
        }
        public void testTMPParseTime()
        {
            TmpFormat tmp = new TmpFormat(null);
            long time = tmp.parseTimeStamp("12:34:56");
            assertEquals(time, 12 * 60 * 60 * 1000 + 34 * 60 * 1000 + 56 * 1000);
        }
        public void testHTMLConversion()
        {
            ArrayList<TextBlock> blocks = null;
            String path = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
            TmpFormat tmp = new TmpFormat(null);
            FileHelper.EncodingWrapper wrapper = FileHelper.readFile(path+"/TMP/testTMPParse.txt",null);
            blocks = tmp.readFile(wrapper.data, wrapper.encoding);
            TimeBlock block1 = (TimeBlock) blocks.get(1);
            TimeBlock block2 = (TimeBlock) blocks.get(2);
            assertEquals(block1.text,"Line 1<br>Line 2");
            assertEquals(block2.text,"Line 1<br>Line 2<br>Line 3<br>Line 4");
        }
}
