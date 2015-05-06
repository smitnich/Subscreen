package com.subscreen;

import com.subscreen.Subtitles.ASSFormat;
import com.subscreen.Subtitles.MPLFormat;
import com.subscreen.Subtitles.MicroDVDFormat;
import com.subscreen.Subtitles.SMIFormat;
import com.subscreen.Subtitles.SrtFormat;
import com.subscreen.Subtitles.SubViewerTwoFormat;
import com.subscreen.Subtitles.SubtitleFormat;
import com.subscreen.Subtitles.TmpFormat;
import com.subscreen.Subtitles.VTTFormat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.PushbackReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
/**
 * Created by Nick on 5/3/2015.
 */
public class FileHelper {
    public static ArrayList<String> readZipFile(String path) {
        ArrayList<String> names = new ArrayList<String>();
        try {
            ZipFile zf = new ZipFile(path);
            Enumeration entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) entries.nextElement();
                String name = ze.getName();
                //Don't include any .nfo files; they do not contain subtitle data
                if (!name.endsWith(".nfo") && !ze.isDirectory()) {
                    names.add(name);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return names;
    }
    public static InputStream readFile(String path, String zipFileName) {
        InputStream data = null;
        try {
            if (zipFileName != null) {
                ZipFile zf = new ZipFile(path);
                data = zf.getInputStream(zf.getEntry(zipFileName));
            }
            else {
                data = new FileInputStream(path);
            }
        } catch (IOException e) {
            return null;
        }
        return data;
    }
}
