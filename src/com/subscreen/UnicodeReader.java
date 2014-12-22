package com.subscreen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


public class UnicodeReader {

	FileInputStream fis = null;
	/**
	 * @param args
	 */
	public UnicodeReader(String fileName) {
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
	}
	public char[] readLine()
	{
		int tmpChar = -1;
		char[] buffer = new char[1024];
		char[] out = null;
		int i = 0;
		try {
			while (fis.available() > 0)
			{
				tmpChar = fis.read();
				if (tmpChar == '\n')
				{
					out = new char[i];
					for (int j = 0; j < i; j++)
					{
						out[j] = buffer[j];
					}
					break;
				}
				buffer[i++] = (char) tmpChar;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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
		return fis.available();
	}
}