package com.subscreenplus.Subtitles;
import java.io.InputStream;
import java.util.ArrayList;

import com.subscreenplus.TextBlock;


public interface SubtitleFormat {
	ArrayList<TextBlock> readFile(InputStream data, String srcCharset);
}