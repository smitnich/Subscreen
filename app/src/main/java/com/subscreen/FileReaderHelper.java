package com.subscreen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.io.FileInputStream;
import java.io.InputStreamReader;

//A helper file to wrap around a FileReader in order to have access to the readLine() method
public class FileReaderHelper {
    char[] buffer = new char[1024];
    int bufferOffset = 0;
	BufferedReader br = null;
    int limit = 0;
    int tmp = 0;
    boolean done = false;
	public FileReaderHelper(String fileName, String charSetName) {
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), Charset.forName(charSetName)));
            limit = br.read(buffer);
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
                        limit = br.read(buffer);
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
                    out[j++] = buffer[i++];
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
			br.close();
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