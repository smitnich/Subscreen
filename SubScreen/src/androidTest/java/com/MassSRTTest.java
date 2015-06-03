package com.subscreen.subscreen;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.subscreen.FileHelper;
import com.subscreen.SubtitlePlayer;
import com.subscreen.Subtitles.SrtFormat;
import com.subscreen.Subtitles.SubtitleFormat;
import com.subscreen.TextBlock;
import com.subscreen.TimeBlock;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Nick on 4/24/2015.
 */
public class MassSRTTest extends ApplicationTestCase<Application> {
    public MassSRTTest() {
        super(Application.class);
    }
    public void testAllFiles()
    {
        long startTime = 500;
        long endTime = 2000;
        String endString = "<font color=\"#ffff00\" size=14>www.moviesubtitles.org</font>";
        ArrayList<TextBlock> blocks = null;
        String path = System.getenv("EXTERNAL_STORAGE") + "/Subtitles/srt/bulk/";
        File bulkFolder = new File(path);
        String tmpFile = "";
        SubtitlePlayer playerInstance = new SubtitlePlayer();
        SubtitleFormat sub;
        FileHelper.EncodingWrapper wrapper;
        for (File f : bulkFolder.listFiles()) {
                try {
                    tmpFile = f.getAbsolutePath();
                    wrapper = FileHelper.readFile(tmpFile, null);
                    BufferedInputStream data = new BufferedInputStream(wrapper.data);
                    if (f.isDirectory())
                        continue;
                    sub = playerInstance.pickFormat(data, wrapper.encoding);
                    blocks = sub.readFile(data, wrapper.encoding);
                    if (blocks == null)
                    {
                        Log.e("EXCEPTION","File " + tmpFile + " did not read");
                        continue;
                    }
                    if (blocks.get(0).showFramerates())
                    {
                        //Log.e("INFO", "File " + tmpFile + " is MicroDVD format");
                        continue;
                    }
                }
                catch (Exception e)
                {
                    Log.e("EXCEPTION","File " + tmpFile + " failed");
                    e.printStackTrace();
                }
            }
        }
    }
