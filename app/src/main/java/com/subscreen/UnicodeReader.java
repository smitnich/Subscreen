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
    int tmp = 0;
    boolean done = false;
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
                        if (limit == -1)
                        {
                            return out;
                        }
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
                    tmp = buffer[i++];
                    //All bytes are signed in java, meaning that they will be interpreted as negative
                    //values by default. By casting up to an int, and masking out all but the first
                    //8 bits, we make it positive and removing all of the leading ones in a negative
                    //number
                    out[j++] = (char) (tmp & 0xFF);
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
        if (limit == -1)
            return 0;
        else
            return 1;
	}
}