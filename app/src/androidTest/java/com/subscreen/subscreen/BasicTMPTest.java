package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreen.Subtitles.TmpFormat;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

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
            blocks = tmp.readFile(path+"/TMP/testTMP.txt", "UTF-8");
            assertEquals(blocks.get(0).getStartTime(),2000);
            TextBlock tmpBlock = blocks.get(blocks.size()-1);
            assertEquals(blocks.size(), 439);
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
            blocks = tmp.readFile(path+"/TMP/testTMPParse.txt", "UTF-8");
            TimeBlock block1 = (TimeBlock) blocks.get(0);
            TimeBlock block2 = (TimeBlock) blocks.get(1);
            assertEquals(block1.text,"Line 1<br>Line 2");
            assertEquals(block2.text,"Line 1<br>Line 2<br>Line 3<br>Line 4");
        }
}
