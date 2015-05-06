package com.subscreen.Subtitles;
import java.io.InputStream;
import java.util.ArrayList;

import com.subscreen.TextBlock;


public interface SubtitleFormat {
	ArrayList<TextBlock> readFile(InputStream data, String srcCharset);
}