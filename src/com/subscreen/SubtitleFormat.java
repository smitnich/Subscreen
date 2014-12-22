package com.subscreen;
import java.util.ArrayList;

import android.content.Context;


public interface SubtitleFormat {
	ArrayList<TextBlock> readFile(String path);
}