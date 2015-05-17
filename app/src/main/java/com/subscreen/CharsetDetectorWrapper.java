package com.subscreen;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by Nick on 5/16/2015.
 */
public class CharsetDetectorWrapper {
    public static String guessEncoding(InputStream fis) {
        byte[] buffer = new byte[4096];
        UniversalDetector detector = null;
        try {
            detector = new UniversalDetector(null);
            int nread;
            while ((nread = fis.read(buffer)) > 0 && !detector.isDone()) {
                detector.handleData(buffer, 0, nread);
            }
            detector.dataEnd();
            return detector.getDetectedCharset();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (detector != null)
                    detector.reset();
            } catch (Exception e) {
            }
        }
    }
}
