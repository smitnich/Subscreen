package com.subscreen.Subtitles;
import java.util.ArrayList;

import com.subscreen.TextBlock;


public interface SubtitleFormat {
	ArrayList<TextBlock> readFile(String path, String srcCharset);
}