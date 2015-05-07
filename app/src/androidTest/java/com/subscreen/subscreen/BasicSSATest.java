package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreen.FileHelper;
import com.subscreen.Subtitles.ASSFormat;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

import java.util.ArrayList;

/**
 * Created by Nick on 4/21/2015.
 */
public class BasicSSATest extends ApplicationTestCase<Application> {
public BasicSSATest() {
            super(Application.class);
        }
        public void testSSA() {
            ArrayList<TextBlock> blocks = null;
            String path = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
            ASSFormat ssa = new ASSFormat(null);
            blocks = ssa.readFile(FileHelper.readFile(path + "ssa/testSSA.ass", null), "UTF-8");
            assertEquals(blocks.get(0).getStartTime(), 2*60*1000 + 36*1000 + 40);
            TimeBlock lastBlock = (TimeBlock) blocks.get(blocks.size()-1);
            assertEquals(lastBlock.text, "End Text.");
        }
        public void testSSAParseTime()
        {
            ASSFormat ssa = new ASSFormat(null);
            long time = ssa.parseTimeStamp("12:34:56.78");
            assertEquals(time,12*60*60*1000+34*60*1000+56*1000+780);
        }
}
