package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.subscreen.FileHelper;
import com.subscreen.Subtitles.SMIFormat;
import com.subscreen.TextBlock;

import java.util.ArrayList;

/**
 * Created by Nick on 4/21/2015.
 */
public class BasicSMITest extends ApplicationTestCase<Application> {
    public BasicSMITest() {
        super(Application.class);
    }
    public void testSMI() {
        ArrayList<TextBlock> blocks = null;
        String path = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/";
        SMIFormat smi = new SMIFormat(null);
        blocks = smi.readFile(FileHelper.readFile(path + "SMI/test.smi", null), "ISO-8859-1");
        assertEquals(blocks.get(0).getStartTime(),3138);
        assertEquals(blocks.size(), 1335);
    }
}
