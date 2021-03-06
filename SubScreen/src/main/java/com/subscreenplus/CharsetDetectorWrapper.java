package com.subscreenplus;

import org.mozilla.universalchardet.UniversalDetector;

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
            String charset = detector.getDetectedCharset();
            //If juniversalcharsetdetector was not able to guess the encoding, default to UTF-8
            if (charset == null)
                return "UTF-8";
            else
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
