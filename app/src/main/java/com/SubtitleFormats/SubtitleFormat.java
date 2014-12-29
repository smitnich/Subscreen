package com.subscreen.SubtitleFormats;
import java.util.ArrayList;

import android.content.Context;

import com.subscreen.TextBlock;


public interface SubtitleFormat {
	ArrayList<TextBlock> readFile(String path);
}