package com.subscreen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.CharBuffer;

public class UnicodeReader {
    byte[] buffer = new byte[1024];
    int bufferOffset = 0;
	FileInputStream fis = null;
    int limit = 0;
	public UnicodeReader(String fileName) {
		try {
			fis = new FileInputStream(fileName);
            limit = fis.read(buffer);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
	}
	public char[] readLine()
	{
		char[] out = null;
		int i = bufferOffset;
        int j = 0;
		try {
                out = new char[buffer.length];
                while (true)
                {
                    if (i >= limit)
                    {
                        limit = fis.read(buffer);
                        bufferOffset = 0;
                        i = 0;
                        out[j++] = (char) buffer[i++];
                    }
                    if (buffer[i] == '\r' || buffer[i] == '\n') {
                        if ((i+1 < limit) && (buffer[i+1] == '\r' || buffer[i+1] == '\n'))
                            i += 2;
                        else
                            i+= 1;
                        out[j++] = 0;
                        break;
                    }
                    out[j++] = (char) buffer[i++];
                }
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
        catch (Exception e)
        {
            e.printStackTrace();
        }
        bufferOffset = i;
		return out;
	}
	public void close()
	{
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	public int available() throws IOException
	{
        int test = fis.available();
		return fis.available();
	}
}