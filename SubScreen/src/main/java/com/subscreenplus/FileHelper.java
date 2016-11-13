package com.subscreenplus;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        catch (Exception e) {
            return null;
        }
        return names;
    }
    public static EncodingWrapper readFile(String path, String zipFileName) {
        InputStream data = null;
        String encoding = null;
        try {
            if (zipFileName != null) {
                ZipFile zf = new ZipFile(path);
                data = zf.getInputStream(zf.getEntry(zipFileName));
                encoding = CharsetDetectorWrapper.guessEncoding(data);
                data.close();
                data = zf.getInputStream(zf.getEntry(zipFileName));
            }
            else {
                data = new FileInputStream(path);
                encoding = CharsetDetectorWrapper.guessEncoding(data);
                data.close();
                data = new FileInputStream(path);
            }
        } catch (IOException e) {
            return null;
        }
        return new EncodingWrapper(data,encoding);
    }
    public static class EncodingWrapper {
        public InputStream data;
        public String encoding;
        EncodingWrapper(InputStream _data, String _encoding) {
            data = _data;
            encoding = _encoding;
        }
    }
}
